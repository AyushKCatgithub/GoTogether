package com.quantax.gotogether.data.fcm

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks which chatroom (if any) the user is currently viewing. Used to suppress
 * system notifications for the active conversation — the message is already
 * visible in-app, so a banner would be redundant noise.
 *
 * Also reflects whether the app is in the foreground. A chatroom is only "active"
 * when (a) the user is on its screen *and* (b) the app process is foregrounded;
 * once the user backgrounds the app, even the most recently-opened chatroom should
 * still produce notifications because the user can no longer see the messages.
 */
@Singleton
class ActiveChatroomTracker @Inject constructor() : DefaultLifecycleObserver {

    @Volatile
    private var activeChatroomId: String? = null

    @Volatile
    private var isAppForegrounded: Boolean = false

    init {
        // ProcessLifecycleOwner observers must be added on the main thread.
        MainScope().launch(Dispatchers.Main.immediate) {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this@ActiveChatroomTracker)
        }
    }

    fun enter(chatroomId: String) {
        activeChatroomId = chatroomId
    }

    fun exit(chatroomId: String) {
        if (activeChatroomId == chatroomId) activeChatroomId = null
    }

    fun isActive(chatroomId: String): Boolean =
        isAppForegrounded && activeChatroomId == chatroomId

    override fun onStart(owner: LifecycleOwner) {
        isAppForegrounded = true
    }

    override fun onStop(owner: LifecycleOwner) {
        isAppForegrounded = false
    }
}
