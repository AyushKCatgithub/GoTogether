package com.quantax.gotogether.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.quantax.gotogether.auth.AuthManager
import com.quantax.gotogether.data.repository.BookmarkRepository
import com.quantax.gotogether.data.repository.ChatRepository
import com.quantax.gotogether.data.repository.JoinRequestRepository
import com.quantax.gotogether.data.repository.NotificationRepository
import com.quantax.gotogether.data.repository.StorageService
import com.quantax.gotogether.data.repository.TripRepository
import com.quantax.gotogether.data.repository.UserProfileRepository
import com.quantax.gotogether.utils.ConnectivityObserver
import com.quantax.gotogether.utils.NetworkConnectivityObserver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideTripRepository(firestore: FirebaseFirestore): TripRepository {
        return TripRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideBookmarkRepository(firestore: FirebaseFirestore): BookmarkRepository {
        return BookmarkRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        firestore: FirebaseFirestore,
        database: FirebaseDatabase
    ): ChatRepository {
        return ChatRepository(firestore, database)
    }

    @Provides
    @Singleton
    fun provideJoinRequestRepository(firestore: FirebaseFirestore): JoinRequestRepository {
        return JoinRequestRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(firestore: FirebaseFirestore): NotificationRepository {
        return NotificationRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideStorageService(storage: FirebaseStorage, auth: FirebaseAuth): StorageService {
        return StorageService(storage, auth)
    }

    @Provides
    @Singleton
    fun provideUserProfileRepository(firestore: FirebaseFirestore): UserProfileRepository {
        return UserProfileRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideAuthManager(auth: FirebaseAuth, profileRepo: UserProfileRepository): AuthManager {
        return AuthManager(auth, profileRepo)
    }

    @Provides
    @Singleton
    fun provideConnectivityObserver(@ApplicationContext context: Context): ConnectivityObserver {
        return NetworkConnectivityObserver(context)
    }
}
