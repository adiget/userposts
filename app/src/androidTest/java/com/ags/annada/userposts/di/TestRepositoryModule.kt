package com.ags.annada.userposts.di

import com.ags.annada.userposts.data.source.FakeCommentsRepository
import com.ags.annada.userposts.data.source.FakePostsRepository
import com.ags.annada.userposts.datasource.CommentsRepository
import com.ags.annada.userposts.datasource.PostsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * PostsRepository binding to use in tests.
 *
 * Hilt will inject a [FakePostsRepository] instead of a [DefaultPostsRepository].
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [PostsRepositoryModule::class]
)
abstract class TestPostsRepositoryModule {
    @Singleton
    @Binds
    abstract fun bindRepository(repo: FakePostsRepository): PostsRepository
}

/**
 * CommentsRepository binding to use in tests.
 *
 * Hilt will inject a [FakeCommentsRepository] instead of a [DefaultCommentsRepository].
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [CommentsRepositoryModule::class]
)
abstract class FakeCommentsRepositoryModule {
    @Singleton
    @Binds
    abstract fun bindRepository(repo: FakeCommentsRepository): CommentsRepository
}
