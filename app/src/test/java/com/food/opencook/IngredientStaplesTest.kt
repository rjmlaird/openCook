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

import com.food.opencook.util.CommonGroceries
import com.food.opencook.util.IngredientMatch
import com.food.opencook.util.IngredientStaples
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IngredientStaplesTest {

    @Test
    fun `every staple is also a known grocery (spelling guard)`() {
        // The match is plural-aware so "salz und pfeffer" — a combo form — is the only
        // entry that doesn't appear as a single word in CommonGroceries; everything else
        // must show up in the curated vocabulary to keep spellings consistent across the
        // app (the corrector, suggestion pool and add-fields all draw from there).
        val groceries = CommonGroceries.LIST
        val missing = IngredientStaples.ALL
            .filter { it != "salz und pfeffer" }
            .filter { !IngredientMatch.containsLike(groceries, it) }
        assertTrue(
            "Staples not found in CommonGroceries — fix the spelling: $missing",
            missing.isEmpty(),
        )
    }

    @Test
    fun `plural and singular forms are recognised`() {
        assertTrue(IngredientStaples.isStaple("Salz"))
        assertTrue(IngredientStaples.isStaple("salz"))
        assertTrue(IngredientStaples.isStaple("Brühen"))
        assertTrue(IngredientStaples.isStaple("Backpulver"))
    }

    @Test
    fun `culinary main ingredients are NOT staples`() {
        // These are deliberately excluded so the reuse / cardinality score stays meaningful.
        listOf("Zwiebel", "Knoblauch", "Ei", "Milch", "Petersilie",
               "Aubergine", "Champignons", "Hackfleisch", "Tomate").forEach {
            assertFalse("$it should NOT be a staple", IngredientStaples.isStaple(it))
        }
    }

    @Test
    fun `staples are recognised through quantity and usage qualifiers`() {
        // Real extracted names carry parentheticals and "zum …" usage phrases; the staple
        // filter must see through them, otherwise salt/butter pollute the planner scoring.
        assertTrue(IngredientStaples.isStaple("Butter zum Anbraten"))
        assertTrue(IngredientStaples.isStaple("Salz nach Belieben"))
        assertTrue(IngredientStaples.isStaple("Rapsöl (ca. 2 EL)"))
        // Guard: a real main ingredient stays a non-staple even with a quantity glued on.
        assertFalse(IngredientStaples.isStaple("Hähnchenbrustfilets (ca. 400 g)"))
    }

    @Test
    fun `union lexicon recognises staples from every bundled language`() {
        // LocalizedLists builds ALL as the union across every bundled content language, so an
        // English-locale device with German recipes (or vice versa) still filters staples.
        val original = IngredientStaples.ALL
        try {
            IngredientStaples.ALL = setOf("salz", "wasser", "salt", "water")
            assertTrue(IngredientStaples.isStaple("Salz")) // German recipe…
            assertTrue(IngredientStaples.isStaple("Salt")) // …English recipe — both inert
            assertTrue(IngredientStaples.isStaple("Wasser"))
            assertTrue(IngredientStaples.isStaple("Water"))
        } finally {
            IngredientStaples.ALL = original
        }
    }

    @Test
    fun `default pantry is a subset of staples (modulo case)`() {
        IngredientStaples.DEFAULT_PANTRY.forEach { item ->
            assertTrue(
                "$item is in DEFAULT_PANTRY but not recognised as staple",
                IngredientStaples.isStaple(item),
            )
        }
    }
}
