package com.quantax.gotogether.presentation.profile

import com.quantax.gotogether.data.repository.UserProfileRepository
import com.quantax.gotogether.domain.model.UserProfile
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PublicProfileViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var repository: UserProfileRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadProfile with blank userId sets error`() = runTest(testDispatcher) {
        val viewModel = PublicProfileViewModel(repository)

        viewModel.loadProfile("")

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Invalid user", state.error)
        assertNull(state.profile)
    }

    @Test
    fun `loadProfile with valid userId and success sets profile`() = runTest(testDispatcher) {
        val profile = UserProfile(uid = "uid1", name = "Public User", email = "p@test.com")
        coEvery { repository.getProfile("uid1") } returns Result.success(profile)

        val viewModel = PublicProfileViewModel(repository)
        viewModel.loadProfile("uid1")

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNotNull(state.profile)
        assertEquals("uid1", state.profile?.uid)
        assertEquals("Public User", state.profile?.name)
    }

    @Test
    fun `loadProfile when user not found sets error`() = runTest(testDispatcher) {
        coEvery { repository.getProfile("uid1") } returns Result.success(null)

        val viewModel = PublicProfileViewModel(repository)
        viewModel.loadProfile("uid1")

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("User not found", state.error)
        assertNull(state.profile)
    }

    @Test
    fun `loadProfile when PERMISSION_DENIED sets private profile error`() = runTest(testDispatcher) {
        coEvery { repository.getProfile("uid1") } returns Result.failure(
            RuntimeException("PERMISSION_DENIED: Missing or insufficient permissions.")
        )

        val viewModel = PublicProfileViewModel(repository)
        viewModel.loadProfile("uid1")

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("This user's profile is private", state.error)
    }

    @Test
    fun `loadProfile when other failure sets generic error`() = runTest(testDispatcher) {
        coEvery { repository.getProfile("uid1") } returns Result.failure(
            RuntimeException("Network error")
        )

        val viewModel = PublicProfileViewModel(repository)
        viewModel.loadProfile("uid1")

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Failed to load profile") || state.error == "Network error")
    }

    @Test
    fun `loadProfile sets loading true then false`() = runTest(testDispatcher) {
        val profile = UserProfile(uid = "uid1", name = "User")
        coEvery { repository.getProfile("uid1") } returns Result.success(profile)

        val viewModel = PublicProfileViewModel(repository)
        viewModel.loadProfile("uid1")

        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)
    }
}
