package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CurrencyType
import com.example.model.Transaction
import com.example.model.TransactionStatus
import com.example.model.TransactionType
import com.example.ui.theme.BankCUPColor
import com.example.ui.theme.BankGoldAccent
import com.example.ui.theme.BankMLCColor
import com.example.ui.theme.BankNavyDark
import com.example.ui.theme.BankNavyPrimary
import com.example.ui.theme.BankWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    transactions: List<Transaction>,
    selectedFilter: String,
    searchQuery: String,
    onFilterChange: (String) -> Unit,
    onSearchChange: (String) -> Unit,
    onSelectTransaction: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryTabs = listOf(
        "TODOS",
        "TRANSFERENCIAS",
        "PAGOS SERVICIOS",
        "EN TRÁNSITO (24H)",
        "MONEDA CUP",
        "DIVISAS MLC"
    )

    var selectedTabIndex by remember(selectedFilter) {
        mutableIntStateOf(
            categoryTabs.indexOfFirst { it.equals(selectedFilter, ignoreCase = true) }
                .takeIf { it >= 0 } ?: 0
        )
    }

    // Secondary sub-filters: Status filter
    var statusSubFilter by remember { mutableStateOf("TODAS") } // "TODAS", "COMPLETADAS", "EN TRÁNSITO"

    // Summary calculations
    val totalCount = transactions.size
    val pending24hCount = transactions.count { it.status == TransactionStatus.PENDING_24H }
    val totalIncomeCup = transactions
        .filter { it.currency == CurrencyType.CUP && (it.type == TransactionType.REGISTRATION_BONUS || it.type == TransactionType.TRANSFER_RECEIVED) }
        .sumOf { it.amount }
    val totalExpenseCup = transactions
        .filter { it.currency == CurrencyType.CUP && (it.type == TransactionType.TRANSFER_SENT || it.type == TransactionType.SERVICE_PAYMENT) }
        .sumOf { it.amount }

    // Filtering logic
    val filteredList by remember(transactions, selectedTabIndex, statusSubFilter, searchQuery) {
        derivedStateOf {
            transactions.filter { tx ->
                val matchesTab = when (selectedTabIndex) {
                    1 -> tx.type == TransactionType.TRANSFER_SENT || tx.type == TransactionType.TRANSFER_RECEIVED
                    2 -> tx.type == TransactionType.SERVICE_PAYMENT || tx.type == TransactionType.LOCAL_GATEWAY_PAYMENT
                    3 -> tx.status == TransactionStatus.PENDING_24H
                    4 -> tx.currency == CurrencyType.CUP
                    5 -> tx.currency == CurrencyType.MLC
                    else -> true
                }

                val matchesStatus = when (statusSubFilter) {
                    "COMPLETADAS" -> tx.status == TransactionStatus.COMPLETED
                    "EN TRÁNSITO" -> tx.status == TransactionStatus.PENDING_24H
                    else -> true
                }

                val matchesSearch = if (searchQuery.isBlank()) true else {
                    tx.title.contains(searchQuery, ignoreCase = true) ||
                            tx.recipient.contains(searchQuery, ignoreCase = true) ||
                            tx.referenceNumber.contains(searchQuery, ignoreCase = true) ||
                            tx.description.contains(searchQuery, ignoreCase = true) ||
                            tx.destinationBank.contains(searchQuery, ignoreCase = true)
                }

                matchesTab && matchesStatus && matchesSearch
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("dedicated_history_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Historial de Transacciones",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Base de datos local Room • Registro oficial BCC",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(BankNavyPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = BankNavyPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Summary Metric Tiles Card
        item {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Resumen de Movimientos",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Metric 1: Total Transacciones
                        MetricTile(
                            title = "Total",
                            value = "$totalCount ops",
                            subtext = "En base de datos",
                            color = BankNavyPrimary,
                            modifier = Modifier.weight(1f)
                        )

                        // Metric 2: En Tránsito (24h)
                        MetricTile(
                            title = "En Tránsito",
                            value = "$pending24hCount pend.",
                            subtext = "Plazo 24h hábiles",
                            color = if (pending24hCount > 0) BankWarning else Color(0xFF2E7D32),
                            modifier = Modifier.weight(1f)
                        )

                        // Metric 3: Ingresos CUP (incl. 890k bono)
                        MetricTile(
                            title = "Ingresos CUP",
                            value = "${"%,.0f".format(totalIncomeCup)}",
                            subtext = "Incluye bono 890k",
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_history_input"),
                placeholder = { Text("Buscar por referencia, beneficiario o servicio...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BankNavyPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )
        }

        // Primary Category Scrollable Tabs
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                contentColor = BankNavyPrimary,
                edgePadding = 8.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = BankNavyPrimary,
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("history_tab_row")
            ) {
                categoryTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            onFilterChange(title)
                        },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) BankNavyPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }
        }

        // Secondary Status Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Estado:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                listOf("TODAS", "EN TRÁNSITO", "COMPLETADAS").forEach { statusLabel ->
                    FilterChip(
                        selected = statusSubFilter == statusLabel,
                        onClick = { statusSubFilter = statusLabel },
                        label = { Text(statusLabel, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (statusLabel == "EN TRÁNSITO") BankWarning.copy(alpha = 0.2f) else BankNavyPrimary.copy(alpha = 0.15f),
                            selectedLabelColor = if (statusLabel == "EN TRÁNSITO") BankWarning else BankNavyPrimary
                        ),
                        modifier = Modifier.testTag("status_filter_$statusLabel")
                    )
                }
            }
        }

        // Results Count Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mostrando ${filteredList.size} ${if (filteredList.size == 1) "registro" else "registros"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (searchQuery.isNotBlank() || selectedTabIndex != 0 || statusSubFilter != "TODAS") {
                    TextButton(
                        onClick = {
                            selectedTabIndex = 0
                            statusSubFilter = "TODAS"
                            onFilterChange("TODOS")
                            onSearchChange("")
                        }
                    ) {
                        Text("Restablecer Filtros", fontSize = 11.sp, color = BankNavyPrimary)
                    }
                }
            }
        }

        // Empty state
        if (filteredList.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No se encontraron operaciones",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No hay transacciones registradas con los filtros y término de búsqueda actuales.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(filteredList, key = { it.id }) { transaction ->
                DedicatedTransactionHistoryCard(
                    transaction = transaction,
                    onClick = { onSelectTransaction(transaction) }
                )
            }
        }
    }
}

