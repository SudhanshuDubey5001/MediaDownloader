package com.sudhanshu.mp3_downloader.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Dagger Hilt dependency injection to initialize library and get instance**/

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
//    @Provides
//    @Singleton
//    fun provideDownloaderInstance(
//        @ApplicationContext context: Context
//    ): Downloader {
//        return DownloadManagerClass(context)
//    }
}
