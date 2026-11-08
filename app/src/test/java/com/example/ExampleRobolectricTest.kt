package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.BancaCubaDatabase
import com.example.data.local.TransactionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  private lateinit var db: BancaCubaDatabase

  @Before
  fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, BancaCubaDatabase::class.java)
      .allowMainThreadQueries()
      .build()
  }

  @After
  fun closeDb() {
    db.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Banca Cuba", appName)
  }

  @Test
  fun testRoomInsertAndFilterByStatus() = runBlocking {
    val dao = db.transactionDao()

    val pendingTx = TransactionEntity(
      id = "tx_pending_01",
      referenceNumber = "TRANSF-CU-123456",
      title = "Transferencia a Juan",
      description = "Transferencia interbancaria",
      amount = 15000.0,
      currency = "CUP",
      type = "TRANSFER_SENT",
      status = "PENDING_24H",
      alertNotice = "Por problemas de conexión, el depósito se reflejará en la cuenta del usuario en un plazo máximo de veinticuatro horas hábiles."
    )

    val completedTx = TransactionEntity(
      id = "tx_completed_01",
      referenceNumber = "BONUS-REG-890K",
      title = "Bono Registro",
      description = "Monto asignado por registro",
      amount = 890000.0,
      currency = "CUP",
      type = "REGISTRATION_BONUS",
      status = "COMPLETED"
    )

    dao.insertTransaction(pendingTx)
    dao.insertTransaction(completedTx)

    val all = dao.getAllTransactions().first()
    assertEquals(2, all.size)

    val pendingList = dao.getTransactionsByStatus("PENDING_24H").first()
    assertEquals(1, pendingList.size)
    assertEquals("tx_pending_01", pendingList[0].id)
    assertEquals("PENDING_24H", pendingList[0].status)
    assertTrue(pendingList[0].alertNotice?.contains("veinticuatro horas") == true)
  }
}
