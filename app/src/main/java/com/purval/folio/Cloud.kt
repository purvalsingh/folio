package com.purval.folio

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/*
 * Accounts and sync, in three layers of protection:
 *  1. Transport — HTTPS only; Supabase stores passwords as bcrypt hashes, never in plain text.
 *  2. Server   — row-level security: a signed-in user can read and write only their own row.
 *  3. Content  — the library is encrypted ON THE PHONE before upload (AES-256-GCM, key derived from
 *                the password with PBKDF2, 210k rounds). The server only ever holds ciphertext.
 *  On the device, the session and the derived key are themselves sealed by an Android Keystore key
 *  that never leaves the phone's secure hardware.
 */

/**
 * Where accounts live. Delivered by the online catalog (so the backend can be switched on or moved
 * without an app update) and cached on the phone. BASE is a Vercel rewrite to Supabase, because
 * supabase.co is blocked on some Indian ISPs. ANON_KEY is Supabase's public "publishable" key.
 */
object CloudConfig {
    var BASE by androidx.compose.runtime.mutableStateOf("")
    var ANON_KEY by androidx.compose.runtime.mutableStateOf("")
    val ready get() = BASE.startsWith("https://") && ANON_KEY.isNotBlank()

    fun load(ctx: Context) {
        val p = ctx.getSharedPreferences("folio_account", Context.MODE_PRIVATE)
        BASE = p.getString("base", "")!!; ANON_KEY = p.getString("anon", "")!!
    }

    fun update(ctx: Context, base: String, anon: String) {
        if (!base.startsWith("https://") || anon.isBlank()) return
        BASE = base.trimEnd('/') + "/"; ANON_KEY = anon
        ctx.getSharedPreferences("folio_account", Context.MODE_PRIVATE).edit().putString("base", BASE).putString("anon", anon).apply()
    }
}

private object B64 {
    fun enc(b: ByteArray): String = java.util.Base64.getEncoder().encodeToString(b)
    fun dec(s: String): ByteArray = java.util.Base64.getDecoder().decode(s)
}

/** Seals small secrets with a hardware-backed AES key that cannot be exported from the device. */
object Vault {
    private const val ALIAS = "folio_vault"

    private fun key(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        gen.init(KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256).build())
        return gen.generateKey()
    }

    fun seal(plain: String): String {
        val c = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key()) }
        return B64.enc(c.iv + c.doFinal(plain.toByteArray()))
    }

    fun open(sealed: String): String? = runCatching {
        val b = B64.dec(sealed)
        val c = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, b, 0, 12)) }
        String(c.doFinal(b, 12, b.size - 12))
    }.getOrNull()
}

/** End-to-end encryption of the synced library. */
object Crypt {
    fun deriveKey(password: String, userId: String): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), "folio:$userId".toByteArray(), 210_000, 256)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }

    fun encrypt(key: ByteArray, plain: String): String {
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
        val c = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(128, iv)) }
        return "v1:" + B64.enc(iv + c.doFinal(plain.toByteArray()))
    }

    /** null when the key is wrong (e.g. the password was reset since this copy was made). */
    fun decrypt(key: ByteArray, blob: String): String? = runCatching {
        val b = B64.dec(blob.removePrefix("v1:"))
        val c = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(128, b, 0, 12)) }
        String(c.doFinal(b, 12, b.size - 12))
    }.getOrNull()
}

data class Session(val userId: String, val email: String, val access: String, val refresh: String, val expiresAt: Long, val dataKey: String)

class AuthError(msg: String) : Exception(msg)

object Cloud {
    private fun call(method: String, path: String, body: JSONObject? = null, token: String? = null, extra: Map<String, String> = emptyMap()): Pair<Int, String> {
        val c = URL(CloudConfig.BASE + path).openConnection() as HttpURLConnection
        c.requestMethod = method; c.connectTimeout = 15000; c.readTimeout = 30000
        c.setRequestProperty("apikey", CloudConfig.ANON_KEY)
        c.setRequestProperty("Authorization", "Bearer " + (token ?: CloudConfig.ANON_KEY))
        c.setRequestProperty("Content-Type", "application/json")
        extra.forEach { (k, v) -> c.setRequestProperty(k, v) }
        if (body != null) { c.doOutput = true; c.outputStream.use { it.write(body.toString().toByteArray()) } }
        val code = c.responseCode
        val text = (if (code in 200..299) c.inputStream else c.errorStream)?.bufferedReader()?.readText().orEmpty()
        return code to text
    }

    private fun friendly(text: String): String {
        val o = runCatching { JSONObject(text) }.getOrNull()
        val m = o?.optString("msg")?.ifBlank { null } ?: o?.optString("error_description")?.ifBlank { null } ?: o?.optString("message")?.ifBlank { null }
        return when {
            m == null -> "Could not reach the library. Check your connection."
            m.contains("Invalid login", true) -> "Email or password is wrong."
            m.contains("already registered", true) -> "That email already has an account. Sign in instead."
            m.contains("rate limit", true) -> "Too many attempts. Please wait a minute."
            else -> m
        }
    }

