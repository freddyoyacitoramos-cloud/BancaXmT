package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [TransactionEntity::class], version = 1, exportSchema = false)
abstract class BancaCubaDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: BancaCubaDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): BancaCubaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BancaCubaDatabase::class.java,
                    "banca_cuba_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        populateInitialTransactions(database.transactionDao())
                    }
                }
            }
        }

        suspend fun populateInitialTransactions(dao: TransactionDao) {
            val initial = listOf(
                TransactionEntity(
                    id = "tx_bonus_001",
                    referenceNumber = "BONUS-REG-CUB-2026",
                    title = "Asignación Monto de Registro",
                    description = "Bono bancario oficial de bienvenida por registro en Banca Cuba",
                    amount = 890000.0,
                    currency = "CUP",
                    type = "REGISTRATION_BONUS",
                    timestamp = System.currentTimeMillis() - 7200000,
                    status = "COMPLETED",
                    recipient = "ALEXIS RAMOS VALDÉS",
                    destinationBank = "Banco Metropolitano",
                    fee = 0.0
                ),
                TransactionEntity(
                    id = "tx_init_002",
                    referenceNumber = "TRANSF-REC-89301",
                    title = "Depósito en Divisas MLC",
                    description = "Transferencia interbancaria internacional familiar",
                    amount = 1250.00,
                    currency = "MLC",
                    type = "TRANSFER_RECEIVED",
                    timestamp = System.currentTimeMillis() - 86400000,
                    status = "COMPLETED",
                    recipient = "ALEXIS RAMOS VALDÉS",
                    destinationBank = "BANDEC",
                    fee = 0.0
                ),
                TransactionEntity(
                    id = "tx_init_003",
                    referenceNumber = "PAGO-UNE-91823",
                    title = "Pago Electricidad UNE",
                    description = "Factura de consumo eléctrico (Ruta: 0812903) con bonificación",
                    amount = 645.00,
                    currency = "CUP",
                    type = "SERVICE_PAYMENT",
                    timestamp = System.currentTimeMillis() - 172800000,
                    status = "COMPLETED",
                    recipient = "Unión Eléctrica de Cuba",
                    destinationBank = "Banco Metropolitano",
                    serviceCategory = "Servicios Básicos",
                    fee = 0.0
                ),
                TransactionEntity(
                    id = "tx_init_004",
                    referenceNumber = "TRANSF-CU-774102",
                    title = "Transferencia a Carlos Pérez",
                    description = "Pago de insumos agropecuarios",
                    amount = 45000.0,
                    currency = "CUP",
                    type = "TRANSFER_SENT",
                    timestamp = System.currentTimeMillis() - 259200000,
                    status = "COMPLETED",
                    recipient = "Carlos Pérez",
                    destinationCard = "9200 1198 3401 2281",
                    destinationBank = "BANDEC",
                    fee = 0.0
                )
            )
            dao.insertAll(initial)
        }
    }
}
