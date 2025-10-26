package dev.aulianenko.myfinances.ui.screens.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.aulianenko.myfinances.data.entity.Account
import dev.aulianenko.myfinances.data.entity.AccountValue
import dev.aulianenko.myfinances.data.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class AccountWithValue(
    val account: Account,
    val currentValue: AccountValue?
)

data class AccountListUiState(
    val accountsWithValues: List<AccountWithValue> = emptyList(),
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
                val accountsWithValuesFlows = accounts.map { account ->
                    combine(
                        kotlinx.coroutines.flow.flowOf(account),
                        repository.getLatestAccountValue(account.id)
                    ) { acc, value ->
                        AccountWithValue(acc, value)
                    }
                }

                if (accountsWithValuesFlows.isEmpty()) {
                    _uiState.value = AccountListUiState(
                        accountsWithValues = emptyList(),
                        isLoading = false
                    )
                } else {
                    combine(accountsWithValuesFlows) { it.toList() }.collect { accountsWithValues ->
                        _uiState.value = AccountListUiState(
                            accountsWithValues = accountsWithValues,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            repository.deleteAccount(account)
        }
    }
}
