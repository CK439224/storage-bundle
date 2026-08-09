package com.storagebundle.core.ocr

import com.storagebundle.core.common.result.Outcome

/**
 * Extracts text from a screenshot.
 *
 * The interface exists so ML Kit sits behind our own abstraction: the recognizer can be
 * swapped (for Tesseract, say) without touching feature code, and v0.1 is not welded to one
 * vendor's model (PLAN.md §4).
 *
 * Implementations must run entirely on-device. Nothing here may reach the network — the app
 * declares no network permission and the build fails if one appears.
 */
interface TextRecognizer {

    /**
     * Recognises text in the image identified by [mediaStoreId].
     *
     * @param mediaStoreId the MediaStore `_ID` of the image to read.
     * @return the extracted text, or a failure describing why recognition did not happen.
     *   An image containing no text is a [Outcome.Success] with an empty [RecognizedText.text].
     */
    suspend fun recognize(mediaStoreId: Long): Outcome<RecognizedText>
}

/**
 * Text extracted from a single image.
 *
 * @property mediaStoreId the image this text came from.
 * @property text the full extracted text, newline-separated by detected block.
 * @property confidence mean confidence across blocks, in `0.0..1.0`. Used to rank search
 *   results and to suppress noise from low-quality captures.
 */
data class RecognizedText(
    val mediaStoreId: Long,
    val text: String,
    val confidence: Float,
)
