package com.example.ui.components

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AppIconImage(
    packageName: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val context = LocalContext.current
    val iconState = produceState<Drawable?>(initialValue = null, key1 = packageName) {
        value = withContext(Dispatchers.IO) {
            try {
                context.packageManager.getApplicationIcon(packageName)
            } catch (e: Exception) {
                null
            }
        }
    }

    val drawable = iconState.value

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (drawable != null) {
            val bitmap = rememberBitmap(drawable)
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "App Icon",
                    modifier = Modifier.size(size)
                )
            } else {
                FallbackIcon(size)
            }
        } else {
            FallbackIcon(size)
        }
    }
}

@Composable
private fun rememberBitmap(drawable: Drawable): android.graphics.Bitmap? {
    return try {
        drawable.toBitmap(
            width = drawable.intrinsicWidth.coerceAtLeast(96),
            height = drawable.intrinsicHeight.coerceAtLeast(96)
        )
    } catch (e: Exception) {
        null
    }
}

@Composable
private fun FallbackIcon(size: Dp) {
    Icon(
        imageVector = Icons.Default.Android,
        contentDescription = "App Icon Placeholder",
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(size * 0.6f)
    )
}
