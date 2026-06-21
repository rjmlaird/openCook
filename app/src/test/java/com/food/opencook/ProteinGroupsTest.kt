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

package com.food.opencook

import com.food.opencook.util.ProteinGroups
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProteinGroupsTest {

    @Test
    fun `classifies common proteins across languages`() {
        assertEquals("geflügel", ProteinGroups.groupOf("Hähnchenbrustfilets (ca. 400 g)"))
        assertEquals("geflügel", ProteinGroups.groupOf("chicken breast"))
        assertEquals("fisch", ProteinGroups.groupOf("Lachsfilet"))
        assertEquals("fisch", ProteinGroups.groupOf("salmon"))
        assertEquals("hackfleisch", ProteinGroups.groupOf("gemischtes Hackfleisch"))
        assertEquals("schwein", ProteinGroups.groupOf("Kochschinken"))
        assertEquals("tofu", ProteinGroups.groupOf("Räuchertofu"))
    }

    @Test
    fun `non-proteins are unclassified`() {
        assertNull(ProteinGroups.groupOf("Aubergine"))
        assertNull(ProteinGroups.groupOf("Spitzkohl"))
        assertNull(ProteinGroups.groupOf("Tomaten"))
        assertNull(ProteinGroups.groupOf(""))
    }

    @Test
    fun `keywords are resource-driven and grow with a translated language`() {
        // LocalizedLists swaps in the unioned, translated keyword lists. Adding a new language's
        // keywords (here French "poulet") must classify without touching ProteinGroups.kt.
        val original = ProteinGroups.activeGroups
        try {
            ProteinGroups.setGroups(
                listOf(
                    "geflügel" to listOf("hähnchen", "chicken", "poulet"),
                    "fisch" to listOf("fisch", "fish", "poisson"),
                ),
            )
            assertEquals("geflügel", ProteinGroups.groupOf("Poulet rôti"))
            assertEquals("fisch", ProteinGroups.groupOf("Filet de poisson"))
        } finally {
            ProteinGroups.setGroups(original)
        }
    }
}
