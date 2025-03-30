package com.example.anoop.lono.di

import com.example.anoop.lono.data.repository.*
import com.example.anoop.lono.data.repository.impl.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()

    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        storage: FirebaseStorage,
        messaging: FirebaseMessaging
    ): UserRepository = FirebaseUserRepository(auth, firestore, storage, messaging)

    @Provides
    @Singleton
    fun provideMemoryRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        storage: FirebaseStorage
    ): MemoryRepository = FirebaseMemoryRepository(firestore, auth, storage)

    @Provides
    @Singleton
    fun provideChatRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        storage: FirebaseStorage
    ): ChatRepository = FirebaseChatRepository(firestore, auth, storage)

    @Provides
    @Singleton
    fun provideChallengeRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): ChallengeRepository = FirebaseChallengeRepository(firestore, auth)
} 