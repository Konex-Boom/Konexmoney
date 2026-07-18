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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Debt
import com.example.ui.Utils
import com.example.ui.theme.*
import com.example.viewmodel.FinanceViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val solde by viewModel.soldeActuel.collectAsState()
    val actives by viewModel.dettesActives.collectAsState()
    val reglees by viewModel.dettesReglees.collectAsState()

    var activeTab by remember { mutableStateOf("active") } // "active" or "settled"
    var searchText by remember { mutableStateOf("") }

    // Dialog state controllers
    var showAddDialog by remember { mutableStateOf(false) }
    var showPostponeDialog by remember { mutableStateOf<Debt?>(null) }
    var showEditDialog by remember { mutableStateOf<Debt?>(null) }
    var debtToDelete by remember { mutableStateOf<Debt?>(null) }

    // Form states for ADD / EDIT
    var inputPerson by remember { mutableStateOf("") }
    var inputType by remember { mutableStateOf("Je dois") } // "Je dois" or "On me doit"
    var inputAmount by remember { mutableStateOf("") }
    var inputBorrowDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var inputDueDate by remember { mutableLongStateOf(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)) } // default 7 days
    var inputNotes by remember { mutableStateOf("") }

    // Postpone Dialog form state
    var postponeDueDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var postponeReason by remember { mutableStateOf("") }

    // Filtered lists based on search
    val filteredActives = actives.filter {
        it.personne.contains(searchText, ignoreCase = true) || (it.notes ?: "").contains(searchText, ignoreCase = true)
    }.sortedBy { it.dateEcheance }

    val filteredReglees = reglees.filter {
        it.personne.contains(searchText, ignoreCase = true)
    }.sortedByDescending { it.dateReglement ?: 0L }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Header & Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Gestion des dettes",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Suivez et réglez vos engagements financiers",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Button(
                onClick = {
                    inputPerson = ""
                    inputType = "Je dois"
                    inputAmount = ""
                    inputBorrowDate = System.currentTimeMillis()
                    inputDueDate = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)
                    inputNotes = ""
                    showAddDialog = true
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SecondaryGreen,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.testTag("btn_add_debt_dialog")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Créer")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Créer", style = MaterialTheme.typography.labelLarge)
            }
        }

        // Segmented Tab Controls
        TabRow(
            selectedTabIndex = if (activeTab == "active") 0 else 1,
            containerColor = Color.Transparent,
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Tab(
                selected = activeTab == "active",
                onClick = { activeTab = "active" },
                modifier = Modifier.testTag("tab_dettes_actives")
            ) {
                Box(modifier = Modifier.padding(vertical = 12.dp)) {
                    Text(
                        text = "Dettes Actives (${actives.size})",
                        fontWeight = if (activeTab == "active") FontWeight.Bold else FontWeight.Normal,
                        color = if (activeTab == "active") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                }
            }
            Tab(
                selected = activeTab == "settled",
                onClick = { activeTab = "settled" },
                modifier = Modifier.testTag("tab_dettes_reglees")
            ) {
                Box(modifier = Modifier.padding(vertical = 12.dp)) {
                    Text(
                        text = "Dettes Réglées (${reglees.size})",
                        fontWeight = if (activeTab == "settled") FontWeight.Bold else FontWeight.Normal,
                        color = if (activeTab == "settled") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // Search bar
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text("Rechercher une dette...", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Rechercher") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("input_search_debts"),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            singleLine = true
        )

        // Selected list content
        val displayedList = if (activeTab == "active") filteredActives else filteredReglees

        if (displayedList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (activeTab == "active") Icons.Default.CheckCircle else Icons.Default.Inbox,
                        contentDescription = "Vide",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (activeTab == "active") "Tout est en ordre ! Aucune dette active." else "Aucune dette réglée historique.",
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp, top = 4.dp)
            ) {
                items(displayedList) { debt ->
                    val daysRemaining = Utils.getDaysRemaining(debt.dateEcheance)
                    val isUrgent = daysRemaining < 3
                    val borderIndicatorColor = if (debt.reglee) MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f) else if (isUrgent) StatusUrgent else StatusUpcoming
                    val tagBgColor = if (isUrgent) StatusUrgentContainer else StatusUpcomingContainer
                    val tagTextColor = if (isUrgent) StatusUrgent else StatusUpcoming
                    val labelText = if (isUrgent) "🔴 URGENT (${daysRemaining}j)" else "🟡 À venir (${daysRemaining}j)"

                    // Suggestion flag "Vous avez de l'argent"
                    val showSuggestion = !debt.reglee && debt.type == "Je dois" && solde >= debt.montant

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("debt_item_${debt.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)) {
                            // Left Accent bar
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .fillMaxHeight()
                                    .background(borderIndicatorColor)
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                // Upper detail Row (Person + Amount)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                        Text(
                                            text = debt.personne,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = debt.type,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = if (debt.type == "Je dois") StatusUrgent else PrimaryGreen
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .background(MaterialTheme.colorScheme.outline, CircleShape)
                                            )
                                            Text(
                                                text = debt.notes ?: "Prêt personnel",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.secondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${Utils.formatAriary(debt.montant)} Ar",
                                            style = MaterialTheme.typography.headlineLarge,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )

                                        if (!debt.reglee) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(top = 4.dp)
                                                    .background(tagBgColor, RoundedCornerShape(100.dp))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = labelText,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = tagTextColor,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = "Réglé le ${Utils.formatDate(debt.dateReglement ?: 0L)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = PrimaryGreen,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }

                                // Suggestion banner
                                if (showSuggestion) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = PrimaryContainerGreen
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lightbulb,
                                                contentDescription = "Suggestion",
                                                tint = PrimaryGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "Vous avez assez d'argent pour rembourser maintenant !",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = OnPrimaryContainerGreen,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Action Buttons (Edit / Settle / Postpone / Delete)
                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (!debt.reglee) {
                                        // "Régler" button
                                        Button(
                                            onClick = { viewModel.settleDebt(debt.id, System.currentTimeMillis()) },
                                            modifier = Modifier
                                                .weight(1.5f)
                                                .height(38.dp)
                                                .testTag("btn_settle_debt_${debt.id}"),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = SecondaryGreen,
                                                contentColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Régler", style = MaterialTheme.typography.labelLarge)
                                        }

                                        // "Reporter" button
                                        OutlinedButton(
                                            onClick = {
                                                postponeDueDate = debt.dateEcheance
                                                postponeReason = ""
                                                showPostponeDialog = debt
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(38.dp)
                                                .testTag("btn_postpone_debt_${debt.id}"),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.secondary
                                            )
                                        ) {
                                            Text("Reporter", style = MaterialTheme.typography.labelLarge)
                                        }
                                    }

                                    // "Modifier" and "Supprimer" button icons
                                    IconButton(
                                        onClick = {
                                            inputPerson = debt.personne
                                            inputType = debt.type
                                            inputAmount = debt.montant.toString()
                                            inputBorrowDate = debt.dateEmprunt
                                            inputDueDate = debt.dateEcheance
                                            inputNotes = debt.notes ?: ""
                                            showEditDialog = debt
                                        },
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .testTag("btn_edit_debt_${debt.id}")
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Modifier",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    IconButton(
                                        onClick = { debtToDelete = debt },
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(
                                                StatusUrgentContainer,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .testTag("btn_delete_debt_${debt.id}")
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Supprimer",
                                            modifier = Modifier.size(16.dp),
                                            tint = StatusUrgent
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Debt Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Ajouter une dette", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Type: "Je dois" or "On me doit"
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Je dois", "On me doit").forEach { type ->
                            val active = inputType == type
                            val col = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            val tc = if (active) Color.White else MaterialTheme.colorScheme.onBackground

                            Button(
                                onClick = { inputType = type },
                                modifier = Modifier.weight(1f).testTag("select_dialog_debt_type_$type"),
                                colors = ButtonDefaults.buttonColors(containerColor = col, contentColor = tc),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(text = type)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = inputPerson,
                        onValueChange = { inputPerson = it },
                        label = { Text("Personne") },
                        modifier = Modifier.fillMaxWidth().testTag("input_debt_person"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = inputAmount,
                        onValueChange = { inputAmount = it },
                        label = { Text("Montant (Ariary)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("input_debt_amount"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = inputNotes,
                        onValueChange = { inputNotes = it },
                        label = { Text("Notes / Raison d'emprunt") },
                        modifier = Modifier.fillMaxWidth().testTag("input_debt_notes"),
                        singleLine = true
                    )

                    // Borrow Date Picker
                    val borrowDatePicker = DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            val cal = Calendar.getInstance()
                            cal.set(y, m, d)
                            inputBorrowDate = cal.timeInMillis
                        },
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH),
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                    )

                    OutlinedTextField(
                        value = Utils.formatDate(inputBorrowDate),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date d'emprunt") },
                        trailingIcon = {
                            IconButton(onClick = { borrowDatePicker.show() }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Borrow Date")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { borrowDatePicker.show() }.testTag("input_debt_borrow_date")
                    )

                    // Due Date Picker
                    val dueDatePicker = DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            val cal = Calendar.getInstance()
                            cal.set(y, m, d)
                            inputDueDate = cal.timeInMillis
                        },
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH),
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                    )

                    OutlinedTextField(
                        value = Utils.formatDate(inputDueDate),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date d'échéance") },
                        trailingIcon = {
                            IconButton(onClick = { dueDatePicker.show() }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Due Date")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { dueDatePicker.show() }.testTag("input_debt_due_date")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = inputAmount.toDoubleOrNull() ?: 0.0
                        if (amount > 0.0 && inputPerson.isNotBlank()) {
                            viewModel.addDebt(
                                personne = inputPerson,
                                type = inputType,
                                montant = amount,
                                dateEmprunt = inputBorrowDate,
                                dateEcheance = inputDueDate,
                                notes = if (inputNotes.isBlank()) null else inputNotes
                            )
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("dialog_debt_save")
                ) {
                    Text("Créer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Postpone Dialog
    if (showPostponeDialog != null) {
        val debtToPostpone = showPostponeDialog!!
        AlertDialog(
            onDismissRequest = { showPostponeDialog = null },
            title = { Text("Reporter l'échéance", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Choisissez une nouvelle date d'échéance pour ${debtToPostpone.personne} et indiquez la raison.", fontSize = 14.sp)

                    val postponeDatePicker = DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            val cal = Calendar.getInstance()
                            cal.set(y, m, d)
                            postponeDueDate = cal.timeInMillis
                        },
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH),
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                    )

                    OutlinedTextField(
                        value = Utils.formatDate(postponeDueDate),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Nouvelle date") },
                        trailingIcon = {
                            IconButton(onClick = { postponeDatePicker.show() }) {
                                Icon(Icons.Default.DateRange, contentDescription = "New Date")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { postponeDatePicker.show() }.testTag("input_postpone_date")
                    )

                    OutlinedTextField(
                        value = postponeReason,
                        onValueChange = { postponeReason = it },
                        label = { Text("Raison du report") },
                        modifier = Modifier.fillMaxWidth().testTag("input_postpone_reason"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.postponeDebt(debtToPostpone.id, postponeDueDate, postponeReason)
                        showPostponeDialog = null
                    },
                    modifier = Modifier.testTag("dialog_postpone_save")
                ) {
                    Text("Confirmer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPostponeDialog = null }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Edit Debt Dialog
    if (showEditDialog != null) {
        val editingDebt = showEditDialog!!
        AlertDialog(
            onDismissRequest = { showEditDialog = null },
            title = { Text("Modifier la dette", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = inputPerson,
                        onValueChange = { inputPerson = it },
                        label = { Text("Personne") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_debt_person"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = inputAmount,
                        onValueChange = { inputAmount = it },
                        label = { Text("Montant (Ariary)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("edit_debt_amount"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = inputNotes,
                        onValueChange = { inputNotes = it },
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_debt_notes"),
                        singleLine = true
                    )

                    // Due Date Picker
                    val dueDatePicker = DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            val cal = Calendar.getInstance()
                            cal.set(y, m, d)
                            inputDueDate = cal.timeInMillis
                        },
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH),
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                    )

                    OutlinedTextField(
                        value = Utils.formatDate(inputDueDate),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date d'échéance") },
                        trailingIcon = {
                            IconButton(onClick = { dueDatePicker.show() }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Due Date")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { dueDatePicker.show() }.testTag("edit_debt_due_date")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = inputAmount.toDoubleOrNull() ?: 0.0
                        if (amount > 0.0 && inputPerson.isNotBlank()) {
                            viewModel.updateDebt(
                                editingDebt.copy(
                                    personne = inputPerson,
                                    montant = amount,
                                    dateEcheance = inputDueDate,
                                    notes = if (inputNotes.isBlank()) null else inputNotes
                                )
                            )
                            showEditDialog = null
                        }
                    },
                    modifier = Modifier.testTag("dialog_edit_save")
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = null }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (debtToDelete != null) {
        AlertDialog(
            onDismissRequest = { debtToDelete = null },
            title = { Text("Supprimer la dette") },
            text = { Text("Êtes-vous sûr de vouloir supprimer définitivement cette dette ? Elle sera retirée de l'historique.") },
            confirmButton = {
                Button(
                    onClick = {
                        debtToDelete?.let { viewModel.deleteDebt(it) }
                        debtToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusUrgent),
                    modifier = Modifier.testTag("btn_confirm_delete_debt")
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { debtToDelete = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}
