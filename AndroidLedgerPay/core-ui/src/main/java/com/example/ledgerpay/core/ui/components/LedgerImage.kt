package com.example.ledgerpay.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ledgerpay.core.ui.R

/**
 * Standardized Image Loader for the application using Coil.
 * Supports crossfade and placeholder/error states.
 */
@Composable
fun LedgerImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholder: Int? = null,
    error: Int? = null,
    contentScale: ContentScale = ContentScale.Crop
) {
    if (imageUrl.isNullOrBlank()) {
        if (placeholder != null) {
            Image(
                painter = painterResource(id = placeholder),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }
        return
    }

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        placeholder = placeholder?.let { painterResource(id = it) },
        error = error?.let { painterResource(id = it) } ?: placeholder?.let { painterResource(id = it) },
        contentScale = contentScale
    )
}