@Composable
fun MetricTile(
    title: String,
    value: String,
    subtext: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.08f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(text = subtext, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DedicatedTransactionHistoryCard(
    transaction: Transaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPositive = transaction.type == TransactionType.REGISTRATION_BONUS ||
            transaction.type == TransactionType.TRANSFER_RECEIVED
    val sign = if (isPositive) "+" else "-"

    val dateFormatted = remember(transaction.timestamp) {
        val sdf = SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale("es", "CU"))
        sdf.format(Date(transaction.timestamp))
    }

    // Pulse animation for pending 24h transactions
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_card")
    val borderPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val cardBorder = if (transaction.status == TransactionStatus.PENDING_24H) {
        Modifier.border(1.5.dp, BankWarning.copy(alpha = borderPulse), RoundedCornerShape(16.dp))
    } else {
        Modifier
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = if (transaction.status == TransactionStatus.PENDING_24H) 4.dp else 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .then(cardBorder)
            .clickable { onClick() }
            .testTag("history_card_${transaction.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Type Icon + Title + Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    transaction.status == TransactionStatus.PENDING_24H -> BankWarning.copy(alpha = 0.15f)
                                    transaction.type == TransactionType.REGISTRATION_BONUS -> BankGoldAccent.copy(alpha = 0.2f)
                                    isPositive -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                                    else -> BankNavyPrimary.copy(alpha = 0.1f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                transaction.status == TransactionStatus.PENDING_24H -> Icons.Default.HourglassTop
                                transaction.type == TransactionType.REGISTRATION_BONUS -> Icons.Default.Star
                                transaction.type == TransactionType.SERVICE_PAYMENT -> Icons.Default.Receipt
                                isPositive -> Icons.Default.CheckCircle
                                else -> Icons.AutoMirrored.Filled.Send
                            },
                            contentDescription = null,
                            tint = when {
                                transaction.status == TransactionStatus.PENDING_24H -> BankWarning
                                transaction.type == TransactionType.REGISTRATION_BONUS -> BankNavyDark
                                isPositive -> Color(0xFF2E7D32)
                                else -> BankNavyPrimary
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = transaction.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = transaction.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Amount
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$sign${"%,.2f".format(transaction.amount)} ${transaction.currency.code}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = if (isPositive) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = transaction.currency.fullName.take(15),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status Indicator Badges & Metadata Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when (transaction.status) {
                        TransactionStatus.PENDING_24H -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(BankWarning.copy(alpha = 0.15f))
                                    .border(1.dp, BankWarning, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.HourglassTop,
                                        contentDescription = null,
                                        tint = BankWarning,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "EN TRÁNSITO (PLAZO 24H)",
                                        color = BankWarning,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        TransactionStatus.PROCESSING -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(BankNavyPrimary.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "EN PROCESAMIENTO",
                                    color = BankNavyPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        TransactionStatus.COMPLETED -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF2E7D32).copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "COMPLETADA",
                                        color = Color(0xFF2E7D32),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    if (transaction.type == TransactionType.REGISTRATION_BONUS) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(BankGoldAccent.copy(alpha = 0.25f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "BONO REGISTRO",
                                color = BankNavyDark,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Reference Number
                Text(
                    text = "Ref: ${transaction.referenceNumber.takeLast(10)}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Notice alert box if applicable
            if (transaction.alertNotice != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BankWarning.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = BankWarning,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = transaction.alertNotice,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Date & Destination footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = dateFormatted,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = transaction.destinationBank,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
