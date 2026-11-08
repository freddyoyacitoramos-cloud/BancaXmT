package com.example.repository

import com.example.data.local.TransactionDao
import com.example.data.local.TransactionEntity
import com.example.model.BankAccount
import com.example.model.BankBranch
import com.example.model.BankNotification
import com.example.model.CurrencyType
import com.example.model.PaymentService
import com.example.model.SupportMessage
import com.example.model.Transaction
import com.example.model.TransactionStatus
import com.example.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class BankRepository(
    private val transactionDao: TransactionDao? = null,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    companion object {
        const val INITIAL_REGISTRATION_BONUS_CUP = 890000.0
        const val CONNECTION_DELAY_ALERT_MSG =
            "Por problemas de conexión, el depósito se reflejará en la cuenta del usuario en un plazo máximo de veinticuatro horas hábiles."
    }

    private val _cupAccount = MutableStateFlow(
        BankAccount(
            id = "acc_cup_01",
            accountNumber = "9202120048194108",
            bankName = "Banco Metropolitano",
            currency = CurrencyType.CUP,
            balance = INITIAL_REGISTRATION_BONUS_CUP,
            cardHolder = "ALEXIS RAMOS VALDÉS",
            expiry = "11/29",
            cvv = "482",
            dailyLimit = 250000.0
        )
    )
    val cupAccount: StateFlow<BankAccount> = _cupAccount.asStateFlow()

    private val _mlcAccount = MutableStateFlow(
        BankAccount(
            id = "acc_mlc_01",
            accountNumber = "9225883204917821",
            bankName = "Banco de Crédito y Comercio (BANDEC)",
            currency = CurrencyType.MLC,
            balance = 1250.00,
            cardHolder = "ALEXIS RAMOS VALDÉS",
            expiry = "08/28",
            cvv = "913",
            dailyLimit = 5000.0
        )
    )
    val mlcAccount: StateFlow<BankAccount> = _mlcAccount.asStateFlow()

    private val _inMemoryTransactions = MutableStateFlow<List<Transaction>>(
        listOf(
            Transaction(
                id = "tx_bonus_001",
                referenceNumber = "BONUS-REG-CUB-2026",
                title = "Asignación Monto de Registro",
                description = "Bono bancario oficial de bienvenida por registro en Banca Cuba",
                amount = INITIAL_REGISTRATION_BONUS_CUP,
                currency = CurrencyType.CUP,
                type = TransactionType.REGISTRATION_BONUS,
                timestamp = System.currentTimeMillis() - 3600000,
                status = TransactionStatus.COMPLETED,
                recipient = "ALEXIS RAMOS VALDÉS",
                destinationBank = "Banco Metropolitano",
                fee = 0.0
            ),
            Transaction(
                id = "tx_init_002",
                referenceNumber = "TRANSF-REC-89301",
                title = "Depósito en Divisas MLC",
                description = "Transferencia interbancaria internacional familiar",
                amount = 1250.00,
                currency = CurrencyType.MLC,
                type = TransactionType.TRANSFER_RECEIVED,
                timestamp = System.currentTimeMillis() - 86400000,
                status = TransactionStatus.COMPLETED,
                recipient = "ALEXIS RAMOS VALDÉS",
                destinationBank = "BANDEC",
                fee = 0.0
            ),
            Transaction(
                id = "tx_init_003",
                referenceNumber = "PAGO-UNE-91823",
                title = "Pago Electricidad UNE",
                description = "Factura de consumo eléctrico (Ruta: 0812903) con bonificación",
                amount = 645.00,
                currency = CurrencyType.CUP,
                type = TransactionType.SERVICE_PAYMENT,
                timestamp = System.currentTimeMillis() - 172800000,
                status = TransactionStatus.COMPLETED,
                recipient = "Unión Eléctrica de Cuba",
                destinationBank = "Banco Metropolitano",
                serviceCategory = "Servicios Básicos",
                fee = 0.0
            ),
            Transaction(
                id = "tx_init_004",
                referenceNumber = "TRANSF-CU-774102",
                title = "Transferencia a Carlos Pérez",
                description = "Pago de insumos agropecuarios",
                amount = 45000.0,
                currency = CurrencyType.CUP,
                type = TransactionType.TRANSFER_SENT,
                timestamp = System.currentTimeMillis() - 259200000,
                status = TransactionStatus.COMPLETED,
                recipient = "Carlos Pérez",
                destinationCard = "9200 1198 3401 2281",
                destinationBank = "BANDEC",
                fee = 0.0
            )
        )
    )

    val transactions: StateFlow<List<Transaction>> = if (transactionDao != null) {
        transactionDao.getAllTransactions()
            .map { list ->
                if (list.isEmpty()) {
                    // Prepopulate if empty
                    scope.launch {
                        _inMemoryTransactions.value.forEach {
                            transactionDao.insertTransaction(TransactionEntity.fromDomainModel(it))
                        }
                    }
                    _inMemoryTransactions.value
                } else {
                    list.map { it.toDomainModel() }
                }
            }
            .stateIn(scope, SharingStarted.Eagerly, _inMemoryTransactions.value)
    } else {
        _inMemoryTransactions.asStateFlow()
    }

    private val _notifications = MutableStateFlow<List<BankNotification>>(
        listOf(
            BankNotification(
                id = "notif_welcome",
                title = "Monto por Registro Asignado",
                message = "Se ha acreditado con éxito el monto de bienvenida de 890,000.00 CUP a su cuenta en Moneda Nacional.",
                timestamp = System.currentTimeMillis() - 3600000,
                isRead = false,
                isUrgent = false,
                type = "BONUS"
            ),
            BankNotification(
                id = "notif_security",
                title = "Autenticación Biométrica Activada",
                message = "Su dispositivo cuenta con protección criptográfica de huella dactilar según regulaciones del Banco Central de Cuba.",
                timestamp = System.currentTimeMillis() - 7200000,
                isRead = true,
                isUrgent = false,
                type = "SECURITY"
            )
        )
    )
    val notifications: StateFlow<List<BankNotification>> = _notifications.asStateFlow()

    private val _activeAlertModal = MutableStateFlow<String?>(null)
    val activeAlertModal: StateFlow<String?> = _activeAlertModal.asStateFlow()

    private val _supportMessages = MutableStateFlow<List<SupportMessage>>(
        listOf(
            SupportMessage(
                id = "sup_01",
                sender = "Asistencia Banca Cuba",
                message = "¡Hola! Bienvenido al canal de soporte oficial. Puedes consultarme sobre transferencias, pago de servicios en CUP o MLC, y estado de tus depósitos.",
                isFromBank = true,
                timestamp = System.currentTimeMillis() - 1800000
            )
        )
    )
    val supportMessages: StateFlow<List<SupportMessage>> = _supportMessages.asStateFlow()

    val availableServices: List<PaymentService> = listOf(
        PaymentService(
            id = "une_electric",
            name = "Electricidad UNE",
            entity = "Unión Eléctrica de Cuba",
            category = "Servicios Básicos",
            description = "Pago de factura mensual de corriente eléctrica con 3% de bonificación",
            discountPercent = 3,
            identifierLabel = "ID de Cliente (Ruta / Folio - 13 dígitos)",
            placeholder = "Ej. 0812345678901",
            currency = CurrencyType.CUP
        ),
        PaymentService(
            id = "etecsa_phone",
            name = "Telefonía Fija y Móvil",
            entity = "ETECSA",
            category = "Telecomunicaciones",
            description = "Pago de factura telefónica y recarga de saldo Cubacel",
            discountPercent = 5,
            identifierLabel = "Número telefónico o de servicio",
            placeholder = "Ej. 5351234567 o 78683535",
            currency = CurrencyType.CUP
        ),
        PaymentService(
            id = "nauta_hogar",
            name = "Nauta Hogar / Internet",
            entity = "ETECSA Nauta",
            category = "Telecomunicaciones",
            description = "Cuota mensual del servicio de conexión a internet residencial",
            discountPercent = 5,
            identifierLabel = "Usuario Nauta / Número Enlace",
            placeholder = "usuario@nauta.com.cu",
            currency = CurrencyType.CUP
        ),
        PaymentService(
            id = "gas_manufacturado",
            name = "Gas Manufacturado",
            entity = "Empresa de Gas de La Habana",
            category = "Servicios Básicos",
            description = "Pago del suministro de gas por cañería",
            discountPercent = 3,
            identifierLabel = "Número de Cliente (7 dígitos)",
            placeholder = "Ej. 1045892",
            currency = CurrencyType.CUP
        ),
        PaymentService(
            id = "aguas_cuba",
            name = "Aguas de La Habana / Acueducto",
            entity = "Instituto Nacional de Recursos Hidráulicos",
            category = "Servicios Básicos",
            description = "Facturación por consumo de agua potable y saneamiento",
            discountPercent = 3,
            identifierLabel = "Código de Cobro / Ruta",
            placeholder = "Ej. 9023415",
            currency = CurrencyType.CUP
        ),
        PaymentService(
            id = "onat_taxes",
            name = "Tributos e Impuestos ONAT",
            entity = "Oficina Nacional de Administración Tributaria",
            category = "Obligaciones Fiscales",
            description = "Pago de declaración jurada, cuota mensual de TCP o MIPYMES",
            discountPercent = 3,
            identifierLabel = "NIT o Carné de Identidad (11 dígitos)",
            placeholder = "Ej. 85041219874",
            currency = CurrencyType.CUP
        ),
        PaymentService(
            id = "transfermovil_pay",
            name = "Pasarela Transfermóvil",
            entity = "Plataforma de Pago Móvil Nacional",
            category = "Comercio Electrónico",
            description = "Pago a comercios locales mediante código de barras o USSD",
            discountPercent = 4,
            identifierLabel = "Código de Comercio o Tarjeta",
            placeholder = "Ej. COMER-99120",
            currency = CurrencyType.CUP
        ),
        PaymentService(
            id = "enzona_pay",
            name = "EnZona Pasarela QR",
            entity = "Plataforma EnZona Xetid",
            category = "Comercio Electrónico",
            description = "Pagos directos por código QR y tiendas digitales en CUP/MLC",
            discountPercent = 4,
            identifierLabel = "Alias o Cuenta EnZona",
            placeholder = "Ej. cuenta@enzona.net",
            currency = CurrencyType.CUP
        )
    )

    val bankBranches: List<BankBranch> = listOf(
        BankBranch(
            name = "Banco Metropolitano - Sucursal Central",
            address = "Calle 23 esq. O, Vedado, Plaza de la Revolución",
            province = "La Habana",
            phone = "7868-3535",
            hasAtm = true,
            cashAvailability = "CUP y MLC disponibles"
        ),
        BankBranch(
            name = "BANDEC - Sucursal Habana Vieja",
            address = "Calle Cuba No. 402 e/ Amargura y Teniente Rey",
            province = "La Habana",
            phone = "802-01999",
            hasAtm = true,
            cashAvailability = "CUP disponible"
        ),
        BankBranch(
            name = "Banco Popular de Ahorro (BPA)",
            address = "Calle Enramadas No. 602, Santiago de Cuba",
            province = "Santiago de Cuba",
            phone = "802-00272",
            hasAtm = true,
            cashAvailability = "CUP y MLC disponibles"
        ),
        BankBranch(
            name = "BANDEC - Sucursal Camagüey",
            address = "Calle República No. 154, Camagüey",
            province = "Camagüey",
            phone = "322-91122",
            hasAtm = true,
            cashAvailability = "CUP disponible"
        ),
        BankBranch(
            name = "Banco Metropolitano - Miramar",
            address = "5ta Avenida y Calle 42, Playa",
            province = "La Habana",
            phone = "7204-1845",
            hasAtm = true,
            cashAvailability = "CUP y MLC disponibles"
        )
    )

    init {
        if (transactionDao != null) {
            scope.launch {
                val count = transactionDao.getTransactionCount()
                if (count == 0) {
                    _inMemoryTransactions.value.forEach {
                        transactionDao.insertTransaction(TransactionEntity.fromDomainModel(it))
                    }
                }
            }
        }
    }

    fun clearActiveAlertModal() {
        _activeAlertModal.value = null
    }

    fun sendTransfer(
        fromCurrency: CurrencyType,
        destinationCard: String,
        destinationBank: String,
        recipientName: String,
        amount: Double,
        concept: String
    ): Boolean {
        if (amount <= 0) return false

        val sourceAccount = if (fromCurrency == CurrencyType.CUP) _cupAccount.value else _mlcAccount.value
        if (sourceAccount.balance < amount) {
            return false
        }

        if (fromCurrency == CurrencyType.CUP) {
            _cupAccount.update { it.copy(balance = it.balance - amount) }
        } else {
            _mlcAccount.update { it.copy(balance = it.balance - amount) }
        }

        val ref = "TRANSF-CU-${(100000..999999).random()}"
        val transaction = Transaction(
            id = UUID.randomUUID().toString(),
            referenceNumber = ref,
            title = "Transferencia a $recipientName",
            description = if (concept.isBlank()) "Transferencia de fondos interbancaria" else concept,
            amount = amount,
            currency = fromCurrency,
            type = TransactionType.TRANSFER_SENT,
            status = TransactionStatus.PENDING_24H,
            recipient = recipientName,
            destinationCard = destinationCard,
            destinationBank = destinationBank,
            fee = 0.0,
            alertNotice = CONNECTION_DELAY_ALERT_MSG
        )

        _inMemoryTransactions.update { listOf(transaction) + it }

        if (transactionDao != null) {
            scope.launch {
                transactionDao.insertTransaction(TransactionEntity.fromDomainModel(transaction))
            }
        }

        // Trigger required alert notice!
        _activeAlertModal.value = CONNECTION_DELAY_ALERT_MSG

        // Real-time Push Notification
        val notif = BankNotification(
            id = UUID.randomUUID().toString(),
            title = "Transferencia en Tránsito - Plazo 24h",
            message = "Transferencia por ${formatCurrency(amount, fromCurrency)} enviada a $recipientName. $CONNECTION_DELAY_ALERT_MSG",
            timestamp = System.currentTimeMillis(),
            isRead = false,
            isUrgent = true,
            type = "TRANSFER_DELAY_ALERT"
        )
        _notifications.update { listOf(notif) + it }

        return true
    }

    fun payService(
        service: PaymentService,
        accountCurrency: CurrencyType,
        clientIdentifier: String,
        rawAmount: Double
    ): Boolean {
        if (rawAmount <= 0) return false
        val discount = rawAmount * (service.discountPercent / 100.0)
        val finalAmount = rawAmount - discount

        val account = if (accountCurrency == CurrencyType.CUP) _cupAccount.value else _mlcAccount.value
        if (account.balance < finalAmount) return false

        if (accountCurrency == CurrencyType.CUP) {
            _cupAccount.update { it.copy(balance = it.balance - finalAmount) }
        } else {
            _mlcAccount.update { it.copy(balance = it.balance - finalAmount) }
        }

        val ref = "PAGO-${service.id.take(4).uppercase()}-${(10000..99999).random()}"
        val transaction = Transaction(
            id = UUID.randomUUID().toString(),
            referenceNumber = ref,
            title = "Pago ${service.name}",
            description = "Pago de servicio (${service.entity}) - Ref: $clientIdentifier (Descuento aplicado: ${service.discountPercent}%)",
            amount = finalAmount,
            currency = accountCurrency,
            type = TransactionType.SERVICE_PAYMENT,
            status = TransactionStatus.COMPLETED,
            recipient = service.entity,
            serviceCategory = service.category,
            fee = 0.0
        )

        _inMemoryTransactions.update { listOf(transaction) + it }

        if (transactionDao != null) {
            scope.launch {
                transactionDao.insertTransaction(TransactionEntity.fromDomainModel(transaction))
            }
        }

        val notif = BankNotification(
            id = UUID.randomUUID().toString(),
            title = "Pago de Servicio Exitoso",
            message = "Se ha pagado ${service.name} por ${formatCurrency(finalAmount, accountCurrency)} con ${service.discountPercent}% de bonificación bancaria.",
            timestamp = System.currentTimeMillis(),
            isRead = false,
            isUrgent = false,
            type = "SERVICE_PAID"
        )
        _notifications.update { listOf(notif) + it }

        return true
    }

    fun toggleCardFreeze(currency: CurrencyType) {
        if (currency == CurrencyType.CUP) {
            _cupAccount.update { it.copy(isFrozen = !it.isFrozen) }
        } else {
            _mlcAccount.update { it.copy(isFrozen = !it.isFrozen) }
        }
    }

    fun updateDailyLimit(currency: CurrencyType, newLimit: Double) {
        if (currency == CurrencyType.CUP) {
            _cupAccount.update { it.copy(dailyLimit = newLimit) }
        } else {
            _mlcAccount.update { it.copy(dailyLimit = newLimit) }
        }
    }

    fun markNotificationsAsRead() {
        _notifications.update { list -> list.map { it.copy(isRead = true) } }
    }

    fun addSupportMessage(userMessage: String) {
        val userMsg = SupportMessage(
            id = UUID.randomUUID().toString(),
            sender = "Tú",
            message = userMessage,
            isFromBank = false,
            timestamp = System.currentTimeMillis()
        )

        val autoResponse = generateSupportAnswer(userMessage)
        val botMsg = SupportMessage(
            id = UUID.randomUUID().toString(),
            sender = "Asistencia Banca Cuba",
            message = autoResponse,
            isFromBank = true,
            timestamp = System.currentTimeMillis() + 500
        )

        _supportMessages.update { it + userMsg + botMsg }
    }

    private fun generateSupportAnswer(query: String): String {
        val q = query.lowercase()
        return when {
            q.contains("24") || q.contains("plazo") || q.contains("demora") || q.contains("conexion") || q.contains("conexión") || q.contains("deposito") || q.contains("depósito") -> {
                "De acuerdo con las normativas bancarias, cuando se presentan intermitencias o problemas de conexión en los nodos interbancarios, los depósitos o transferencias quedan asegurados y se reflejan en la cuenta del usuario en un plazo máximo de veinticuatro (24) horas hábiles. Tu comprobante con número de referencia certifica la transacción."
            }
            q.contains("890") || q.contains("registro") || q.contains("bono") || q.contains("monto") -> {
                "El monto de bienvenida por registro asignado a tu cuenta es de 890,000.00 CUP en Moneda Nacional. Puedes consultarlo y gestionarlo en la sección de cuentas."
            }
            q.contains("mlc") || q.contains("usd") || q.contains("tasa") || q.contains("cambio") -> {
                "Las cuentas en MLC operan con tarjetas magnéticas vinculadas a depósitos en divisa. La tasa oficial del Banco Central de Cuba se actualiza diariamente conforme a la Resolución 176."
            }
            q.contains("transfermovil") || q.contains("enzona") -> {
                "Banca Cuba está integrada con Transfermóvil y EnZona, permitiendo pagos con bonificaciones de hasta el 5% en facturas de electricidad, agua, gas y recargas ETECSA."
            }
            q.contains("bloqueo") || q.contains("perdida") || q.contains("tarjeta") -> {
                "Puedes congelar o descongelar tus tarjetas de inmediato desde la pestaña 'Cuentas' con un solo toque, o llamar al 7868-3535 (BANMET) / 802-01999 (BANDEC)."
            }
            else -> {
                "Gracias por comunicarte con el Centro de Atención de Banca Cuba. Para asistencia especializada, también puedes acudir a cualquiera de nuestras sucursales o llamar al servicio telefónico 24 horas al 7868-3535."
            }
        }
    }

    private fun formatCurrency(amount: Double, currency: CurrencyType): String {
        return "%,.2f %s".format(amount, currency.code)
    }
}
