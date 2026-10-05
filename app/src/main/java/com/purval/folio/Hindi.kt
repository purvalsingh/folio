package com.purval.folio

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * English → Hindi for meanings and examples, on the phone (Google ML Kit). The ~30 MB language
 * model downloads once; after that it works offline, for every book including the reader's own PDFs.
 */
object Hindi {
    private val cache = HashMap<String, String>()
    private val translator: Translator by lazy {
        Translation.getClient(TranslatorOptions.Builder().setSourceLanguage(TranslateLanguage.ENGLISH).setTargetLanguage(TranslateLanguage.HINDI).build())
    }

    private suspend fun <T> Task<T>.await(): T? = suspendCancellableCoroutine { c ->
        addOnSuccessListener { c.resume(it) }
        addOnFailureListener { c.resume(null) }
    }

    private suspend fun Task<*>.ok(): Boolean = suspendCancellableCoroutine { c ->
        addOnSuccessListener { c.resume(true) }
        addOnFailureListener { c.resume(false) }
    }

    /** Downloads the model if needed; false if offline on first use. */
    suspend fun prepare(): Boolean = translator.downloadModelIfNeeded(DownloadConditions.Builder().build()).ok()

    suspend fun of(text: String): String? {
        if (text.isBlank()) return ""
        cache[text]?.let { return it }
        if (!prepare()) return null
        return translator.translate(text).await()?.also { cache[text] = it }
    }
}
