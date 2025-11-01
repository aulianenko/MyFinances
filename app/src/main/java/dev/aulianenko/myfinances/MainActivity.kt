package dev.aulianenko.myfinances

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.aulianenko.myfinances.data.repository.ThemeMode
import dev.aulianenko.myfinances.data.repository.UserPreferencesRepository
import dev.aulianenko.myfinances.security.BiometricAuthManager
import dev.aulianenko.myfinances.security.PasswordAuthManager
import dev.aulianenko.myfinances.ui.components.PasswordAuthDialog
import dev.aulianenko.myfinances.ui.navigation.BottomNavItem
import dev.aulianenko.myfinances.ui.navigation.NavGraph
import dev.aulianenko.myfinances.ui.theme.MyFinancesTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var biometricAuthManager: BiometricAuthManager

    private var isAppLocked by mutableStateOf(false)
    private var shouldLockOnResume = false
    private var showPasswordDialog by mutableStateOf(false)
    private var passwordError by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Add lifecycle observer to detect app going to background
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                // App is going to background
                lifecycleScope.launch {
                    val appLockEnabled = userPreferencesRepository.appLockEnabled.first()
                    if (appLockEnabled) {
                        shouldLockOnResume = true
                    }
                }
            }

            override fun onResume(owner: LifecycleOwner) {
                // App is coming to foreground
                if (shouldLockOnResume) {
                    shouldLockOnResume = false
                    checkAndShowAuthentication()
                }
            }
        })

        // Check if app lock should be shown on first launch
        lifecycleScope.launch {
            val appLockEnabled = userPreferencesRepository.appLockEnabled.first()
            if (appLockEnabled) {
                isAppLocked = true
                checkAndShowAuthentication()
            }
        }

        setContent {
            val themeMode by userPreferencesRepository.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val systemInDarkTheme = isSystemInDarkTheme()

            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> systemInDarkTheme
            }

            MyFinancesTheme(darkTheme = darkTheme) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    // Determine if bottom bar should be shown
                    val showBottomBar = currentRoute in BottomNavItem.items.map { it.route }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            if (showBottomBar) {
                                NavigationBar {
                                    BottomNavItem.items.forEach { item ->
                                        NavigationBarItem(
                                            icon = { Icon(item.icon, contentDescription = item.title) },
                                            label = { Text(item.title) },
                                            selected = currentRoute == item.route,
                                            onClick = {
                                                if (currentRoute != item.route) {
                                                    navController.navigate(item.route) {
                                                        popUpTo(navController.graph.startDestinationId) {
                                                            saveState = true
                                                        }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavGraph(
                            navController = navController,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }

                    // Lock screen overlay
                    if (isAppLocked) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "App Locked",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Authenticate to continue",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Password authentication dialog
                    if (showPasswordDialog) {
                        PasswordAuthDialog(
                            title = "Unlock MyFinances",
                            message = "Enter your app lock password",
                            onPasswordEntered = { password ->
                                onPasswordEntered(password)
                            },
                            onDismiss = null, // Non-dismissible - must enter password
                            errorMessage = passwordError
                        )
                    }
                }
            }
        }
    }

    private fun checkAndShowAuthentication() {
        lifecycleScope.launch {
            val biometricEnabled = userPreferencesRepository.biometricEnabled.first()
            val hasPassword = userPreferencesRepository.hasAppLockPassword()

            if (biometricEnabled) {
                // Show biometric authentication
                biometricAuthManager.authenticate(
                    activity = this@MainActivity,
                    title = "Unlock MyFinances",
                    subtitle = "Authenticate to access your financial data",
                    onSuccess = {
                        isAppLocked = false
                        showPasswordDialog = false
                    },
                    onError = { errorCode, errorMessage ->
                        // If biometric fails and password is available, show password dialog
                        if (hasPassword) {
                            showPasswordDialog = true
                        } else {
                            // Keep app locked, user can try biometric again
                            isAppLocked = true
                        }
                    },
                    onFailed = {
                        // Authentication failed - show password dialog if available
                        if (hasPassword) {
                            showPasswordDialog = true
                        } else {
                            isAppLocked = true
                        }
                    }
                )
            } else if (hasPassword) {
                // No biometric but has password - show password dialog
                showPasswordDialog = true
            } else {
                // No authentication method set up - this shouldn't happen in normal flow
                // but unlock anyway to prevent being permanently locked out
                isAppLocked = false
            }
        }
    }

    private fun onPasswordEntered(password: String) {
        lifecycleScope.launch {
            val storedHash = userPreferencesRepository.getAppLockPasswordHash()

            if (storedHash != null && PasswordAuthManager.verifyPassword(password, storedHash)) {
                // Password correct - unlock app
                isAppLocked = false
                showPasswordDialog = false
                passwordError = null
            } else {
                // Password incorrect - show error
                passwordError = "Incorrect password"
            }
        }
    }
}