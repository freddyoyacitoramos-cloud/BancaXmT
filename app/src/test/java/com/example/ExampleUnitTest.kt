package com.example

import com.example.model.CurrencyType
import com.example.model.TransactionStatus
import com.example.repository.BankRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testInitialRegistrationBonusAmountIs890ThousandCup() {
    val repository = BankRepository()
    assertEquals(890000.0, repository.cupAccount.value.balance, 0.01)
    assertEquals(CurrencyType.CUP, repository.cupAccount.value.currency)
  }

  @Test
  fun testTransferTriggersRequired24HourDelayAlert() {
    val repository = BankRepository()
    val initialBalance = repository.cupAccount.value.balance

    val success = repository.sendTransfer(
      fromCurrency = CurrencyType.CUP,
      destinationCard = "9202 0000 1111 2222",
      destinationBank = "Banco Metropolitano",
      recipientName = "Destinatario Prueba",
      amount = 5000.0,
      concept = "Prueba de transferencia"
    )

    assertTrue(success)
    assertEquals(initialBalance - 5000.0, repository.cupAccount.value.balance, 0.01)

    // Verify required alert prompt
    val expectedAlert = "Por problemas de conexión, el depósito se reflejará en la cuenta del usuario en un plazo máximo de veinticuatro horas hábiles."
    assertEquals(expectedAlert, repository.activeAlertModal.value)

    // Verify transaction status is pending 24h
    val latestTx = repository.transactions.value.first()
    assertEquals(TransactionStatus.PENDING_24H, latestTx.status)
    assertEquals(expectedAlert, latestTx.alertNotice)
  }
}
