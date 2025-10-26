package dev.aulianenko.myfinances.ui.screens.account

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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountDetailUiState(
    val account: Account? = null,
    val accountValues: List<AccountValue> = emptyList(),
    val latestValue: AccountValue? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class AccountDetailViewModel @Inject constructor(
    private val repository: AccountRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val accountId: String = checkNotNull(savedStateHandle["accountId"])

    private val _uiState = MutableStateFlow(AccountDetailUiState())
    val uiState: StateFlow<AccountDetailUiState> = _uiState.asStateFlow()

    init {
        loadAccountDetails()
    }

    private fun loadAccountDetails() {
        viewModelScope.launch {
            combine(
                repository.getAccountById(accountId),
                repository.getAccountValues(accountId),
                repository.getLatestAccountValue(accountId)
            ) { account, values, latestValue ->
                AccountDetailUiState(
                    account = account,
                    accountValues = values,
                    latestValue = latestValue,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun deleteAccountValue(accountValue: AccountValue) {
        viewModelScope.launch {
            repository.deleteAccountValue(accountValue)
        }
    }
}
