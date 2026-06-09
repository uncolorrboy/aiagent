package ru.sapozhnikov.aiagent.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.sapozhnikov.aiagent.domain.interactor.AiAgentInteractor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
internal class ChatViewModel @Inject constructor(
    private val aiAgentInteractor: AiAgentInteractor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatScreenUiState())
    val uiState = _uiState.asStateFlow()

    fun onMessageSent(message: String) {
        if (message.isBlank() || _uiState.value.isLoading) return

        val userMessage = ChatMessage(
            text = message.trim(),
            time = currentTime(),
            messageOwner = MessageOwner.USER,
        )

        _uiState.update { state ->
            state.copy(
                items = listOf(userMessage) + state.items,
                isLoading = true,
                sendButtonState = SendButtonState.IN_PROCESS,
            )
        }

        viewModelScope.launch {
            aiAgentInteractor.sendMessage(message)
                .onSuccess { response ->
                    val aiMessage = ChatMessage(
                        text = response,
                        time = currentTime(),
                        messageOwner = MessageOwner.AI,
                    )
                    _uiState.update { state ->
                        state.copy(
                            items = listOf(aiMessage) + state.items,
                            isLoading = false,
                            sendButtonState = SendButtonState.DEFAULT,
                        )
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            sendButtonState = SendButtonState.DEFAULT,
                        )
                    }
                }
        }
    }

    private fun currentTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }
}
