package com.example.model

enum class CurrencyType(val code: String, val symbol: String, val fullName: String) {
    CUP("CUP", "₱", "Moneda Nacional (CUP)"),
    MLC("MLC", "US$", "Moneda Libremente Convertible (MLC)")
}

enum class TransactionType {
    TRANSFER_SENT,
    TRANSFER_RECEIVED,
    REGISTRATION_BONUS,
    SERVICE_PAYMENT,
    LOCAL_GATEWAY_PAYMENT
}

enum class TransactionStatus {
    COMPLETED,
    PENDING_24H,
    PROCESSING
}

data class BankAccount(
    val id: String,
    val accountNumber: String,
    val bankName: String,
    val currency: CurrencyType,
    val balance: Double,
    val cardHolder: String = "ALEXIS RAMOS VALDÉS",
    val expiry: String = "11/29",
    val cvv: String = "482",
    val isFrozen: Boolean = false,
    val dailyLimit: Double = 150000.0,
    val cardNumberMasked: String = if (accountNumber.length >= 16) {
        "${accountNumber.take(4)} •••• •••• ${accountNumber.takeLast(4)}"
    } else accountNumber
)

data class Transaction(
    val id: String,
    val referenceNumber: String,
    val title: String,
    val description: String,
    val amount: Double,
    val currency: CurrencyType,
    val type: TransactionType,
    val timestamp: Long = System.currentTimeMillis(),
    val status: TransactionStatus = TransactionStatus.COMPLETED,
    val recipient: String = "",
    val destinationCard: String = "",
    val destinationBank: String = "Banco Metropolitano",
    val serviceCategory: String? = null,
    val fee: Double = 0.0,
    val alertNotice: String? = null
)

data class PaymentService(
    val id: String,
    val name: String,
    val entity: String,
    val category: String,
    val description: String,
    val discountPercent: Int,
    val identifierLabel: String,
    val placeholder: String,
    val currency: CurrencyType = CurrencyType.CUP
)

data class BankNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isUrgent: Boolean = false,
    val type: String = "ALERT"
)

data class SupportMessage(
    val id: String,
    val sender: String,
    val message: String,
    val isFromBank: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class BankBranch(
    val name: String,
    val address: String,
    val province: String,
    val phone: String,
    val hasAtm: Boolean = true,
    val cashAvailability: String = "CUP y MLC disponibles"
)
