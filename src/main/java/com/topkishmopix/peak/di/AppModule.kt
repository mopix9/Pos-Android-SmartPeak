package com.topkishmopix.peak.di

import android.content.Context
import android.content.SharedPreferences
import com.topkishmopix.peak.main.view.view.LoadingFragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideLoadingFragment(): LoadingFragment = LoadingFragment()


    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext app: Context): SharedPreferences = app.getSharedPreferences("DATA", Context.MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideApplicationContext(@ApplicationContext app: Context) = app


}