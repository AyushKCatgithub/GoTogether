package com.quantax.gotogether.domain.usecase.chat

import com.quantax.gotogether.data.repository.ChatRepository
import com.quantax.gotogether.domain.model.Chatroom
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChatroomsUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    operator fun invoke(userId: String): Flow<List<Chatroom>> {
        if (userId.isBlank()) {
            return kotlinx.coroutines.flow.flowOf(emptyList())
        }
        return repository.getUserChatrooms(userId)
    }
}
