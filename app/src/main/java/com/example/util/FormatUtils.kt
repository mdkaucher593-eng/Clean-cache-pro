package com.example.util

import java.text.CharacterIterator
import java.text.DateFormat
import java.text.StringCharacterIterator
import java.util.Date
import java.util.Locale

object FormatUtils {

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        var b = bytes
        if (b < 1024) return "$b B"
        val ci: CharacterIterator = StringCharacterIterator("KMGTPE")
        var i = 40
        while (i >= 0 && bytes > (0xfffccccccccccccL shr i)) {
            b = b shr 10
            ci.next()
            i -= 10
        }
        b = b shr 10
        ci.next()
        val unit = ci.current()
        val value = bytes.toDouble() / (1L shl (10 * (ci.index + 1)))
        return String.format(Locale.US, "%.1f %cB", value, unit)
    }

    fun formatDate(timestamp: Long): String {
        if (timestamp <= 0) return "Unknown"
        val formatter = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
        return formatter.format(Date(timestamp))
    }
}
