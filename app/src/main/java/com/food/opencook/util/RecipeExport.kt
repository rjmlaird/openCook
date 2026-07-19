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

import com.food.opencook.data.local.relation.RecipeWithDetails
import com.food.opencook.data.remote.dto.HowToStepDto
import com.food.opencook.data.remote.dto.IngredientDto
import com.food.opencook.data.remote.dto.NutritionDto
import com.food.opencook.data.remote.dto.RecipeDto

/**
 * Turns a saved recipe back into the same schema.org/Recipe + openCook-extension shape
 * [com.food.opencook.data.recipeimport.RecipeImportParser] reads on the way in — so an
 * exported recipe round-trips: re-importing the file this produces (into openCook again,
 * a household member's device, or any other app that speaks schema.org/Recipe) recreates
 * the recipe with its structured ingredients intact, not just a flattened text blob.
 *
 * Images are deliberately **not** included: [com.food.opencook.data.local.entity.ImageEntity]
 * holds either a device-local file path or a bare filename that only resolves against this
 * household's own self-hosted server — neither is portable to whoever opens the exported
 * file, so emitting one would be actively misleading rather than merely incomplete.
 */
object RecipeExport {

    fun toDto(data: RecipeWithDetails): RecipeDto {
        val recipe = data.recipe
        return RecipeDto(
            context = "https://schema.org",
            type = "Recipe",
            name = recipe.name,
            recipeYield = recipe.recipeYield,
            openCookServings = recipe.servings,
            openCookCategory = recipe.category,
            recipeIngredient = data.ingredients.sortedBy { it.position }.map { ingredientLine(it.quantity, it.unit, it.name) },
            recipeInstructions = data.instructions.sortedBy { it.position }
                .map { HowToStepDto(type = "HowToStep", text = it.text) },
            openCookIngredients = data.ingredients.sortedBy { it.position }
                .map { IngredientDto(quantity = it.quantity, unit = it.unit, name = it.name) },
            openCookNotes = recipe.notes.orEmpty().lines().map(String::trim).filter(String::isNotEmpty),
            openCookTags = recipe.tags.orEmpty().lines().map(String::trim).filter(String::isNotEmpty),
            prepTime = recipe.prepTime,
            cookTime = recipe.cookTime,
            totalTime = recipe.totalTime,
            nutrition = data.nutrition?.let {
                NutritionDto(
                    type = "NutritionInformation",
                    calories = it.calories,
                    proteinContent = it.proteinContent,
                    fatContent = it.fatContent,
                    carbohydrateContent = it.carbohydrateContent,
                    fiberContent = it.fiberContent,
                    sugarContent = it.sugarContent,
                    openCookBasis = it.basis,
                )
            },
            cookbook = recipe.cookbook,
        )
    }

    /** "400 g Nudeln" / "3 Eier" / "etwas Salz" — the flattened text line kept alongside the
     *  structured fields, for any importer that only understands plain recipeIngredient text. */
    private fun ingredientLine(quantity: Double?, unit: String?, name: String): String =
        listOfNotNull(quantity?.let(::formatQuantity), unit, name).joinToString(" ")

    /** Room stores quantity as Double; print "400" rather than "400.0" for whole numbers. */
    private fun formatQuantity(quantity: Double): String =
        if (quantity == quantity.toLong().toDouble()) quantity.toLong().toString() else quantity.toString()
}
