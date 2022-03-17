package com.ags.annada.userposts.di

import android.content.Context
import androidx.room.Room
import com.ags.annada.userposts.datasource.*
import com.ags.annada.userposts.datasource.api.ApiService
import com.ags.annada.userposts.datasource.local.CommentsLocalDataSource
import com.ags.annada.userposts.datasource.local.PostsLocalDataSource
import com.ags.annada.userposts.datasource.remote.CommentsRemoteDataSource
import com.ags.annada.userposts.datasource.remote.PostsRemoteDataSource
import com.ags.annada.userposts.datasource.room.PostDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class RemotePostsDataSource

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class LocalPostsDataSource

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class RemoteCommentsDataSource

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class LocalCommentsDataSource

//    @Singleton
//    @RemotePostsDataSource
//    @Provides
//    fun providePostsRemoteDataSource(): PostsDataSource {
//        return PostsRemoteDataSource
//    }

    @Singleton
    @RemotePostsDataSource
    @Provides
    fun providePostsRemoteDataSource(
        remoteService: ApiService,
        ioDispatcher: CoroutineDispatcher
    ): PostsDataSource {
        return PostsRemoteDataSource(
            remoteService, ioDispatcher
        )
    }

    @Singleton
    @LocalPostsDataSource
    @Provides
    fun providePostsLocalDataSource(
        database: PostDatabase,
        ioDispatcher: CoroutineDispatcher
    ): PostsDataSource {
        return PostsLocalDataSource(
            database.postDao(), ioDispatcher
        )
    }

    @Singleton
    @RemoteCommentsDataSource
    @Provides
    fun provideCommentsRemoteDataSource(
        remoteService: ApiService,
        ioDispatcher: CoroutineDispatcher
    ): CommentsDataSource {
        return CommentsRemoteDataSource(
            remoteService, ioDispatcher
        )
    }

    @Singleton
    @LocalCommentsDataSource
    @Provides
    fun provideCommentsLocalDataSource(
        database: PostDatabase,
        ioDispatcher: CoroutineDispatcher
    ): CommentsDataSource {
        return CommentsLocalDataSource(
            database.commentDao(), ioDispatcher
        )
    }

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): PostDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            PostDatabase::class.java,
            "Posts.db"
        ).build()
    }

    @Singleton
    @Provides
    fun provideIoDispatcher() = Dispatchers.IO
}

/**
 * The binding for PostsRepository is on its own module so that we can replace it easily in tests.
 */
@Module
@InstallIn(SingletonComponent::class)
object PostsRepositoryModule {

    @Singleton
    @Provides
    fun providePostsRepository(
        @AppModule.RemotePostsDataSource remotePostsDataSource: PostsDataSource,
        @AppModule.LocalPostsDataSource localPostsDataSource: PostsDataSource,
        ioDispatcher: CoroutineDispatcher
    ): PostsRepository {
        return DefaultPostsRepository(
            remotePostsDataSource, localPostsDataSource, ioDispatcher
        )
    }
}

/**
 * The binding for CommentsRepository is on its own module so that we can replace it easily in tests.
 */
@Module
@InstallIn(SingletonComponent::class)
object CommentsRepositoryModule {

    @Singleton
    @Provides
    fun provideCommentsRepository(
        @AppModule.RemoteCommentsDataSource remoteCommentsDataSource: CommentsDataSource,
        @AppModule.LocalCommentsDataSource localCommentsDataSource: CommentsDataSource,
        ioDispatcher: CoroutineDispatcher
    ): CommentsRepository {
        return DefaultCommentsRepository(
            remoteCommentsDataSource, localCommentsDataSource, ioDispatcher
        )
    }
}
