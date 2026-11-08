package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.CurrencyType
import com.example.ui.components.BankBottomNavBar
import com.example.ui.components.BankTopAppBar
import com.example.ui.components.BiometricAuthDialog
import com.example.ui.components.ConnectionDelayAlertModal
import com.example.ui.components.NotificationsSheet
import com.example.ui.components.OfficialReceiptDialog
import com.example.ui.screens.AccountsCardScreen
import com.example.ui.screens.BiometricLockScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ServicesScreen
import com.example.ui.screens.SupportScreen
import com.example.ui.screens.TransfersScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.BankViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BankViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BancaCubaApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun BancaCubaApp(
    viewModel: BankViewModel,
    modifier: Modifier = Modifier
) {
    val isAppUnlocked by viewModel.isAppUnlocked.collectAsStateWithLifecycle()
    val isBiometricScanning by viewModel.isBiometricScanning.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()

    val cupAccount by viewModel.cupAccount.collectAsStateWithLifecycle()
    val mlcAccount by viewModel.mlcAccount.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()
    val supportMessages by viewModel.supportMessages.collectAsStateWithLifecycle()

    val activeAlertModal by viewModel.activeAlertModal.collectAsStateWithLifecycle()
    val showBiometricAuthForTransfer by viewModel.showBiometricAuthDialogForTransfer.collectAsStateWithLifecycle()
    val selectedReceipt by viewModel.selectedTransactionForReceipt.collectAsStateWithLifecycle()
    val showNotificationsSheet by viewModel.showNotificationsSheet.collectAsStateWithLifecycle()
    val showBonusBanner by viewModel.showRegistrationBonusBanner.collectAsStateWithLifecycle()

    val transferForm by viewModel.transferForm.collectAsStateWithLifecycle()
    val selectedService by viewModel.selectedServiceForPayment.collectAsStateWithLifecycle()
    val serviceIdentifier by viewModel.serviceIdentifierInput.collectAsStateWithLifecycle()
    val serviceAmount by viewModel.serviceAmountInput.collectAsStateWithLifecycle()
    val transactionFilter by viewModel.transactionFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    // Handle system back navigation
    if (currentScreen != AppScreen.DASHBOARD && isAppUnlocked) {
        BackHandler {
            viewModel.navigateTo(AppScreen.DASHBOARD)
        }
    }

    if (!isAppUnlocked) {
        BiometricLockScreen(
            isScanning = isBiometricScanning,
            onUnlockBiometric = { viewModel.unlockAppWithBiometrics() }
        )
    } else {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                BankTopAppBar(
                    unreadCount = unreadCount,
                    onNotificationsClick = { viewModel.toggleNotificationsSheet(true) },
                    onLockAppClick = { viewModel.lockApp() }
                )
            },
            bottomBar = {
                BankBottomNavBar(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    AppScreen.DASHBOARD -> {
                        DashboardScreen(
                            cupAccount = cupAccount,
                            mlcAccount = mlcAccount,
                            recentTransactions = transactions,
                            showRegistrationBonusBanner = showBonusBanner,
                            onDismissBonusBanner = { viewModel.dismissRegistrationBanner() },
                            onNavigate = { viewModel.navigateTo(it) },
                            onSelectTransaction = { viewModel.viewReceipt(it) }
                        )
                    }

                    AppScreen.TRANSFERS -> {
                        TransfersScreen(
                            cupAccount = cupAccount,
                            mlcAccount = mlcAccount,
                            formState = transferForm,
                            onFormChange = { viewModel.updateTransferForm(it) },
                            onSubmitTransfer = { viewModel.initiateTransferConfirmation() }
                        )
                    }

                    AppScreen.SERVICES -> {
                        ServicesScreen(
                            services = viewModel.availableServices,
                            cupAccount = cupAccount,
                            mlcAccount = mlcAccount,
                            selectedService = selectedService,
                            identifierInput = serviceIdentifier,
                            amountInput = serviceAmount,
                            onSelectService = { viewModel.selectService(it) },
                            onInputsChange = { id, amt -> viewModel.updateServiceInputs(id, amt) },
                            onExecutePayment = { currency -> viewModel.executeServicePayment(currency) }
                        )
                    }

                    AppScreen.ACCOUNTS_CARD -> {
                        AccountsCardScreen(
                            cupAccount = cupAccount,
                            mlcAccount = mlcAccount,
                            onToggleFreeze = { viewModel.toggleCardFreeze(it) }
                        )
                    }

                    AppScreen.HISTORY -> {
                        HistoryScreen(
                            transactions = transactions,
                            selectedFilter = transactionFilter,
                            searchQuery = searchQuery,
                            onFilterChange = { viewModel.setTransactionFilter(it) },
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            onSelectTransaction = { viewModel.viewReceipt(it) }
                        )
                    }

                    AppScreen.SUPPORT -> {
                        SupportScreen(
                            supportMessages = supportMessages,
                            branches = viewModel.bankBranches,
                            onSendMessage = { viewModel.sendSupportQuery(it) }
                        )
                    }
                }
            }

            // REQUIRED ALERT MODAL: Triggered on transfer / connection notice
            if (activeAlertModal != null) {
                ConnectionDelayAlertModal(
                    message = activeAlertModal!!,
                    onDismiss = { viewModel.dismissAlertModal() }
                )
            }

            // Biometric Auth Modal for transfer confirmation
            if (showBiometricAuthForTransfer) {
                BiometricAuthDialog(
                    amount = transferForm.amountText.toDoubleOrNull() ?: 0.0,
                    currency = transferForm.fromCurrency,
                    recipient = transferForm.recipientName.ifBlank { transferForm.destinationCard },
                    onConfirm = { viewModel.confirmTransferWithBiometric() },
                    onCancel = { viewModel.cancelTransferAuth() }
                )
            }

            // Official Voucher / Receipt Dialog
            if (selectedReceipt != null) {
                OfficialReceiptDialog(
                    transaction = selectedReceipt!!,
                    onDismiss = { viewModel.viewReceipt(null) }
                )
            }

            // Real-time Push Notifications Sheet
            if (showNotificationsSheet) {
                NotificationsSheet(
                    notifications = notifications,
                    onDismiss = { viewModel.toggleNotificationsSheet(false) }
                )
            }
        }
    }
}
