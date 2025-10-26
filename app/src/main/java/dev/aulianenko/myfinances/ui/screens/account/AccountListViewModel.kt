package dev.aulianenko.myfinances.ui.screens.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.aulianenko.myfinances.data.entity.Account
import dev.aulianenko.myfinances.data.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AccountListUiState(
    val accounts: List<Account> = emptyList(),
    val isLoading: Boolean = true
)

class AccountListViewModel(
    private val repository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountListUiState())
    val uiState: StateFlow<AccountListUiState> = _uiState.asStateFlow()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            repository.getAllAccounts().collect { accounts ->
                _uiState.value = AccountListUiState(
                    accounts = accounts,
                    isLoading = false
                )
            }
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            repository.deleteAccount(account)
        }
    }
}
