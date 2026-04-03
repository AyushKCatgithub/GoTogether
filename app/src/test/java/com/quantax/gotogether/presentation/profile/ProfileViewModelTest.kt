package com.quantax.gotogether.presentation.profile

import com.google.firebase.auth.FirebaseUser
import com.quantax.gotogether.auth.AuthManager
import com.quantax.gotogether.data.repository.StorageService
import com.quantax.gotogether.data.repository.UserProfileRepository
import com.quantax.gotogether.domain.model.UserProfile
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var repository: UserProfileRepository
    private lateinit var storageService: StorageService
    private lateinit var authManager: AuthManager

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        storageService = mockk(relaxed = true)
        authManager = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init when user is null sets error and not loading`() = runTest(testDispatcher) {
        every { authManager.currentUser } returns null

        val viewModel = ProfileViewModel(repository, storageService, authManager)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("User not logged in", state.error)
        assertNull(state.profile)
    }

    @Test
    fun `init when user is not null loads profile from flow`() = runTest(testDispatcher) {
        val firebaseUser = mockk<FirebaseUser>()
        every { firebaseUser.uid } returns "uid1"
        every { firebaseUser.email } returns "user@test.com"
        every { authManager.currentUser } returns firebaseUser

        val profile = UserProfile(uid = "uid1", name = "Test", email = "user@test.com")
        every { repository.getProfileFlow("uid1") } returns flowOf(profile)

        val viewModel = ProfileViewModel(repository, storageService, authManager)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNotNull(state.profile)
        assertEquals("uid1", state.profile?.uid)
        assertEquals("Test", state.profile?.name)
    }

    @Test
    fun `init when flow emits null uses empty profile with uid and email`() =
        runTest(testDispatcher) {
            val firebaseUser = mockk<FirebaseUser>()
            every { firebaseUser.uid } returns "uid1"
            every { firebaseUser.email } returns "user@test.com"
            every { authManager.currentUser } returns firebaseUser
            every { repository.getProfileFlow("uid1") } returns flowOf(null)

            val viewModel = ProfileViewModel(repository, storageService, authManager)

            val state = viewModel.state.value
            assertFalse(state.isLoading)
            assertEquals("uid1", state.profile?.uid)
            assertEquals("user@test.com", state.profile?.email)
        }

    @Test
    fun `LoadProfile when user is null sets error`() = runTest(testDispatcher) {
        every { authManager.currentUser } returns null
        val viewModel = ProfileViewModel(repository, storageService, authManager)

        viewModel.onEvent(ProfileEvent.LoadProfile)

        assertEquals("User not logged in", viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `LoadProfile when user exists fetches profile`() = runTest(testDispatcher) {
        val firebaseUser = mockk<FirebaseUser>()
        every { firebaseUser.uid } returns "uid1"
        every { firebaseUser.email } returns "e@e.com"
        every { authManager.currentUser } returns firebaseUser
        every { repository.getProfileFlow("uid1") } returns flowOf(null)

        val viewModel = ProfileViewModel(repository, storageService, authManager)
        val profile = UserProfile(uid = "uid1", name = "Loaded")
        coEvery { repository.getProfile("uid1") } returns Result.success(profile)

        viewModel.onEvent(ProfileEvent.LoadProfile)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Loaded", state.profile?.name)
    }

    @Test
    fun `ClearError clears error in state`() = runTest(testDispatcher) {
        every { authManager.currentUser } returns null
        val viewModel = ProfileViewModel(repository, storageService, authManager)
        assertNotNull(viewModel.state.value.error)

        viewModel.onEvent(ProfileEvent.ClearError)

        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `ResetSaveSuccess clears saveSuccess`() = runTest(testDispatcher) {
        val firebaseUser = mockk<FirebaseUser>()
        every { firebaseUser.uid } returns "uid1"
        every { firebaseUser.email } returns "e@e.com"
        every { authManager.currentUser } returns firebaseUser
        every { repository.getProfileFlow("uid1") } returns flowOf(
            UserProfile(uid = "uid1", email = "e@e.com")
        )
        every { storageService.isLocalUri(any()) } returns false
        coEvery { repository.saveProfile(any(), any()) } returns Result.success(Unit)

        val viewModel = ProfileViewModel(repository, storageService, authManager)
        viewModel.onEvent(ProfileEvent.SaveProfile(UserProfile(uid = "uid1", email = "e@e.com")))
        assertTrue(viewModel.state.value.saveSuccess)

        viewModel.onEvent(ProfileEvent.ResetSaveSuccess)

        assertFalse(viewModel.state.value.saveSuccess)
    }

    @Test
    fun `SignOut calls authManager signOut`() = runTest(testDispatcher) {
        every { authManager.currentUser } returns null
        val viewModel = ProfileViewModel(repository, storageService, authManager)

        viewModel.onEvent(ProfileEvent.SignOut)

        verify(exactly = 1) { authManager.signOut() }
    }

    @Test
    fun `SaveProfile when user is null sets error`() = runTest(testDispatcher) {
        every { authManager.currentUser } returns null
        val viewModel = ProfileViewModel(repository, storageService, authManager)
        val profile = UserProfile(uid = "uid1", name = "Test")

        viewModel.onEvent(ProfileEvent.SaveProfile(profile))

        assertEquals("User not logged in", viewModel.state.value.error)
        assertFalse(viewModel.state.value.isSaving)
    }

    @Test
    fun `SaveProfile when repository succeeds sets saveSuccess`() = runTest(testDispatcher) {
        val firebaseUser = mockk<FirebaseUser>()
        every { firebaseUser.uid } returns "uid1"
        every { firebaseUser.email } returns "e@e.com"
        every { authManager.currentUser } returns firebaseUser
        every { repository.getProfileFlow("uid1") } returns flowOf(
            UserProfile(uid = "uid1", email = "e@e.com")
        )
        every { storageService.isLocalUri(any()) } returns false
        coEvery { repository.saveProfile(any(), any()) } returns Result.success(Unit)

        val viewModel = ProfileViewModel(repository, storageService, authManager)
        val profile = UserProfile(uid = "uid1", name = "Saved", email = "e@e.com")

        viewModel.onEvent(ProfileEvent.SaveProfile(profile))

        assertFalse(viewModel.state.value.isSaving)
        assertTrue(viewModel.state.value.saveSuccess)
        assertEquals("Saved", viewModel.state.value.profile?.name)
    }

    @Test
    fun `SaveProfile when repository fails sets error`() = runTest(testDispatcher) {
        val firebaseUser = mockk<FirebaseUser>()
        every { firebaseUser.uid } returns "uid1"
        every { firebaseUser.email } returns "e@e.com"
        every { authManager.currentUser } returns firebaseUser
        every { repository.getProfileFlow("uid1") } returns flowOf(
            UserProfile(uid = "uid1", email = "e@e.com")
        )
        every { storageService.isLocalUri(any()) } returns false
        coEvery {
            repository.saveProfile(
                any(),
                any()
            )
        } returns Result.failure(RuntimeException("Save failed"))

        val viewModel = ProfileViewModel(repository, storageService, authManager)
        viewModel.onEvent(ProfileEvent.SaveProfile(UserProfile(uid = "uid1", email = "e@e.com")))

        assertFalse(viewModel.state.value.isSaving)
        assertFalse(viewModel.state.value.saveSuccess)
        assertNotNull(viewModel.state.value.error)
        assertTrue(viewModel.state.value.error!!.contains("Save failed"))
    }
}
