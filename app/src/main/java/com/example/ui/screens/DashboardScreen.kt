package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.BankAccount
import com.example.model.CurrencyType
import com.example.model.Transaction
import com.example.model.TransactionStatus
import com.example.model.TransactionType
import com.example.ui.components.CubanDebitCard
import com.example.ui.theme.BankCUPColor
import com.example.ui.theme.BankGoldAccent
import com.example.ui.theme.BankMLCColor
import com.example.ui.theme.BankNavyDark
import com.example.ui.theme.BankNavyPrimary
import com.example.ui.theme.BankWarning
import com.example.viewmodel.AppScreen

@Composable
fun DashboardScreen(
    cupAccount: BankAccount,
    mlcAccount: BankAccount,
    recentTransactions: List<Transaction>,
    showRegistrationBonusBanner: Boolean,
    onDismissBonusBanner: () -> Unit,
    onNavigate: (AppScreen) -> Unit,
    onSelectTransaction: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: CUP, 1: MLC
    val currentAccount = if (selectedTab == 0) cupAccount else mlcAccount

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Image Header
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .testTag("hero_banner_card")
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_bank_hero),
                        contentDescription = "Banca Digital Cubana",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        BankNavyDark.copy(alpha = 0.88f),
                                        BankNavyPrimary.copy(alpha = 0.55f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = BankGoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BIENVENIDO A BANCA CUBA",
                                color = BankGoldAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Hola, ${cupAccount.cardHolder}",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Gestión Oficial de Cuentas CUP y MLC",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // REQUIRED: Prominent Registration Bonus Card (890 mil CUP)
        if (showRegistrationBonusBanner) {
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = BankNavyPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("registration_bonus_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(BankGoldAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "★",
                                    color = BankNavyDark,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Monto Asignado por Registro",
                                    color = BankGoldAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "890,000.00 CUP",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Acreditado a su cuenta principal",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        TextButton(
                            onClick = onDismissBonusBanner,
                            colors = ButtonDefaults.textButtonColors(contentColor = BankGoldAccent)
                        ) {
                            Text("Cerrar", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Currency Tabs (CUP vs MLC)
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                contentColor = BankNavyPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = if (selectedTab == 0) BankCUPColor else BankMLCColor
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("currency_tab_row")
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Moneda Nacional (CUP)",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Divisa Libre (MLC)",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        // Active Account Card Display
        item {
            CubanDebitCard(
                account = currentAccount,
                onCardClick = { onNavigate(AppScreen.ACCOUNTS_CARD) }
            )
        }

        // Balance Summary Box
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Saldo Total Disponible",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${"%,.2f".format(currentAccount.balance)} ${currentAccount.currency.code}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (currentAccount.currency == CurrencyType.CUP) BankNavyPrimary else BankMLCColor
                        )
                    }

                    Button(
                        onClick = { onNavigate(AppScreen.TRANSFERS) },
                        colors = ButtonDefaults.buttonColors(containerColor = BankNavyPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_quick_transfer")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Transferir", fontSize = 13.sp)
                    }
                }
            }
        }

        // Quick Access Actions
        item {
            Column {
                Text(
                    text = "Operaciones Rápidas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickActionButton(
                        icon = Icons.AutoMirrored.Filled.Send,
                        label = "Transferir",
                        tag = "action_transfer",
                        onClick = { onNavigate(AppScreen.TRANSFERS) }
                    )
                    QuickActionButton(
                        icon = Icons.Default.ReceiptLong,
                        label = "Servicios",
                        tag = "action_services",
                        onClick = { onNavigate(AppScreen.SERVICES) }
                    )
                    QuickActionButton(
                        icon = Icons.Default.PhoneAndroid,
                        label = "ETECSA",
                        tag = "action_etecsa",
                        onClick = { onNavigate(AppScreen.SERVICES) }
                    )
                    QuickActionButton(
                        icon = Icons.Default.LocalAtm,
                        label = "Cajeros",
                        tag = "action_atms",
                        onClick = { onNavigate(AppScreen.SUPPORT) }
                    )
                }
            }
        }

        // Connectivity Information Note
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = BankWarning.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = BankWarning,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Aviso Operativo: Si surgen intermitencias de red, los depósitos se procesan y reflejan en un plazo máximo de 24 horas hábiles.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Recent Movements Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Últimos Movimientos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = { onNavigate(AppScreen.HISTORY) },
                    modifier = Modifier.testTag("btn_view_all_history")
                ) {
                    Text("Ver todos", fontSize = 13.sp)
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Transaction items
        if (recentTransactions.isEmpty()) {
            item {
                Text(
                    text = "No hay transacciones recientes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(recentTransactions.take(4)) { transaction ->
                TransactionRowItem(
                    transaction = transaction,
                    onClick = { onSelectTransaction(transaction) }
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    tag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
            .testTag(tag)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = BankNavyPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun TransactionRowItem(
    transaction: Transaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPositive = transaction.type == TransactionType.REGISTRATION_BONUS ||
            transaction.type == TransactionType.TRANSFER_RECEIVED
    val sign = if (isPositive) "+" else "-"
    val amountColor = if (isPositive) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("tx_item_${transaction.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (transaction.status == TransactionStatus.PENDING_24H)
                                BankWarning.copy(alpha = 0.15f)
                            else if (isPositive) Color(0xFF2E7D32).copy(alpha = 0.15f)
                            else BankNavyPrimary.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (transaction.status == TransactionStatus.PENDING_24H)
                            Icons.Default.HourglassTop
                        else if (isPositive) Icons.Default.CheckCircle
                        else Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        tint = if (transaction.status == TransactionStatus.PENDING_24H)
                            BankWarning
                        else if (isPositive) Color(0xFF2E7D32)
                        else BankNavyPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (transaction.status == TransactionStatus.PENDING_24H)
                            "En Tránsito • Reflejo en 24h hábiles"
                        else transaction.destinationBank,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (transaction.status == TransactionStatus.PENDING_24H)
                            BankWarning
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$sign${"%,.2f".format(transaction.amount)} ${transaction.currency.code}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
                Text(
                    text = transaction.referenceNumber.takeLast(10),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
