package com.quantax.gotogether.presentation.profile

import com.quantax.gotogether.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileStateTest {

    @Test
    fun `ProfileState has sensible defaults`() {
        val state = ProfileState()
        assertFalse(state.isLoading)
        assertNull(state.profile)
        assertNull(state.error)
        assertFalse(state.isSaving)
        assertFalse(state.saveSuccess)
    }

    @Test
    fun `ProfileEvent LoadProfile is object`() {
        val event = ProfileEvent.LoadProfile
        assertEquals(ProfileEvent.LoadProfile, event)
    }

    @Test
    fun `ProfileEvent SaveProfile carries profile`() {
        val profile = UserProfile(uid = "u1", name = "Test")
        val event = ProfileEvent.SaveProfile(profile)
        assertEquals(profile, event.profile)
    }

    @Test
    fun `ProfileEvent ResetSaveSuccess is object`() {
        assertEquals(ProfileEvent.ResetSaveSuccess, ProfileEvent.ResetSaveSuccess)
    }

    @Test
    fun `ProfileEvent SignOut is object`() {
        assertEquals(ProfileEvent.SignOut, ProfileEvent.SignOut)
    }

    @Test
    fun `ProfileEvent ClearError is object`() {
        assertEquals(ProfileEvent.ClearError, ProfileEvent.ClearError)
    }

    @Test
    fun `PublicProfileState has sensible defaults`() {
        val state = PublicProfileState()
        assertFalse(state.isLoading)
        assertNull(state.profile)
        assertNull(state.error)
    }
}
