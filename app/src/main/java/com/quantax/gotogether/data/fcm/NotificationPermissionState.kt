package com.quantax.gotogether.data.fcm

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks whether the runtime POST_NOTIFICATIONS permission is currently missing.
 * Set by anything that has a Context (the displayer when it has to drop a notification,
 * the permission launcher callback in MainActivity); observed by the UI to surface
 * a re-enable affordance.
 */
@Singleton
class NotificationPermissionState @Inject constructor() {

    private val _denied = MutableStateFlow(false)
    val denied: StateFlow<Boolean> = _denied.asStateFlow()

    fun setDenied(denied: Boolean) {
        _denied.value = denied
    }
}
