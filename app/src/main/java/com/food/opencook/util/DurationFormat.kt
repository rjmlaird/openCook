/*
 *  openCook
 *  Copyright (C) 2026 olie.xdev <olie.xdeveloper@googlemail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.food.opencook.util

import java.util.Locale

/**
 * Converts between schema.org ISO-8601 durations ("PT25M", "PT1H10M") and a
 * human form localized to the device language ("1 Std 10 Min" in German, "1 h 10 min"
 * otherwise). Storage stays ISO-8601; only the UI shows/edits the friendly form.
 * Parsing accepts both German ("Std/Min") and English ("h/min") units, so an edited
 * value round-trips regardless of locale. Anything that doesn't look like a duration is
 * passed through unchanged, so user free-text is never destroyed.
 */
object DurationFormat {

    // Accept an optional seconds component too: some recipes (e.g. AI-extracted Japanese
    // ones) carry durations like "PT900S" or "PT0M" instead of "PT15M". Seconds are folded
    // into minutes below so the UI never shows a raw ISO string. See GitHub issue #2.
    private val ISO = Regex("""^PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?$""", RegexOption.IGNORE_CASE)
    private val HOURS = Regex("""(\d+)\s*(?:Std|Stunde|Stunden|h)""", RegexOption.IGNORE_CASE)
    private val MINUTES = Regex("""(\d+)\s*(?:Min|Minuten|m)""", RegexOption.IGNORE_CASE)

    /** A matched [ISO] duration as total minutes, seconds rounded to the nearest minute. */
    private fun totalMinutes(match: MatchResult): Int {
        val hours = match.groupValues[1].toIntOrNull() ?: 0
        val minutes = match.groupValues[2].toIntOrNull() ?: 0
        val seconds = match.groupValues[3].toIntOrNull() ?: 0
        return (hours * 3600 + minutes * 60 + seconds + 30) / 60
    }

    /** ISO-8601 -> total minutes ("PT1H10M" -> 70, "PT900S" -> 15), or null if not a PT duration. */
    fun minutes(iso: String?): Int? {
        if (iso.isNullOrBlank()) return null
        val match = ISO.matchEntire(iso.trim()) ?: return null
        val total = totalMinutes(match)
        return if (total > 0) total else null
    }

    /** ISO-8601 -> "1 Std 10 Min". Returns the input unchanged if it isn't a PT duration. */
    fun toHuman(iso: String?): String {
        if (iso.isNullOrBlank()) return ""
        val match = ISO.matchEntire(iso.trim()) ?: return iso
        val total = totalMinutes(match)
        val hours = total / 60
        val minutes = total % 60
        val (h, m) = if (Locale.getDefault().language == "de") "Std" to "Min" else "h" to "min"
        return when {
            hours > 0 && minutes > 0 -> "$hours $h $minutes $m"
            hours > 0 -> "$hours $h"
            minutes > 0 -> "$minutes $m"
            else -> "" // a matched but zero-length duration ("PT0M") shows nothing, not raw ISO
        }
    }

    /**
     * "1 Std 10 Min" -> "PT70M". Returns null for blank input; passes through text
     * that carries no recognisable hours/minutes (stored verbatim).
     */
    fun toIso(text: String?): String? {
        val trimmed = text?.trim().orEmpty()
        if (trimmed.isEmpty()) return null
        if (ISO.matches(trimmed)) return trimmed.uppercase()
        val hours = HOURS.find(trimmed)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val minutes = MINUTES.find(trimmed)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val total = hours * 60 + minutes
        return if (total > 0) "PT${total}M" else trimmed
    }
}
