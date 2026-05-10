package com.termux.di

import android.content.Context
import androidx.room.Room
import com.termux.data.KorexDatabase
import com.termux.data.SettingsDataStore
import com.termux.data.history.CommandHistoryDao
import com.termux.data.history.CommandHistoryRepositoryImpl
import com.termux.data.session.SessionDao
import com.termux.data.session.SessionRepositoryImpl
import com.termux.data.snippet.SnippetDao
import com.termux.data.snippet.SnippetRepositoryImpl
import com.termux.data.theme.ThemeDao
import com.termux.data.theme.ThemeRepositoryImpl
import com.termux.domain.CommandHistoryRepository
import com.termux.domain.SessionRepository
import com.termux.domain.SnippetRepository
import com.termux.domain.ThemeRepository
import dagger.Binds
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
    fun provideDatabase(@ApplicationContext context: Context): KorexDatabase =
        Room.databaseBuilder(context, KorexDatabase::class.java, "korex.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideSessionDao(db: KorexDatabase): SessionDao = db.sessionDao()
    @Provides fun provideSnippetDao(db: KorexDatabase): SnippetDao = db.snippetDao()
    @Provides fun provideCommandHistoryDao(db: KorexDatabase): CommandHistoryDao = db.commandHistoryDao()
    @Provides fun provideThemeDao(db: KorexDatabase): ThemeDao = db.themeDao()

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore =
        SettingsDataStore(context)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

    @Binds @Singleton
    abstract fun bindSnippetRepository(impl: SnippetRepositoryImpl): SnippetRepository

    @Binds @Singleton
    abstract fun bindCommandHistoryRepository(impl: CommandHistoryRepositoryImpl): CommandHistoryRepository

    @Binds @Singleton
    abstract fun bindThemeRepository(impl: ThemeRepositoryImpl): ThemeRepository
}