    private fun session(text: String, password: String?, old: Session? = null): Session {
        val o = JSONObject(text)
        val user = o.getJSONObject("user")
        val id = user.getString("id")
        val key = if (password != null) B64.enc(Crypt.deriveKey(password, id)) else old!!.dataKey
        return Session(id, user.optString("email"), o.getString("access_token"), o.getString("refresh_token"),
            System.currentTimeMillis() / 1000 + o.optLong("expires_in", 3600) - 60, key)
    }

    suspend fun signUp(email: String, password: String): Session = withContext(Dispatchers.IO) {
        val (code, text) = call("POST", "auth/v1/signup", JSONObject().put("email", email).put("password", password))
        if (code !in 200..299) throw AuthError(friendly(text))
        if (!JSONObject(text).has("access_token")) throw AuthError("Account created. Check your email to confirm it, then sign in.")
        session(text, password)
    }

    suspend fun signIn(email: String, password: String): Session = withContext(Dispatchers.IO) {
        val (code, text) = call("POST", "auth/v1/token?grant_type=password", JSONObject().put("email", email).put("password", password))
        if (code !in 200..299) throw AuthError(friendly(text))
        session(text, password)
    }

    /** Sends a 6-digit reset code by email. */
    suspend fun sendResetCode(email: String) = withContext(Dispatchers.IO) {
        val (code, text) = call("POST", "auth/v1/recover", JSONObject().put("email", email))
        if (code !in 200..299) throw AuthError(friendly(text))
    }

    /** Verifies the emailed code, sets the new password and signs in. Old cloud copies can't be read with the new key. */
    suspend fun resetPassword(email: String, codeText: String, newPassword: String): Session = withContext(Dispatchers.IO) {
        val (code, text) = call("POST", "auth/v1/verify", JSONObject().put("type", "recovery").put("email", email).put("token", codeText.trim()))
        if (code !in 200..299) throw AuthError(if (code == 403 || code == 401) "That code is wrong or has expired." else friendly(text))
        val access = JSONObject(text).getString("access_token")
        val (c2, t2) = call("PUT", "auth/v1/user", JSONObject().put("password", newPassword), token = access)
        if (c2 !in 200..299) throw AuthError(friendly(t2))
        session(text, newPassword)
    }

    suspend fun fresh(s: Session): Session = withContext(Dispatchers.IO) {
        if (System.currentTimeMillis() / 1000 < s.expiresAt) return@withContext s
        val (code, text) = call("POST", "auth/v1/token?grant_type=refresh_token", JSONObject().put("refresh_token", s.refresh))
        if (code !in 200..299) throw AuthError("Your session ended. Please sign in again.")
        session(text, null, s)
    }

    suspend fun signOut(s: Session) = withContext(Dispatchers.IO) { runCatching { call("POST", "auth/v1/logout", token = s.access) } }

    /** Returns the decrypted library JSON, "" if there is no cloud copy yet, or null if it can't be decrypted. */
    suspend fun pull(s: Session): String? = withContext(Dispatchers.IO) {
        val (code, text) = call("GET", "rest/v1/folio_sync?select=blob&user_id=eq.${s.userId}", token = s.access)
        if (code !in 200..299) throw AuthError(friendly(text))
        val arr = JSONArray(text)
        if (arr.length() == 0) "" else Crypt.decrypt(B64.dec(s.dataKey), arr.getJSONObject(0).getString("blob"))
    }

    suspend fun push(s: Session, plain: String) = withContext(Dispatchers.IO) {
        val blob = Crypt.encrypt(B64.dec(s.dataKey), plain)
        val (code, text) = call("POST", "rest/v1/folio_sync?on_conflict=user_id",
            JSONObject().put("user_id", s.userId).put("blob", blob).put("updated_at", java.time.Instant.now().toString()),
            token = s.access, extra = mapOf("Prefer" to "resolution=merge-duplicates,return=minimal"))
        if (code !in 200..299) throw AuthError(friendly(text))
    }

    suspend fun deleteCloudCopy(s: Session) = withContext(Dispatchers.IO) {
        val (code, text) = call("DELETE", "rest/v1/folio_sync?user_id=eq.${s.userId}", token = s.access)
        if (code !in 200..299) throw AuthError(friendly(text))
    }

    // ---- sealed session storage ----
    fun save(ctx: Context, s: Session?) {
        val p = ctx.getSharedPreferences("folio_account", Context.MODE_PRIVATE).edit()
        if (s == null) p.remove("s") else p.putString("s", Vault.seal(JSONObject()
            .put("u", s.userId).put("e", s.email).put("a", s.access).put("r", s.refresh).put("x", s.expiresAt).put("k", s.dataKey).toString()))
        p.apply()
    }

    fun load(ctx: Context): Session? {
        val sealed = ctx.getSharedPreferences("folio_account", Context.MODE_PRIVATE).getString("s", null) ?: return null
        val o = Vault.open(sealed)?.let { runCatching { JSONObject(it) }.getOrNull() } ?: return null
        return Session(o.getString("u"), o.getString("e"), o.getString("a"), o.getString("r"), o.getLong("x"), o.getString("k"))
    }
}
