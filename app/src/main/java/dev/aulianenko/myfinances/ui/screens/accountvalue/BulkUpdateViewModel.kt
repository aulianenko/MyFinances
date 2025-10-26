package dev.aulianenko.myfinances.ui.screens.accountvalue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.aulianenko.myfinances.data.entity.Account
import dev.aulianenko.myfinances.data.entity.AccountValue
import dev.aulianenko.myfinances.data.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AccountValueInput(
    val account: Account,
    val value: String = "",
    val hasValue: Boolean = false
)

data class BulkUpdateUiState(
    val accountInputs: List<AccountValueInput> = emptyList(),
    val note: String = "",
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
    val validationErrors: Map<String, String> = emptyMap()
)

class BulkUpdateViewModel(
    private val repository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BulkUpdateUiState())
    val uiState: StateFlow<BulkUpdateUiState> = _uiState.asStateFlow()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            repository.getAllAccounts().collect { accounts ->
                _uiState.update {
                    it.copy(
                        accountInputs = accounts.map { account ->
                            AccountValueInput(account = account)
                        },
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onValueChange(accountId: String, value: String) {
        _uiState.update { state ->
            state.copy(
                accountInputs = state.accountInputs.map { input ->
                    if (input.account.id == accountId) {
                        input.copy(value = value, hasValue = value.isNotBlank())
                    } else {
                        input
                    }
                },
                validationErrors = state.validationErrors - accountId,
                errorMessage = null
            )
        }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun saveAllValues() {
        val currentState = _uiState.value
        val inputsWithValues = currentState.accountInputs.filter { it.hasValue }

        if (inputsWithValues.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter at least one account value") }
            return
        }

        // Validate all inputs
        val errors = mutableMapOf<String, String>()
        inputsWithValues.forEach { input ->
            val valueDouble = input.value.toDoubleOrNull()
            when {
                valueDouble == null -> {
                    errors[input.account.id] = "Invalid number"
                }
                valueDouble < 0 -> {
                    errors[input.account.id] = "Cannot be negative"
                }
            }
        }

        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(validationErrors = errors) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val timestamp = System.currentTimeMillis()
                val note = currentState.note.trim().ifBlank { null }

                inputsWithValues.forEach { input ->
                    val accountValue = AccountValue(
                        accountId = input.account.id,
                        value = input.value.toDouble(),
                        timestamp = timestamp,
                        note = note
                    )
                    repository.insertAccountValue(accountValue)
                }

                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to save values: ${e.message}"
                    )
                }
            }
        }
    }
}
