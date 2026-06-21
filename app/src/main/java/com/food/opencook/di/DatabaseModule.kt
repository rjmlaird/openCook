package com.food.opencook.di

import android.content.Context
import androidx.room.Room
import com.food.opencook.data.local.OpenCookDatabase
import com.food.opencook.data.local.RoomTransactor
import com.food.opencook.data.local.Transactor
import com.food.opencook.data.local.dao.JobDao
import com.food.opencook.data.local.dao.MealDayDao
import com.food.opencook.data.local.dao.MealPlanDao
import com.food.opencook.data.local.dao.MessageDao
import com.food.opencook.data.local.dao.PantryDao
import com.food.opencook.data.local.dao.ProductCacheDao
import com.food.opencook.data.local.dao.RecipeDao
import com.food.opencook.data.local.dao.RecipeLikeDao
import com.food.opencook.data.local.dao.ShoppingDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTransactor(impl: RoomTransactor): Transactor = impl

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): OpenCookDatabase =
        // v1 is the released baseline schema (see OpenCookDatabase): no migrations yet.
        // Real migrations must be added here as the schema evolves past v1.
        Room.databaseBuilder(context, OpenCookDatabase::class.java, "opencook.db")
            .build()

    @Provides
    fun provideRecipeDao(database: OpenCookDatabase): RecipeDao = database.recipeDao()

    @Provides
    fun provideJobDao(database: OpenCookDatabase): JobDao = database.jobDao()

    @Provides
    fun provideMessageDao(database: OpenCookDatabase): MessageDao = database.messageDao()

    @Provides
    fun provideShoppingDao(database: OpenCookDatabase): ShoppingDao = database.shoppingDao()

    @Provides
    fun providePantryDao(database: OpenCookDatabase): PantryDao = database.pantryDao()

    @Provides
    fun provideMealPlanDao(database: OpenCookDatabase): MealPlanDao = database.mealPlanDao()

    @Provides
    fun provideMealDayDao(database: OpenCookDatabase): MealDayDao = database.mealDayDao()

    @Provides
    fun provideRecipeLikeDao(database: OpenCookDatabase): RecipeLikeDao = database.recipeLikeDao()

    @Provides
    fun provideProductCacheDao(database: OpenCookDatabase): ProductCacheDao = database.productCacheDao()
}
