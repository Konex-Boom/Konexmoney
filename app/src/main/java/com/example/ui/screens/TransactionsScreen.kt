package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Transaction
import com.example.ui.Utils
import com.example.ui.theme.*
import com.example.viewmodel.FinanceViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val transactions by viewModel.filteredTransactions.collectAsState()
    val activeType by viewModel.typeFilter.collectAsState()
    val activeCat by viewModel.categoryFilter.collectAsState()
    val activePeriod by viewModel.periodFilter.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

    // Dialog state for adding transactions
    var inputAmount by remember { mutableStateOf("") }
    var inputType by remember { mutableStateOf("Sortie") } // "Entrée" or "Sortie"
    var inputDesc by remember { mutableStateOf("") }
    var inputCat by remember { mutableStateOf("Alimentation") }
    var inputMoyen by remember { mutableStateOf("Espèces") }
    var inputDate by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val categories = listOf("Salaire", "Alimentation", "Transport", "Loisirs", "Santé", "Remboursement dette", "Autre")
    val moyens = listOf("Espèces", "Mobile Money", "Carte", "Virement")

    val dateString = Utils.formatDate(inputDate)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Section Header & Add Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Transactions",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Gérez vos flux financiers",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Button(
                onClick = {
                    inputAmount = ""
                    inputType = "Sortie"
                    inputDesc = ""
                    inputCat = "Alimentation"
                    inputMoyen = "Espèces"
                    inputDate = System.currentTimeMillis()
                    showAddDialog = true
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SecondaryGreen,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.testTag("btn_add_transaction_dialog")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Ajouter",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Ajouter",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // Quick Filters Type Tab Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("Tout", "Entrée", "Sortie").forEach { filterType ->
                val isActive = activeType == filterType
                val bg = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                val textCol = if (isActive) Color.White else MaterialTheme.colorScheme.secondary

                Box(
                    modifier = Modifier
                        .background(bg, RoundedCornerShape(100.dp))
                        .clickable { viewModel.setFilters(filterType, activeCat, activePeriod) }
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .testTag("filter_type_$filterType")
                ) {
                    Text(
                        text = if (filterType == "Tout") "Tout" else if (filterType == "Entrée") "Entrées" else "Sorties",
                        style = MaterialTheme.typography.labelLarge,
                        color = textCol
                    )
                }
            }
        }

        // Sub Filter Selectors Row (Category and Period)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Category Selector Filter
            var showCatDropdown by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                OutlinedCard(
                    onClick = { showCatDropdown = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (activeCat == "Tout") "Catégorie: Tout" else activeCat,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                    }
                }
                DropdownMenu(
                    expanded = showCatDropdown,
                    onDismissRequest = { showCatDropdown = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Toutes catégories") },
                        onClick = {
                            viewModel.setFilters(activeType, "Tout", activePeriod)
                            showCatDropdown = false
                        }
                    )
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                viewModel.setFilters(activeType, cat, activePeriod)
                                showCatDropdown = false
                            }
                        )
                    }
                }
            }

            // Period Selector Filter
            var showPeriodDropdown by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                OutlinedCard(
                    onClick = { showPeriodDropdown = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (activePeriod == "Tout") "Période: Tout" else activePeriod,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                    }
                }
                DropdownMenu(
                    expanded = showPeriodDropdown,
                    onDismissRequest = { showPeriodDropdown = false }
                ) {
                    listOf("Tout", "Mois en cours", "Année en cours").forEach { per ->
                        DropdownMenuItem(
                            text = { Text(per) },
                            onClick = {
                                viewModel.setFilters(activeType, activeCat, per)
                                showPeriodDropdown = false
                            }
                        )
                    }
                }
            }
        }

        // Transactions History list
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Wallet,
                        contentDescription = "Aucun",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Aucune transaction trouvée.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp)
            ) {
                items(transactions) { tx ->
                    val isIncome = tx.type == "Entrée"
                    val itemColor = if (isIncome) SecondaryGreen else StatusUrgent
                    val indicatorSym = if (isIncome) "+" else "-"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { transactionToDelete = tx }
                            .testTag("tx_item_${tx.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(
                                            if (isIncome) PrimaryContainerGreen else StatusUrgentContainer,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(tx.categorie),
                                        contentDescription = tx.categorie,
                                        tint = if (isIncome) PrimaryGreen else StatusUrgent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = tx.description,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${Utils.formatDate(tx.date)} • ${tx.categorie}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$indicatorSym${Utils.formatAriary(tx.montant)} Ar",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = itemColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = tx.moyenPaiement,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Transaction Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "Ajouter une transaction",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Type selector tab
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Sortie", "Entrée").forEach { type ->
                            val active = inputType == type
                            val col = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            val tc = if (active) Color.White else MaterialTheme.colorScheme.onBackground

                            Button(
                                onClick = { inputType = type },
                                modifier = Modifier.weight(1f).testTag("select_dialog_type_$type"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = col,
                                    contentColor = tc
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(text = if (type == "Sortie") "Dépense" else "Recette")
                            }
                        }
                    }

                    // Amount input
                    OutlinedTextField(
                        value = inputAmount,
                        onValueChange = { inputAmount = it },
                        label = { Text("Montant (Ariary)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("input_tx_amount"),
                        singleLine = true
                    )

                    // Description input
                    OutlinedTextField(
                        value = inputDesc,
                        onValueChange = { inputDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth().testTag("input_tx_desc"),
                        singleLine = true
                    )

                    // Category dropdown selector
                    var catExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        ExposedDropdownMenuBox(
                            expanded = catExpanded,
                            onExpandedChange = { catExpanded = !catExpanded }
                        ) {
                            OutlinedTextField(
                                value = inputCat,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Catégorie") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth().testTag("input_tx_cat")
                            )
                            ExposedDropdownMenu(
                                expanded = catExpanded,
                                onDismissRequest = { catExpanded = false }
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            inputCat = cat
                                            catExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Payment method selector
                    var moyenExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        ExposedDropdownMenuBox(
                            expanded = moyenExpanded,
                            onExpandedChange = { moyenExpanded = !moyenExpanded }
                        ) {
                            OutlinedTextField(
                                value = inputMoyen,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Moyen de paiement") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = moyenExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth().testTag("input_tx_moyen")
                            )
                            ExposedDropdownMenu(
                                expanded = moyenExpanded,
                                onDismissRequest = { moyenExpanded = false }
                            ) {
                                moyens.forEach { m ->
                                    DropdownMenuItem(
                                        text = { Text(m) },
                                        onClick = {
                                            inputMoyen = m
                                            moyenExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Date picker field
                    val datePickerDialog = DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            val cal = Calendar.getInstance()
                            cal.set(y, m, d)
                            inputDate = cal.timeInMillis
                        },
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH),
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                    )

                    OutlinedTextField(
                        value = dateString,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date de transaction") },
                        trailingIcon = {
                            IconButton(onClick = { datePickerDialog.show() }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerDialog.show() }
                            .testTag("input_tx_date")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = inputAmount.toDoubleOrNull() ?: 0.0
                        if (amount > 0.0 && inputDesc.isNotBlank()) {
                            viewModel.addTransaction(
                                montant = amount,
                                type = inputType,
                                description = inputDesc,
                                categorie = inputCat,
                                date = inputDate,
                                moyenPaiement = inputMoyen
                            )
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("dialog_tx_save")
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Confirmation delete transaction Dialog
    if (transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Supprimer la transaction") },
            text = { Text("Êtes-vous sûr de vouloir supprimer définitivement cette transaction ?") },
            confirmButton = {
                Button(
                    onClick = {
                        transactionToDelete?.let { viewModel.deleteTransaction(it) }
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusUrgent),
                    modifier = Modifier.testTag("btn_confirm_delete_tx")
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "Salaire" -> Icons.Default.Payments
        "Alimentation" -> Icons.Default.ShoppingCart
        "Transport" -> Icons.Default.DirectionsCar
        "Loisirs" -> Icons.Default.SportsEsports
        "Santé" -> Icons.Default.MedicalServices
        "Remboursement dette" -> Icons.Default.CheckCircle
        else -> Icons.Default.AccountBalanceWallet
    }
}
