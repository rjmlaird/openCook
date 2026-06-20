package com.food.opencook

import com.food.opencook.util.DurationFormat
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.Locale

class DurationFormatTest {

    private val originalLocale = Locale.getDefault()

    // toHuman() localizes its unit labels to the default locale; pin it for deterministic assertions.
    @Before fun setGerman() = Locale.setDefault(Locale.GERMAN)
    @After fun restore() = Locale.setDefault(originalLocale)

    @Test
    fun isoToHuman() {
        assertEquals("25 Min", DurationFormat.toHuman("PT25M"))
        assertEquals("1 Std", DurationFormat.toHuman("PT1H"))
        assertEquals("1 Std 10 Min", DurationFormat.toHuman("PT1H10M"))
        assertEquals("", DurationFormat.toHuman(null))
        assertEquals("", DurationFormat.toHuman(""))
    }

    @Test
    fun isoToHumanEnglishLocale() {
        Locale.setDefault(Locale.ENGLISH)
        assertEquals("25 min", DurationFormat.toHuman("PT25M"))
        assertEquals("1 h", DurationFormat.toHuman("PT1H"))
        assertEquals("1 h 10 min", DurationFormat.toHuman("PT1H10M"))
        // English units must still round-trip back to ISO.
        assertEquals("PT70M", DurationFormat.toIso(DurationFormat.toHuman("PT1H10M")))
    }

    @Test
    fun nonIsoPassesThrough() {
        assertEquals("über Nacht", DurationFormat.toHuman("über Nacht"))
    }

    @Test
    fun humanToIso() {
        assertEquals("PT25M", DurationFormat.toIso("25 Min"))
        assertEquals("PT70M", DurationFormat.toIso("1 Std 10 Min"))
        assertEquals("PT120M", DurationFormat.toIso("2 Std"))
        assertNull(DurationFormat.toIso(""))
        assertNull(DurationFormat.toIso(null))
    }

    @Test
    fun alreadyIsoIsKept() {
        assertEquals("PT25M", DurationFormat.toIso("PT25M"))
    }

    @Test
    fun unparseableTextStoredVerbatim() {
        assertEquals("über Nacht", DurationFormat.toIso("über Nacht"))
    }

    @Test
    fun roundTrips() {
        assertEquals("PT25M", DurationFormat.toIso(DurationFormat.toHuman("PT25M")))
        assertEquals("PT70M", DurationFormat.toIso(DurationFormat.toHuman("PT1H10M")))
    }
}
