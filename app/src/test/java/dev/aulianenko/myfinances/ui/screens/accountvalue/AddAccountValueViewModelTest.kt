package dev.aulianenko.myfinances.ui.screens.accountvalue

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import dev.aulianenko.myfinances.data.entity.Account
import dev.aulianenko.myfinances.data.entity.AccountValue
import dev.aulianenko.myfinances.data.repository.AccountRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddAccountValueViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: AccountRepository
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: AddAccountValueViewModel

    private val testAccountId = "test-account-id"
    private val testAccount = Account(
        id = testAccountId,
        name = "Test Account",
        currency = "USD",
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        savedStateHandle = SavedStateHandle(mapOf("accountId" to testAccountId))

        // Default mock behavior - return test account
        every { repository.getAccountById(testAccountId) } returns flowOf(testAccount)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be loading`() = runTest {
        viewModel = AddAccountValueViewModel(repository, savedStateHandle)

        viewModel.uiState.test {
            val initialState = awaitItem()
            assertTrue(initialState.isLoading)
            assertNull(initialState.account)
            assertEquals("", initialState.value)
            assertEquals("", initialState.note)
            assertFalse(initialState.isSaved)
            assertNull(initialState.errorMessage)
        }
    }

    @Test
    fun `should load account successfully`() = runTest {
        viewModel = AddAccountValueViewModel(repository, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertNotNull(state.account)
            assertEquals(testAccount, state.account)
        }
    }

    @Test
    fun `onValueChange should update value and clear error`() = runTest {
        viewModel = AddAccountValueViewModel(repository, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onValueChange("100.50")

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("100.50", state.value)
            assertNull(state.errorMessage)
        }
    }

    @Test
    fun `onNoteChange should update note`() = runTest {
        viewModel = AddAccountValueViewModel(repository, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onNoteChange("Test note")

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Test note", state.note)
        }
    }

    @Test
    fun `onTimestampChange should update timestamp`() = runTest {
        viewModel = AddAccountValueViewModel(repository, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        val newTimestamp = 1234567890L
        viewModel.onTimestampChange(newTimestamp)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(newTimestamp, state.timestamp)
        }
    }

    @Test
    fun `saveAccountValue should show error when value is blank`() = runTest {
        viewModel = AddAccountValueViewModel(repository, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onValueChange("")
        viewModel.saveAccountValue()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Value cannot be empty", state.errorMessage)
            assertFalse(state.isSaved)
        }
    }

    @Test
    fun `saveAccountValue should show error when value is not a number`() = runTest {
        viewModel = AddAccountValueViewModel(repository, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onValueChange("invalid")
        viewModel.saveAccountValue()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Please enter a valid number", state.errorMessage)
            assertFalse(state.isSaved)
        }
    }

    @Test
    fun `saveAccountValue should show error when value is negative`() = runTest {
        viewModel = AddAccountValueViewModel(repository, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onValueChange("-100")
        viewModel.saveAccountValue()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Value cannot be negative", state.errorMessage)
            assertFalse(state.isSaved)
        }
    }

    @Test
    fun `saveAccountValue should save successfully with valid data`() = runTest {
        coEvery { repository.insertAccountValue(any()) } returns Unit

        viewModel = AddAccountValueViewModel(repository, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onValueChange("250.75")
        viewModel.onNoteChange("Monthly update")
        viewModel.saveAccountValue()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isSaved)
            assertFalse(state.isLoading)
            assertNull(state.errorMessage)
        }

        coVerify {
            repository.insertAccountValue(
                match { accountValue ->
                    accountValue.accountId == testAccountId &&
                    accountValue.value == 250.75 &&
                    accountValue.note == "Monthly update"
                }
            )
        }
    }

    @Test
    fun `saveAccountValue should trim and save note as null if blank`() = runTest {
        coEvery { repository.insertAccountValue(any()) } returns Unit

        viewModel = AddAccountValueViewModel(repository, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onValueChange("100.0")
        viewModel.onNoteChange("   ")  // Blank note
        viewModel.saveAccountValue()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            repository.insertAccountValue(
                match { accountValue ->
                    accountValue.note == null
                }
            )
        }
    }

    @Test
    fun `saveAccountValue should use custom timestamp`() = runTest {
        coEvery { repository.insertAccountValue(any()) } returns Unit

        viewModel = AddAccountValueViewModel(repository, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        val customTimestamp = 9876543210L
        viewModel.onValueChange("100.0")
        viewModel.onTimestampChange(customTimestamp)
        viewModel.saveAccountValue()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            repository.insertAccountValue(
                match { accountValue ->
                    accountValue.timestamp == customTimestamp
                }
            )
        }
    }

    @Test
    fun `saveAccountValue should handle repository errors`() = runTest {
        val errorMessage = "Database error"
        coEvery { repository.insertAccountValue(any()) } throws Exception(errorMessage)

        viewModel = AddAccountValueViewModel(repository, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onValueChange("100.0")
        viewModel.saveAccountValue()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isSaved)
            assertFalse(state.isLoading)
            assertTrue(state.errorMessage?.contains("Failed to save value") == true)
            assertTrue(state.errorMessage?.contains(errorMessage) == true)
        }
    }

    @Test
    fun `saveAccountValue should clear loading state after completion`() = runTest {
        coEvery { repository.insertAccountValue(any()) } returns Unit

        viewModel = AddAccountValueViewModel(repository, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onValueChange("100.0")
        viewModel.saveAccountValue()
        testDispatcher.scheduler.advanceUntilIdle()

        // After save completes, loading should be false
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertTrue(state.isSaved)
        }
    }

    @Test
    fun `saveAccountValue should accept zero value`() = runTest {
        coEvery { repository.insertAccountValue(any()) } returns Unit

        viewModel = AddAccountValueViewModel(repository, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onValueChange("0")
        viewModel.saveAccountValue()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isSaved)
            assertNull(state.errorMessage)
        }

        coVerify {
            repository.insertAccountValue(
                match { accountValue ->
                    accountValue.value == 0.0
                }
            )
        }
    }

    @Test
    fun `saveAccountValue should handle decimal values correctly`() = runTest {
        coEvery { repository.insertAccountValue(any()) } returns Unit

        viewModel = AddAccountValueViewModel(repository, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onValueChange("123.456")
        viewModel.saveAccountValue()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            repository.insertAccountValue(
                match { accountValue ->
                    accountValue.value == 123.456
                }
            )
        }
    }
}
