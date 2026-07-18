package com.wealthwise.app.di

import android.content.Context
import com.wealthwise.app.data.local.WealthWiseDatabase
import com.wealthwise.app.data.local.dao.TransactionDao
import com.wealthwise.app.security.DatabaseKeyProvider
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
    fun provideKeyProvider(@ApplicationContext context: Context): DatabaseKeyProvider =
        DatabaseKeyProvider(context)

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        keyProvider: DatabaseKeyProvider
    ): WealthWiseDatabase = WealthWiseDatabase.getInstance(context, keyProvider)

    @Provides
    fun provideTransactionDao(database: WealthWiseDatabase): TransactionDao = database.transactionDao()
}
