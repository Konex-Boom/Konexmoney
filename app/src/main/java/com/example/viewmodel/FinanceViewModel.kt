package com.example.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Debt
import com.example.data.FinanceRepository
import com.example.data.Transaction
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class UserProfile(
    val name: String,
    val phone: String,
    val email: String,
    val birthDate: String,
    val imageUri: String?
)

class FinanceViewModel(
    private val repository: FinanceRepository,
    private val prefs: SharedPreferences
) : ViewModel() {

    // Active screen navigation state
    private val _currentScreen = MutableStateFlow(Screen.Accueil)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    // User Profile State
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        val completed = prefs.getBoolean("profile_completed", false)
        if (completed) {
            _userProfile.value = UserProfile(
                name = prefs.getString("profile_name", "") ?: "",
                phone = prefs.getString("profile_phone", "") ?: "",
                email = prefs.getString("profile_email", "") ?: "",
                birthDate = prefs.getString("profile_birth_date", "") ?: "",
                imageUri = prefs.getString("profile_image_uri", null)
            )
        } else {
            _userProfile.value = null
        }
    }

    fun saveUserProfile(name: String, phone: String, email: String, birthDate: String, imageUri: String?) {
        prefs.edit().apply {
            putString("profile_name", name)
            putString("profile_phone", phone)
            putString("profile_email", email)
            putString("profile_birth_date", birthDate)
            putString("profile_image_uri", imageUri)
            putBoolean("profile_completed", true)
            apply()
        }
        _userProfile.value = UserProfile(name, phone, email, birthDate, imageUri)
    }

    fun clearUserProfile() {
        prefs.edit().apply {
            clear()
            apply()
        }
        _userProfile.value = null
    }

    // Raw sources from DB
    val allTransactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDebts: StateFlow<List<Debt>> = repository.allDebts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filter states for transaction history
    private val _typeFilter = MutableStateFlow("Tout") // "Tout", "Entrée", "Sortie"
    val typeFilter: StateFlow<String> = _typeFilter.asStateFlow()

    private val _categoryFilter = MutableStateFlow("Tout") // "Tout", categories...
    val categoryFilter: StateFlow<String> = _categoryFilter.asStateFlow()

    private val _periodFilter = MutableStateFlow("Tout") // "Tout", "Mois en cours", "Année en cours"
    val periodFilter: StateFlow<String> = _periodFilter.asStateFlow()

    fun setFilters(type: String, category: String, period: String) {
        _typeFilter.value = type
        _categoryFilter.value = category
        _periodFilter.value = period
    }

    // Filtered transaction history
    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        allTransactions, _typeFilter, _categoryFilter, _periodFilter
    ) { txList, type, cat, period ->
        var result = txList
        if (type != "Tout") {
            result = result.filter { it.type == type }
        }
        if (cat != "Tout") {
            result = result.filter { it.categorie == cat }
        }
        if (period != "Tout") {
            val startLimit = when (period) {
                "Mois en cours" -> getStartOfMonthTimestamp()
                "Année en cours" -> getStartOfYearTimestamp()
                else -> 0L
            }
            result = result.filter { it.date >= startLimit }
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Financial calculations
    val soldeActuel: StateFlow<Double> = allTransactions.map { list ->
        list.sumOf { if (it.type == "Entrée") it.montant else -it.montant }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalEntreesMois: StateFlow<Double> = allTransactions.map { list ->
        val startOfMonth = getStartOfMonthTimestamp()
        list.filter { it.type == "Entrée" && it.date >= startOfMonth }.sumOf { it.montant }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalSortiesMois: StateFlow<Double> = allTransactions.map { list ->
        val startOfMonth = getStartOfMonthTimestamp()
        list.filter { it.type == "Sortie" && it.date >= startOfMonth }.sumOf { it.montant }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Debts classification
    val dettesActives: StateFlow<List<Debt>> = allDebts.map { list ->
        list.filter { !it.reglee }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dettesReglees: StateFlow<List<Debt>> = allDebts.map { list ->
        list.filter { it.reglee }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Suggestions & Intelligent Alerts
    val remboursementsSuggere: StateFlow<Debt?> = combine(soldeActuel, dettesActives) { solde, debts ->
        debts.firstOrNull { it.type == "Je dois" && solde >= it.montant }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Database manipulation functions
    fun addTransaction(montant: Double, type: String, description: String, categorie: String, date: Long, moyenPaiement: String) {
        viewModelScope.launch {
            repository.insertTransaction(montant, type, description, categorie, date, moyenPaiement)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun addDebt(personne: String, type: String, montant: Double, dateEmprunt: Long, dateEcheance: Long, notes: String?) {
        viewModelScope.launch {
            val debt = Debt(
                personne = personne,
                type = type,
                montant = montant,
                dateEmprunt = dateEmprunt,
                dateEcheance = dateEcheance,
                statut = "En attente",
                notes = notes
            )
            repository.insertDebt(debt)
        }
    }

    fun settleDebt(debtId: Long, dateReglement: Long) {
        viewModelScope.launch {
            repository.settleDebt(debtId, dateReglement)
        }
    }

    fun postponeDebt(debtId: Long, newDueDate: Long, reason: String?) {
        viewModelScope.launch {
            repository.postponeDebt(debtId, newDueDate, reason)
        }
    }

    fun updateDebt(debt: Debt) {
        viewModelScope.launch {
            repository.updateDebt(debt)
        }
    }

    fun deleteDebt(debt: Debt) {
        viewModelScope.launch {
            repository.deleteDebt(debt)
        }
    }

    // Helpers
    private fun getStartOfMonthTimestamp(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getStartOfYearTimestamp(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, Calendar.JANUARY)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}

enum class Screen {
    Accueil, Transactions, Dettes, Statistiques
}

class FinanceViewModelFactory(
    private val repository: FinanceRepository,
    private val prefs: SharedPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinanceViewModel(repository, prefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
