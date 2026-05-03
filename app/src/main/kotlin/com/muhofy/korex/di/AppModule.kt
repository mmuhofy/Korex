package com.muhofy.korex.di

import android.content.Context
import androidx.room.Room
import com.muhofy.korex.data.KorexDatabase
import com.muhofy.korex.data.session.SessionDao
import com.muhofy.korex.data.session.SessionRepositoryImpl
import com.muhofy.korex.domain.SessionRepository
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
    fun provideSessionDao(db: KorexDatabase): SessionDao =
        db.sessionDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository
}