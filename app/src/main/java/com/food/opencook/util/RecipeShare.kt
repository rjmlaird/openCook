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

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/**
 * Writes an exported recipe (schema.org/Recipe JSON, see [RecipeExport]) to a cache file
 * and hands back a share [Intent] for it. Same "private cache dir → content:// URI via
 * FileProvider" pattern as [IcsShare]; kept as its own small object rather than sharing
 * code with IcsShare so the two export features stay independent of each other.
 */
object RecipeShare {

    /** Writes [json] under cacheDir/exports/[fileNameHint].json and returns a chooser-ready
     *  ACTION_SEND intent for it (type "application/json" — Files, email, Drive, messaging
     *  apps, and openCook's own recipe import all offer to handle that). [fileNameHint] is
     *  sanitized since it's usually a recipe name, which may contain '/' or other characters
     *  a plain File path can't. */
    fun shareIntent(context: Context, json: String, fileNameHint: String): Intent {
        val safeName = fileNameHint.replace(Regex("[^A-Za-z0-9-_ ]"), "_").trim().ifEmpty { "recipe" }
        val dir = File(context.cacheDir, "exports")
        dir.mkdirs()
        val file = File(dir, "$safeName.json")
        file.writeText(json)

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
