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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.GasMeter
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.BankAccount
import com.example.model.CurrencyType
import com.example.model.PaymentService
import com.example.ui.theme.BankGoldAccent
import com.example.ui.theme.BankNavyDark
import com.example.ui.theme.BankNavyPrimary
import com.example.ui.theme.BankTealAccent

@Composable
fun ServicesScreen(
    services: List<PaymentService>,
    cupAccount: BankAccount,
    mlcAccount: BankAccount,
    selectedService: PaymentService?,
    identifierInput: String,
    amountInput: String,
    onSelectService: (PaymentService?) -> Unit,
    onInputsChange: (String, String) -> Unit,
    onExecutePayment: (CurrencyType) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryTab by remember { mutableIntStateOf(0) }
    val categories = listOf("Todos", "Servicios Básicos", "Telecomunicaciones", "Comercio & QR")

    val filteredServices = services.filter { service ->
        when (selectedCategoryTab) {
            1 -> service.category == "Servicios Básicos"
            2 -> service.category == "Telecomunicaciones"
            3 -> service.category == "Comercio Electrónico" || service.category == "Obligaciones Fiscales"
            else -> true
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("services_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Pagos Electrónicos Locales",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Integrado con Transfermóvil, EnZona y empresas estatales cubanas",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Promotional Discount Card
        item {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = BankTealAccent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Hasta 5% de Descuento Bancario",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Ahorra en tus facturas de UNE, ETECSA y tributos ONAT pagando desde tu app.",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Category Filter
        item {
            TabRow(
                selectedTabIndex = selectedCategoryTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                contentColor = BankNavyPrimary,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                categories.forEachIndexed { index, cat ->
                    Tab(
                        selected = selectedCategoryTab == index,
                        onClick = { selectedCategoryTab = index },
                        text = {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = if (selectedCategoryTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }

        // Services List
        items(filteredServices) { service ->
            PaymentServiceCard(
                service = service,
                onClick = { onSelectService(service) }
            )
        }
    }

    // Payment Dialog
    if (selectedService != null) {
        var paymentCurrency by remember { mutableStateOf(CurrencyType.CUP) }
        val account = if (paymentCurrency == CurrencyType.CUP) cupAccount else mlcAccount

        Dialog(onDismissRequest = { onSelectService(null) }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .testTag("service_payment_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = selectedService.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = selectedService.entity,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF2E7D32).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "-${selectedService.discountPercent}% Bonificación",
                                color = Color(0xFF2E7D32),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Account selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { paymentCurrency = CurrencyType.CUP },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = if (paymentCurrency == CurrencyType.CUP) {
                                ButtonDefaults.outlinedButtonColors(containerColor = BankNavyPrimary.copy(alpha = 0.1f))
                            } else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text("CUP (${"%,.0f".format(cupAccount.balance)})", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { paymentCurrency = CurrencyType.MLC },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = if (paymentCurrency == CurrencyType.MLC) {
                                ButtonDefaults.outlinedButtonColors(containerColor = BankNavyPrimary.copy(alpha = 0.1f))
                            } else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text("MLC (${"%,.2f".format(mlcAccount.balance)})", fontSize = 11.sp)
                        }
                    }

                    // Identifier input
                    OutlinedTextField(
                        value = identifierInput,
                        onValueChange = { onInputsChange(it, amountInput) },
                        label = { Text(selectedService.identifierLabel) },
                        placeholder = { Text(selectedService.placeholder) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_service_identifier"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Amount input
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { onInputsChange(identifierInput, it) },
                        label = { Text("Importe de Factura (${paymentCurrency.code})") },
                        placeholder = { Text("0.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_service_amount"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    val enteredAmount = amountInput.toDoubleOrNull() ?: 0.0
                    val discount = enteredAmount * (selectedService.discountPercent / 100.0)
                    val totalToPay = (enteredAmount - discount).coerceAtLeast(0.0)

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Importe Bruto:", fontSize = 12.sp)
                                Text("${"%,.2f".format(enteredAmount)} ${paymentCurrency.code}", fontSize = 12.sp)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Ahorro por Bono (${selectedService.discountPercent}%):", fontSize = 12.sp, color = Color(0xFF2E7D32))
                                Text("-${"%,.2f".format(discount)} ${paymentCurrency.code}", fontSize = 12.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total a Debitar:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${"%,.2f".format(totalToPay)} ${paymentCurrency.code}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BankNavyPrimary)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onSelectService(null) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = { onExecutePayment(paymentCurrency) },
                            enabled = totalToPay > 0 && totalToPay <= account.balance,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_confirm_pay_service"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BankNavyPrimary)
                        ) {
                            Text("Confirmar Pago")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentServiceCard(
    service: PaymentService,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when {
        service.id.contains("electric") -> Icons.Default.ElectricBolt
        service.id.contains("phone") || service.id.contains("nauta") -> Icons.Default.PhoneAndroid
        service.id.contains("gas") -> Icons.Default.GasMeter
        service.id.contains("agua") -> Icons.Default.WaterDrop
        service.id.contains("enzona") -> Icons.Default.QrCode
        service.id.contains("transfermovil") -> Icons.Default.Receipt
        else -> Icons.Default.AccountBalance
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("service_card_${service.id}")
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
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(BankNavyPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = BankNavyPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = service.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = service.entity,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${service.discountPercent}% descuento directo",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Button(
                onClick = onClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BankNavyPrimary)
            ) {
                Text("Pagar", fontSize = 12.sp)
            }
        }
    }
}
