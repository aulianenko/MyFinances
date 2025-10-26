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

data class EditAccountUiState(
    val account: Account? = null,
    val accountName: String = "",
    val selectedCurrency: String = "USD",
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

class EditAccountViewModel(
    private val repository: AccountRepository,
    private val accountId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditAccountUiState())
    val uiState: StateFlow<EditAccountUiState> = _uiState.asStateFlow()

    init {
        loadAccount()
    }

    private fun loadAccount() {
        viewModelScope.launch {
            repository.getAccountById(accountId).collect { account ->
                if (account != null) {
                    _uiState.update {
                        it.copy(
                            account = account,
                            accountName = account.name,
                            selectedCurrency = account.currency,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Account not found"
                        )
                    }
                }
            }
        }
    }

    fun onAccountNameChange(name: String) {
        _uiState.update { it.copy(accountName = name, errorMessage = null) }
    }

    fun onCurrencyChange(currency: String) {
        _uiState.update { it.copy(selectedCurrency = currency, errorMessage = null) }
    }

    fun saveAccount() {
        val currentState = _uiState.value
        val account = currentState.account ?: return

        if (currentState.accountName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Account name cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val updatedAccount = account.copy(
                    name = currentState.accountName.trim(),
                    currency = currentState.selectedCurrency,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateAccount(updatedAccount)
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to update account: ${e.message}"
                    )
                }
            }
        }
    }
}
