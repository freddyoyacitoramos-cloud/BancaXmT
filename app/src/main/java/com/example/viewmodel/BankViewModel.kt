package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BancaCubaDatabase
import com.example.model.BankAccount
import com.example.model.BankBranch
import com.example.model.BankNotification
import com.example.model.CurrencyType
import com.example.model.PaymentService
import com.example.model.SupportMessage
import com.example.model.Transaction
import com.example.repository.BankRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppScreen {
    DASHBOARD,
    TRANSFERS,
    SERVICES,
    HISTORY,
    SUPPORT,
    ACCOUNTS_CARD
}

data class TransferFormState(
    val fromCurrency: CurrencyType = CurrencyType.CUP,
    val destinationBank: String = "Banco Metropolitano",
    val destinationCard: String = "",
    val recipientName: String = "",
    val amountText: String = "",
    val concept: String = "",
    val error: String? = null
)

class BankViewModel(
    application: Application,
    customRepository: BankRepository? = null
) : AndroidViewModel(application) {

    private val repository: BankRepository = customRepository ?: BankRepository(
        transactionDao = BancaCubaDatabase.getDatabase(application, viewModelScope).transactionDao(),
        scope = viewModelScope
    )

    val cupAccount: StateFlow<BankAccount> = repository.cupAccount
    val mlcAccount: StateFlow<BankAccount> = repository.mlcAccount
    val transactions: StateFlow<List<Transaction>> = repository.transactions
    val notifications: StateFlow<List<BankNotification>> = repository.notifications
    val availableServices: List<PaymentService> = repository.availableServices
    val bankBranches: List<BankBranch> = repository.bankBranches
    val supportMessages: StateFlow<List<SupportMessage>> = repository.supportMessages
    val activeAlertModal: StateFlow<String?> = repository.activeAlertModal

    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _isAppUnlocked = MutableStateFlow(true)
    val isAppUnlocked: StateFlow<Boolean> = _isAppUnlocked.asStateFlow()

    private val _isBiometricScanning = MutableStateFlow(false)
    val isBiometricScanning: StateFlow<Boolean> = _isBiometricScanning.asStateFlow()

    private val _selectedAccountCurrency = MutableStateFlow(CurrencyType.CUP)
    val selectedAccountCurrency: StateFlow<CurrencyType> = _selectedAccountCurrency.asStateFlow()

    private val _transferForm = MutableStateFlow(TransferFormState())
    val transferForm: StateFlow<TransferFormState> = _transferForm.asStateFlow()

    private val _selectedTransactionForReceipt = MutableStateFlow<Transaction?>(null)
    val selectedTransactionForReceipt: StateFlow<Transaction?> = _selectedTransactionForReceipt.asStateFlow()

    private val _selectedServiceForPayment = MutableStateFlow<PaymentService?>(null)
    val selectedServiceForPayment: StateFlow<PaymentService?> = _selectedServiceForPayment.asStateFlow()

    private val _serviceIdentifierInput = MutableStateFlow("")
    val serviceIdentifierInput: StateFlow<String> = _serviceIdentifierInput.asStateFlow()

    private val _serviceAmountInput = MutableStateFlow("")
    val serviceAmountInput: StateFlow<String> = _serviceAmountInput.asStateFlow()

    private val _transactionFilter = MutableStateFlow("TODOS")
    val transactionFilter: StateFlow<String> = _transactionFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showRegistrationBonusBanner = MutableStateFlow(true)
    val showRegistrationBonusBanner: StateFlow<Boolean> = _showRegistrationBonusBanner.asStateFlow()

    private val _showNotificationsSheet = MutableStateFlow(false)
    val showNotificationsSheet: StateFlow<Boolean> = _showNotificationsSheet.asStateFlow()

    private val _showBiometricAuthDialogForTransfer = MutableStateFlow(false)
    val showBiometricAuthDialogForTransfer: StateFlow<Boolean> = _showBiometricAuthDialogForTransfer.asStateFlow()

    val unreadNotificationsCount: StateFlow<Int> = notifications.combine(_showNotificationsSheet) { list, _ ->
        list.count { !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun selectAccountCurrency(currency: CurrencyType) {
        _selectedAccountCurrency.value = currency
    }

    fun lockApp() {
        _isAppUnlocked.value = false
    }

    fun unlockAppWithBiometrics() {
        _isBiometricScanning.value = true
        viewModelScope.launch {
            kotlinx.coroutines.delay(650)
            _isBiometricScanning.value = false
            _isAppUnlocked.value = true
        }
    }

    fun updateTransferForm(transform: TransferFormState.() -> TransferFormState) {
        _transferForm.update { it.transform() }
    }

    fun initiateTransferConfirmation() {
        val form = _transferForm.value
        val amount = form.amountText.toDoubleOrNull() ?: 0.0

        if (form.destinationCard.replace(" ", "").length < 16) {
            _transferForm.update { it.copy(error = "El número de tarjeta cubana debe tener 16 dígitos.") }
            return
        }
        if (amount <= 0.0) {
            _transferForm.update { it.copy(error = "Ingrese un monto mayor a 0.") }
            return
        }
        val account = if (form.fromCurrency == CurrencyType.CUP) cupAccount.value else mlcAccount.value
        if (amount > account.balance) {
            _transferForm.update { it.copy(error = "Saldo insuficiente en su cuenta ${form.fromCurrency.code}.") }
            return
        }

        _transferForm.update { it.copy(error = null) }
        _showBiometricAuthDialogForTransfer.value = true
    }

    fun confirmTransferWithBiometric() {
        _showBiometricAuthDialogForTransfer.value = false
        val form = _transferForm.value
        val amount = form.amountText.toDoubleOrNull() ?: 0.0

        val success = repository.sendTransfer(
            fromCurrency = form.fromCurrency,
            destinationCard = form.destinationCard,
            destinationBank = form.destinationBank,
            recipientName = form.recipientName.ifBlank { "Destinatario Bancario" },
            amount = amount,
            concept = form.concept
        )

        if (success) {
            _transferForm.value = TransferFormState(fromCurrency = form.fromCurrency)
        }
    }

    fun cancelTransferAuth() {
        _showBiometricAuthDialogForTransfer.value = false
    }

    fun selectService(service: PaymentService?) {
        _selectedServiceForPayment.value = service
        _serviceIdentifierInput.value = ""
        _serviceAmountInput.value = ""
    }

    fun updateServiceInputs(identifier: String, amount: String) {
        _serviceIdentifierInput.value = identifier
        _serviceAmountInput.value = amount
    }

    fun executeServicePayment(currency: CurrencyType) {
        val service = _selectedServiceForPayment.value ?: return
        val amount = _serviceAmountInput.value.toDoubleOrNull() ?: return
        val id = _serviceIdentifierInput.value.ifBlank { "000000" }

        val success = repository.payService(service, currency, id, amount)
        if (success) {
            _selectedServiceForPayment.value = null
            _serviceIdentifierInput.value = ""
            _serviceAmountInput.value = ""
        }
    }

    fun setTransactionFilter(filter: String) {
        _transactionFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun viewReceipt(transaction: Transaction?) {
        _selectedTransactionForReceipt.value = transaction
    }

    fun dismissAlertModal() {
        repository.clearActiveAlertModal()
    }

    fun dismissRegistrationBanner() {
        _showRegistrationBonusBanner.value = false
    }

    fun toggleNotificationsSheet(show: Boolean) {
        _showNotificationsSheet.value = show
        if (show) {
            repository.markNotificationsAsRead()
        }
    }

    fun toggleCardFreeze(currency: CurrencyType) {
        repository.toggleCardFreeze(currency)
    }

    fun sendSupportQuery(message: String) {
        if (message.isNotBlank()) {
            repository.addSupportMessage(message)
        }
    }
}
