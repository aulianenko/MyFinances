package dev.aulianenko.myfinances.ui.screens.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.aulianenko.myfinances.data.entity.Account
import dev.aulianenko.myfinances.data.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddAccountUiState(
    val accountName: String = "",
    val selectedCurrency: String = "USD",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

class AddAccountViewModel(
    private val repository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddAccountUiState())
    val uiState: StateFlow<AddAccountUiState> = _uiState.asStateFlow()

    fun onAccountNameChange(name: String) {
        _uiState.update { it.copy(accountName = name, errorMessage = null) }
    }

    fun onCurrencyChange(currency: String) {
        _uiState.update { it.copy(selectedCurrency = currency, errorMessage = null) }
    }

    fun saveAccount() {
        val currentState = _uiState.value

        if (currentState.accountName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Account name cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val account = Account(
                    name = currentState.accountName.trim(),
                    currency = currentState.selectedCurrency
                )
                repository.insertAccount(account)
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to save account: ${e.message}"
                    )
                }
            }
        }
    }
}
