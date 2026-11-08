package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.CurrencyType
import com.example.model.Transaction
import com.example.model.TransactionStatus
import com.example.model.TransactionType

@Entity(tableName = "bank_transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val referenceNumber: String,
    val title: String,
    val description: String,
    val amount: Double,
    val currency: String, // "CUP", "MLC"
    val type: String,     // "TRANSFER_SENT", "TRANSFER_RECEIVED", "REGISTRATION_BONUS", "SERVICE_PAYMENT"
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "COMPLETED", // "COMPLETED", "PENDING_24H", "PROCESSING"
    val recipient: String = "",
    val destinationCard: String = "",
    val destinationBank: String = "Banco Metropolitano",
    val serviceCategory: String? = null,
    val fee: Double = 0.0,
    val alertNotice: String? = null
) {
    fun toDomainModel(): Transaction {
        val curr = if (currency.equals("MLC", ignoreCase = true)) CurrencyType.MLC else CurrencyType.CUP
        val txType = when (type) {
            "TRANSFER_SENT" -> TransactionType.TRANSFER_SENT
            "TRANSFER_RECEIVED" -> TransactionType.TRANSFER_RECEIVED
            "REGISTRATION_BONUS" -> TransactionType.REGISTRATION_BONUS
            "SERVICE_PAYMENT" -> TransactionType.SERVICE_PAYMENT
            else -> TransactionType.LOCAL_GATEWAY_PAYMENT
        }
        val txStatus = when (status) {
            "PENDING_24H" -> TransactionStatus.PENDING_24H
            "PROCESSING" -> TransactionStatus.PROCESSING
            else -> TransactionStatus.COMPLETED
        }

        return Transaction(
            id = id,
            referenceNumber = referenceNumber,
            title = title,
            description = description,
            amount = amount,
            currency = curr,
            type = txType,
            timestamp = timestamp,
            status = txStatus,
            recipient = recipient,
            destinationCard = destinationCard,
            destinationBank = destinationBank,
            serviceCategory = serviceCategory,
            fee = fee,
            alertNotice = alertNotice
        )
    }

    companion object {
        fun fromDomainModel(domain: Transaction): TransactionEntity {
            return TransactionEntity(
                id = domain.id,
                referenceNumber = domain.referenceNumber,
                title = domain.title,
                description = domain.description,
                amount = domain.amount,
                currency = domain.currency.code,
                type = domain.type.name,
                timestamp = domain.timestamp,
                status = domain.status.name,
                recipient = domain.recipient,
                destinationCard = domain.destinationCard,
                destinationBank = domain.destinationBank,
                serviceCategory = domain.serviceCategory,
                fee = domain.fee,
                alertNotice = domain.alertNotice
            )
        }
    }
}
