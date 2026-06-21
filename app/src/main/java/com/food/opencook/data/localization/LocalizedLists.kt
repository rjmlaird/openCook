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

package com.food.opencook.data.localization

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import com.food.opencook.R
import com.food.opencook.data.recipeimport.IngredientLineParser
import com.food.opencook.data.settings.ContentLanguages
import com.food.opencook.data.settings.SettingsRepository
import com.food.opencook.util.GroceryCategories
import com.food.opencook.util.GroceryCategory
import com.food.opencook.util.IngredientStaples
import com.food.opencook.util.ProteinGroups
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads the language-specific domain word-lists (grocery-aisle keywords, staples, units)
 * from the per-locale arrays.xml resources for the effective recipe content language and
 * pushes them into the domain singletons. Resources are read via a locale-overridden context so
 * lists follow the household's `contentLanguage` setting, not just the device UI language.
 */
@Singleton
class LocalizedLists @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsRepository,
) {

    /** Reload for the current effective content language. Safe to call repeatedly. */
    suspend fun reload() {
        val lang = settings.effectiveContentLanguage(settings.contentLanguageOnce())
        apply(resourcesFor(lang))
    }

    private fun apply(res: Resources) {
        // Classification data (aisle keywords, staples, units) describes the ingredient *word*,
        // not the UI language: a household can run an English device but hold German recipes.
        // Loading only the active language's list would leave German words unmatched against the
        // English vocabulary (staples slip into the planner, ingredients fall into the "Other"
        // aisle, German units fail to parse). Union every bundled content language so both "salz"
        // and "salt", "Hähnchen" and "chicken", "EL" and "tbsp" are recognised regardless of locale.
        GroceryCategories.setRules(
            listOf(
                GroceryCategory.FROZEN to unionLower(R.array.grocery_kw_frozen),
                GroceryCategory.MEAT_FISH to unionLower(R.array.grocery_kw_meat_fish),
                GroceryCategory.PRODUCE to unionLower(R.array.grocery_kw_produce),
                GroceryCategory.BAKERY to unionLower(R.array.grocery_kw_bakery),
                GroceryCategory.DRINKS to unionLower(R.array.grocery_kw_drinks),
                GroceryCategory.SPICES to unionLower(R.array.grocery_kw_spices),
                GroceryCategory.PANTRY to unionLower(R.array.grocery_kw_pantry),
                GroceryCategory.DAIRY to unionLower(R.array.grocery_kw_dairy),
            ),
        )
        IngredientStaples.setData(
            all = unionLower(R.array.ingredient_staples).toSet(),
            // Pantry defaults keep their display capitalization (shown as-is in the pantry) and
            // stay in the active content language — they seed a new household's pantry rows.
            pantry = res.getStringArray(R.array.pantry_defaults).toList(),
        )
        IngredientLineParser.setUnits(unionLower(R.array.ingredient_units).toSet())
        // Main-protein keywords for meal-planner variety. Fixed canonical group keys (also shown
        // in the reason), translated keyword lists unioned across languages — priority order kept.
        ProteinGroups.setGroups(
            listOf(
                "geflügel" to unionLower(R.array.protein_kw_poultry),
                "fisch" to unionLower(R.array.protein_kw_fish),
                "hackfleisch" to unionLower(R.array.protein_kw_mince),
                "schwein" to unionLower(R.array.protein_kw_pork),
                "rind" to unionLower(R.array.protein_kw_beef),
                "lamm" to unionLower(R.array.protein_kw_lamb),
                "tofu" to unionLower(R.array.protein_kw_plant),
            ),
        )
    }

    private fun resourcesFor(lang: String): Resources {
        val config = Configuration(context.resources.configuration)
        config.setLocale(Locale.forLanguageTag(lang))
        return context.createConfigurationContext(config).resources
    }

    private fun Resources.lower(id: Int): List<String> = getStringArray(id).map { it.lowercase() }

    /** Union of a string-array across every bundled content language, so classification is
     *  locale-independent (the device UI language no longer decides which vocabulary applies). */
    private fun unionLower(id: Int): List<String> =
        ContentLanguages.CODES.flatMap { resourcesFor(it).lower(id) }
}
