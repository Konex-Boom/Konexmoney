package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.FinanceRepository
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DebtsScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.KonexMoneyTheme
import com.example.ui.theme.PrimaryContainerGreen
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SecondaryGreen
import com.example.viewmodel.FinanceViewModel
import com.example.viewmodel.FinanceViewModelFactory
import com.example.viewmodel.Screen

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KonexMoneyTheme {
                val context = LocalContext.current

                // Instantiate Room Database and Repository
                val database = remember { AppDatabase.getDatabase(context) }
                val repository = remember { FinanceRepository(database.transactionDao(), database.debtDao()) }
                val viewModel: FinanceViewModel = viewModel(factory = FinanceViewModelFactory(repository))

                val currentScreen by viewModel.currentScreen.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Column(modifier = Modifier.padding(start = 4.dp)) {
                                    Text(
                                        text = "BONJOUR, USER",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "KonexMoney",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = { /* Notification action */ },
                                    modifier = Modifier.testTag("btn_notifications")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notifications",
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .padding(end = 16.dp, start = 4.dp)
                                        .size(42.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                        .clip(CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "KM",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    },
                    bottomBar = {
                        // Bottom Navigation Bar with exact safe navigation bar padding
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp,
                            modifier = Modifier
                                .navigationBarsPadding()
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        ) {
                            // Tab 1: Accueil
                            NavigationBarItem(
                                selected = currentScreen == Screen.Accueil,
                                onClick = { viewModel.navigateTo(Screen.Accueil) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = "Accueil"
                                    )
                                },
                                label = { Text("Accueil") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.secondary,
                                    unselectedTextColor = MaterialTheme.colorScheme.secondary
                                ),
                                modifier = Modifier.testTag("nav_tab_accueil")
                            )

                            // Tab 2: Transactions
                            NavigationBarItem(
                                selected = currentScreen == Screen.Transactions,
                                onClick = { viewModel.navigateTo(Screen.Transactions) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = "Transactions"
                                    )
                                },
                                label = { Text("Transactions") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.secondary,
                                    unselectedTextColor = MaterialTheme.colorScheme.secondary
                                ),
                                modifier = Modifier.testTag("nav_tab_transactions")
                            )

                            // Tab 3: Dettes
                            NavigationBarItem(
                                selected = currentScreen == Screen.Dettes,
                                onClick = { viewModel.navigateTo(Screen.Dettes) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.PriorityHigh,
                                        contentDescription = "Dettes"
                                    )
                                },
                                label = { Text("Dettes") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.secondary,
                                    unselectedTextColor = MaterialTheme.colorScheme.secondary
                                ),
                                modifier = Modifier.testTag("nav_tab_dettes")
                            )

                            // Tab 4: Statistiques
                            NavigationBarItem(
                                selected = currentScreen == Screen.Statistiques,
                                onClick = { viewModel.navigateTo(Screen.Statistiques) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Insights,
                                        contentDescription = "Statistiques"
                                    )
                                },
                                label = { Text("Statistiques") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.secondary,
                                    unselectedTextColor = MaterialTheme.colorScheme.secondary
                                ),
                                modifier = Modifier.testTag("nav_tab_statistiques")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            Screen.Accueil -> {
                                DashboardScreen(
                                    viewModel = viewModel,
                                    onSettleDebt = { debtId ->
                                        viewModel.settleDebt(debtId, System.currentTimeMillis())
                                    }
                                )
                            }
                            Screen.Transactions -> {
                                TransactionsScreen(viewModel = viewModel)
                            }
                            Screen.Dettes -> {
                                DebtsScreen(viewModel = viewModel)
                            }
                            Screen.Statistiques -> {
                                StatisticsScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
