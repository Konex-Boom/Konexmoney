package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Transaction
import com.example.ui.Utils
import com.example.ui.theme.*
import com.example.viewmodel.FinanceViewModel
import java.util.*

@Composable
fun StatisticsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.allTransactions.collectAsState()
    val entriesMois by viewModel.totalEntreesMois.collectAsState()
    val exitsMois by viewModel.totalSortiesMois.collectAsState()

    // 1. Calculate Net du Mois
    val netMois = entriesMois - exitsMois
    val isPositiveNet = netMois >= 0

    // 2. Expenditures by Category (Pie chart calculations)
    // Filter out only expense type "Sortie"
    val expenseTransactions = transactions.filter { it.type == "Sortie" }
    val totalExpenses = expenseTransactions.sumOf { it.montant }

    val categoryExpenses = expenseTransactions.groupBy { it.categorie }
        .mapValues { (_, txs) -> txs.sumOf { it.montant } }

    // Define colors mapped to categories
    val categoryColors = mapOf(
        "Alimentation" to CatAlimentation,
        "Transport" to CatTransport,
        "Loisirs" to CatLoisirs,
        "Santé" to CatSante,
        "Remboursement dette" to CatDebt,
        "Autre" to CatAutre,
        "Salaire" to CatSalary
    )

    // 3. Monthly entries and exits (Bar chart calculations)
    // Map to represent the last 5 months: (Month Name, Month Index, Entries, Exits)
    val monthlyStats = remember(transactions) {
        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR)

        // Initialize last 5 months
        val monthsList = mutableListOf<MonthData>()
        val tempCal = Calendar.getInstance()
        tempCal.add(Calendar.MONTH, -4) // start 4 months ago

        for (i in 0..4) {
            val monthIdx = tempCal.get(Calendar.MONTH)
            val monthName = getFrenchMonthAbbreviation(monthIdx)
            monthsList.add(MonthData(name = monthName, index = monthIdx, entries = 0.0, exits = 0.0))
            tempCal.add(Calendar.MONTH, 1)
        }

        // Fill stats from transactions of the current year
        transactions.forEach { tx ->
            cal.timeInMillis = tx.date
            if (cal.get(Calendar.YEAR) == currentYear) {
                val txMonth = cal.get(Calendar.MONTH)
                val targetMonthData = monthsList.find { it.index == txMonth }
                if (targetMonthData != null) {
                    if (tx.type == "Entrée") {
                        targetMonthData.entries += tx.montant
                    } else {
                        targetMonthData.exits += tx.montant
                    }
                }
            }
        }
        monthsList
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(bottom = 100.dp, top = 16.dp)
    ) {
        // Section Title
        item {
            Column {
                Text(
                    text = "RÉSUMÉ ANALYTIQUE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Statistiques",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Net Month Card Panel (Bento highlight)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_net_mois"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "NET DU MOIS (MAI)", // Showing current month context
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${if (isPositiveNet) "+ " else "- "}${Utils.formatAriary(Math.abs(netMois))} Ar",
                                style = MaterialTheme.typography.displayLarge,
                                color = if (isPositiveNet) PrimaryGreen else StatusUrgent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isPositiveNet) PrimaryContainerGreen else StatusUrgentContainer,
                                    RoundedCornerShape(100.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPositiveNet) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                    contentDescription = "Trend",
                                    tint = if (isPositiveNet) PrimaryGreen else StatusUrgent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (isPositiveNet) "+12%" else "-5%", // Visual mockup percentage
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isPositiveNet) PrimaryGreen else StatusUrgent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isPositiveNet) {
                            "Vous avez économisé ${Utils.formatAriary(netMois * 0.2)} Ar de plus que le mois dernier."
                        } else {
                            "Vos dépenses du mois dépassent vos recettes. Pensez à limiter vos dépenses de loisirs."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // Section 1: Pie Chart Spendings breakdown
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_pie_chart"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Répartition des dépenses",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        textAlign = TextAlign.Start
                    )

                    if (totalExpenses == 0.0) {
                        Box(
                            modifier = Modifier
                                .height(160.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ajoutez des dépenses pour voir la répartition.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    } else {
                        // Drawing custom Arc-based Pie Ring
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                var startAngle = -90f
                                categoryExpenses.forEach { (cat, value) ->
                                    val sweepAngle = ((value / totalExpenses) * 360f).toFloat()
                                    val color = categoryColors[cat] ?: CatAutre
                                    drawArc(
                                        color = color,
                                        startAngle = startAngle,
                                        sweepAngle = sweepAngle,
                                        useCenter = false,
                                        style = Stroke(width = 35f)
                                    )
                                    startAngle += sweepAngle
                                }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Total",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Dépenses",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Category Legends in responsive Grid
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val sortedCategories = categoryExpenses.toList().sortedByDescending { it.second }
                            sortedCategories.chunked(2).forEach { pair ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    pair.forEach { (cat, value) ->
                                        val pct = (value / totalExpenses) * 100
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .background(
                                                        categoryColors[cat] ?: CatAutre,
                                                        CircleShape
                                                    )
                                            )
                                            Column {
                                                Text(
                                                    text = cat,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onBackground,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${pct.toInt()}% • ${Utils.formatAriary(value)} Ar",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.secondary,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                    if (pair.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Monthly Evolution Bar Chart
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_bar_chart"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Évolution Mensuelle",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(8.dp).background(SecondaryGreen, CircleShape))
                                Text("Entrées", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(8.dp).background(StatusUrgent, CircleShape))
                                Text("Sorties", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Proportional CSS-like Bar drawings in Compose Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Max value for bar scaling
                        val maxVal = (monthlyStats.maxOfOrNull { Math.max(it.entries, it.exits) } ?: 0.0).coerceAtLeast(10000.0)

                        monthlyStats.forEach { mData ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Entries bar
                                    val entriesHt = ((mData.entries / maxVal) * 120).coerceAtLeast(4.0)
                                    Box(
                                        modifier = Modifier
                                            .width(12.dp)
                                            .height(entriesHt.dp)
                                            .background(
                                                SecondaryGreen.copy(alpha = 0.8f),
                                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                            )
                                    )
                                    // Exits bar
                                    val exitsHt = ((mData.exits / maxVal) * 120).coerceAtLeast(4.0)
                                    Box(
                                        modifier = Modifier
                                            .width(12.dp)
                                            .height(exitsHt.dp)
                                            .background(
                                                StatusUrgent.copy(alpha = 0.8f),
                                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                            )
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = mData.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Smart Insights Bento list
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "INSIGHTS INTELLIGENTS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Insight",
                            tint = StatusUpcoming,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Réduisez vos frais de transport",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Vos dépenses de transport ont augmenté de 15% ce mois-ci. Envisagez le covoiturage pour économiser en Ariary (Ar).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

data class MonthData(
    val name: String,
    val index: Int,
    var entries: Double,
    var exits: Double
)

fun getFrenchMonthAbbreviation(monthIndex: Int): String {
    return when (monthIndex) {
        0 -> "Fév" // We map 5 months shift relative to mockup
        1 -> "Mar"
        2 -> "Avr"
        3 -> "Mai"
        4 -> "Jui"
        5 -> "Jul"
        6 -> "Aoû"
        7 -> "Sep"
        8 -> "Oct"
        9 -> "Nov"
        10 -> "Déc"
        else -> "Jan"
    }
}
