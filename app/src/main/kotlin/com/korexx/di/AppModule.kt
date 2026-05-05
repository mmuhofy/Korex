package com.korexx.di

import android.content.Context
import androidx.room.Room
import com.korexx.data.KorexDatabase
import com.korexx.data.history.CommandHistoryDao
import com.korexx.data.history.CommandHistoryRepositoryImpl
import com.korexx.data.session.SessionDao
import com.korexx.data.session.SessionRepositoryImpl
import com.korexx.data.snippet.SnippetDao
import com.korexx.data.snippet.SnippetRepositoryImpl
import com.korexx.domain.CommandHistoryRepository
import com.korexx.domain.SessionRepository
import com.korexx.domain.SnippetRepository
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

    @Provides
    fun provideSessionDao(db: KorexDatabase): SessionDao = db.sessionDao()

    @Provides
    fun provideSnippetDao(db: KorexDatabase): SnippetDao = db.snippetDao()

    @Provides
    fun provideCommandHistoryDao(db: KorexDatabase): CommandHistoryDao = db.commandHistoryDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

    @Binds
    @Singleton
    abstract fun bindSnippetRepository(impl: SnippetRepositoryImpl): SnippetRepository

    @Binds
    @Singleton
    abstract fun bindCommandHistoryRepository(impl: CommandHistoryRepositoryImpl): CommandHistoryRepository
}