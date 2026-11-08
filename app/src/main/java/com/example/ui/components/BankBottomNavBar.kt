package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BankNavyPrimary
import com.example.viewmodel.AppScreen

@Composable
fun BankBottomNavBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("bank_bottom_nav_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == AppScreen.DASHBOARD,
            onClick = { onNavigate(AppScreen.DASHBOARD) },
            icon = { Icon(Icons.Default.AccountBalance, contentDescription = "Inicio") },
            label = { Text("Inicio", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_item_dashboard"),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = BankNavyPrimary.copy(alpha = 0.15f),
                selectedIconColor = BankNavyPrimary,
                selectedTextColor = BankNavyPrimary
            )
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.TRANSFERS,
            onClick = { onNavigate(AppScreen.TRANSFERS) },
            icon = { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Transferir") },
            label = { Text("Transferir", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_item_transfers"),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = BankNavyPrimary.copy(alpha = 0.15f),
                selectedIconColor = BankNavyPrimary,
                selectedTextColor = BankNavyPrimary
            )
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.SERVICES,
            onClick = { onNavigate(AppScreen.SERVICES) },
            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Pagos") },
            label = { Text("Servicios", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_item_services"),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = BankNavyPrimary.copy(alpha = 0.15f),
                selectedIconColor = BankNavyPrimary,
                selectedTextColor = BankNavyPrimary
            )
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.ACCOUNTS_CARD,
            onClick = { onNavigate(AppScreen.ACCOUNTS_CARD) },
            icon = { Icon(Icons.Default.CreditCard, contentDescription = "Cuentas") },
            label = { Text("Cuentas", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_item_accounts"),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = BankNavyPrimary.copy(alpha = 0.15f),
                selectedIconColor = BankNavyPrimary,
                selectedTextColor = BankNavyPrimary
            )
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.HISTORY,
            onClick = { onNavigate(AppScreen.HISTORY) },
            icon = { Icon(Icons.Default.History, contentDescription = "Historial") },
            label = { Text("Historial", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_item_history"),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = BankNavyPrimary.copy(alpha = 0.15f),
                selectedIconColor = BankNavyPrimary,
                selectedTextColor = BankNavyPrimary
            )
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.SUPPORT,
            onClick = { onNavigate(AppScreen.SUPPORT) },
            icon = { Icon(Icons.Default.SupportAgent, contentDescription = "Soporte") },
            label = { Text("Soporte", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_item_support"),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = BankNavyPrimary.copy(alpha = 0.15f),
                selectedIconColor = BankNavyPrimary,
                selectedTextColor = BankNavyPrimary
            )
        )
    }
}
