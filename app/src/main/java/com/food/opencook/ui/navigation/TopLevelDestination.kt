package com.food.opencook.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.annotation.StringRes
import com.food.opencook.R

/**
 * Top-level destinations for the adaptive navigation (bottom bar on phones,
 * rail/drawer on tablets). Scanning is contextual (add-recipe / barcode), not a tab.
 */
enum class TopLevelDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    HOME("home", R.string.nav_home, Icons.Outlined.Home),
    RECIPES("recipes", R.string.nav_recipes, Icons.AutoMirrored.Outlined.MenuBook),
    PLAN("plan", R.string.nav_plan, Icons.Outlined.CalendarMonth),
    SHOPPING("shopping", R.string.nav_shopping_short, Icons.Outlined.ShoppingCart),
    SETTINGS("settings", R.string.nav_more, Icons.Outlined.Tune),
}
