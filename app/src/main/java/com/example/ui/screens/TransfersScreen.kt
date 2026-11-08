package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BankAccount
import com.example.model.CurrencyType
import com.example.ui.theme.BankCUPColor
import com.example.ui.theme.BankGoldAccent
import com.example.ui.theme.BankMLCColor
import com.example.ui.theme.BankNavyPrimary
import com.example.ui.theme.BankWarning
import com.example.viewmodel.TransferFormState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransfersScreen(
    cupAccount: BankAccount,
    mlcAccount: BankAccount,
    formState: TransferFormState,
    onFormChange: (TransferFormState.() -> TransferFormState) -> Unit,
    onSubmitTransfer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val banks = listOf(
        "Banco Metropolitano",
        "Banco de Crédito y Comercio (BANDEC)",
        "Banco Popular de Ahorro (BPA)"
    )
    var isBankDropdownExpanded by remember { mutableStateOf(false) }

    val recentContacts = listOf(
        Triple("María González", "9202 4410 8921 5402", "Banco Metropolitano"),
        Triple("Carlos Pérez", "9200 1198 3401 2281", "BANDEC"),
        Triple("Elena Rodríguez", "9201 8832 9901 4110", "BPA"),
        Triple("Tienda MLC Miramar", "9225 9012 3341 7890", "BANDEC")
    )

    val currentAccount = if (formState.fromCurrency == CurrencyType.CUP) cupAccount else mlcAccount

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("transfers_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Transferencias Interbancarias Seguras",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Operaciones en tiempo real entre tarjetas RED de Cuba",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Account Currency Switcher
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Cuenta de Origen",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    TabRow(
                        selectedTabIndex = if (formState.fromCurrency == CurrencyType.CUP) 0 else 1,
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.clip(RoundedCornerShape(10.dp))
                    ) {
                        Tab(
                            selected = formState.fromCurrency == CurrencyType.CUP,
                            onClick = { onFormChange { copy(fromCurrency = CurrencyType.CUP) } },
                            text = {
                                Text(
                                    "CUP (${"%,.0f".format(cupAccount.balance)} CUP)",
                                    fontSize = 12.sp,
                                    fontWeight = if (formState.fromCurrency == CurrencyType.CUP) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                        Tab(
                            selected = formState.fromCurrency == CurrencyType.MLC,
                            onClick = { onFormChange { copy(fromCurrency = CurrencyType.MLC) } },
                            text = {
                                Text(
                                    "MLC (${"%,.2f".format(mlcAccount.balance)} MLC)",
                                    fontSize = 12.sp,
                                    fontWeight = if (formState.fromCurrency == CurrencyType.MLC) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Saldo Disponible: ${"%,.2f".format(currentAccount.balance)} ${currentAccount.currency.code}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (formState.fromCurrency == CurrencyType.CUP) BankCUPColor else BankMLCColor
                    )
                }
            }
        }

        // Quick contacts
        item {
            Column {
                Text(
                    text = "Contactos Frecuentes",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(recentContacts) { (name, card, bank) ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp,
                            modifier = Modifier
                                .clickable {
                                    onFormChange {
                                        copy(
                                            recipientName = name,
                                            destinationCard = card,
                                            destinationBank = bank
                                        )
                                    }
                                }
                                .testTag("contact_$name")
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(BankNavyPrimary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = BankNavyPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = card.takeLast(4),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Form Fields
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Bank selector dropdown
                    ExposedDropdownMenuBox(
                        expanded = isBankDropdownExpanded,
                        onExpandedChange = { isBankDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = formState.destinationBank,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Banco de Destino") },
                            leadingIcon = {
                                Icon(Icons.Default.AccountBalance, contentDescription = null)
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isBankDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("dropdown_destination_bank"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = isBankDropdownExpanded,
                            onDismissRequest = { isBankDropdownExpanded = false }
                        ) {
                            banks.forEach { bank ->
                                DropdownMenuItem(
                                    text = { Text(bank) },
                                    onClick = {
                                        onFormChange { copy(destinationBank = bank) }
                                        isBankDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Card Number
                    OutlinedTextField(
                        value = formState.destinationCard,
                        onValueChange = { input ->
                            // Format digits
                            val digits = input.filter { it.isDigit() }.take(16)
                            val formatted = digits.chunked(4).joinToString(" ")
                            onFormChange { copy(destinationCard = formatted) }
                        },
                        label = { Text("Número de Tarjeta (16 dígitos)") },
                        placeholder = { Text("Ej. 9202 1234 5678 9012") },
                        leadingIcon = {
                            Icon(Icons.Default.CreditCard, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_destination_card"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Recipient Name
                    OutlinedTextField(
                        value = formState.recipientName,
                        onValueChange = { name -> onFormChange { copy(recipientName = name) } },
                        label = { Text("Nombre del Beneficiario (Opcional)") },
                        placeholder = { Text("Ej. Roberto Gómez") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_recipient_name"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Amount
                    OutlinedTextField(
                        value = formState.amountText,
                        onValueChange = { amount ->
                            // Allow valid decimal
                            if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d{0,2}\$"))) {
                                onFormChange { copy(amountText = amount) }
                            }
                        },
                        label = { Text("Monto a Transferir (${formState.fromCurrency.code})") },
                        placeholder = { Text("0.00") },
                        leadingIcon = {
                            Text(
                                text = formState.fromCurrency.symbol,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_amount"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Concept
                    OutlinedTextField(
                        value = formState.concept,
                        onValueChange = { concept -> onFormChange { copy(concept = concept) } },
                        label = { Text("Motivo / Concepto") },
                        placeholder = { Text("Ej. Pago de factura, apoyo familiar") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_concept"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Error text
                    if (formState.error != null) {
                        Text(
                            text = formState.error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    // Regulatory reminder
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BankWarning.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = BankWarning,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Aviso Operativo: Si la red presenta intermitencias de conexión, el depósito se reflejará en la cuenta en un plazo máximo de 24 horas hábiles.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Transfer submit button
                    Button(
                        onClick = onSubmitTransfer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_submit_transfer"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BankNavyPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = BankGoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Autorizar y Enviar Transferencia",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
