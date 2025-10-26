package dev.aulianenko.myfinances.ui.screens.accountvalue

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.aulianenko.myfinances.data.entity.Account
import dev.aulianenko.myfinances.data.entity.AccountValue
import dev.aulianenko.myfinances.data.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddAccountValueUiState(
    val account: Account? = null,
    val value: String = "",
    val note: String = "",
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AddAccountValueViewModel @Inject constructor(
    private val repository: AccountRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val accountId: String = checkNotNull(savedStateHandle["accountId"])

    private val _uiState = MutableStateFlow(AddAccountValueUiState())
    val uiState: StateFlow<AddAccountValueUiState> = _uiState.asStateFlow()

    init {
        loadAccount()
    }

    private fun loadAccount() {
        viewModelScope.launch {
            repository.getAccountById(accountId).collect { account ->
                _uiState.update {
                    it.copy(
                        account = account,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onValueChange(value: String) {
        _uiState.update { it.copy(value = value, errorMessage = null) }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun saveAccountValue() {
        val currentState = _uiState.value

        if (currentState.value.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Value cannot be empty") }
            return
        }

        val valueDouble = currentState.value.toDoubleOrNull()
        if (valueDouble == null) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid number") }
            return
        }

        if (valueDouble < 0) {
            _uiState.update { it.copy(errorMessage = "Value cannot be negative") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val accountValue = AccountValue(
                    accountId = accountId,
                    value = valueDouble,
                    note = currentState.note.trim().ifBlank { null }
                )
                repository.insertAccountValue(accountValue)
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to save value: ${e.message}"
                    )
                }
            }
        }
    }
}
