package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.lang.IllegalArgumentException

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val debtDao: DebtDao
) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val allDebts: Flow<List<Debt>> = debtDao.getAllDebts()

    suspend fun insertTransaction(
        montant: Double,
        type: String,
        description: String,
        categorie: String,
        date: Long,
        moyenPaiement: String
    ) {
        val currentList = transactionDao.getAllTransactions().firstOrNull() ?: emptyList()
        val currentBalance = currentList.sumOf {
            if (it.type == "Entrée") it.montant else -it.montant
        }
        val soldeApres = currentBalance + if (type == "Entrée") montant else -montant

        val transaction = Transaction(
            montant = montant,
            type = type,
            description = description,
            categorie = categorie,
            date = date,
            moyenPaiement = moyenPaiement,
            soldeApres = soldeApres
        )
        transactionDao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun insertDebt(debt: Debt) {
        debtDao.insertDebt(debt)
    }

    suspend fun deleteDebt(debt: Debt) {
        debtDao.deleteDebt(debt)
    }

    suspend fun deleteDebtById(id: Long) {
        debtDao.deleteDebtById(id)
    }

    suspend fun deleteTransactionById(id: Long) {
        transactionDao.deleteTransactionById(id)
    }

    /**
     * Marks a debt as settled, updates its status and records the settlement date.
     * It also automatically creates the corresponding transaction in history.
     */
    suspend fun settleDebt(debtId: Long, dateReglement: Long) {
        val debt = debtDao.getDebtById(debtId) ?: return
        if (debt.reglee) return

        val finalStatut = if (debt.type == "Je dois") "Payé" else "Récupéré"
        val updatedDebt = debt.copy(
            statut = finalStatut,
            reglee = true,
            dateReglement = dateReglement
        )

        // Save updated debt status
        debtDao.insertDebt(updatedDebt)

        // Create transaction entry
        val transactionType = if (debt.type == "Je dois") "Sortie" else "Entrée"
        val transactionDescription = if (debt.type == "Je dois") {
            "Remboursement à ${debt.personne}"
        } else {
            "Remboursement de ${debt.personne}"
        }

        insertTransaction(
            montant = debt.montant,
            type = transactionType,
            description = transactionDescription,
            categorie = "Remboursement dette",
            date = dateReglement,
            moyenPaiement = "Espèces" // Default payment method
        )
    }

    /**
     * Postpones a debt with a new due date and a reason.
     */
    suspend fun postponeDebt(debtId: Long, newDueDate: Long, reason: String?) {
        val debt = debtDao.getDebtById(debtId) ?: return
        val updatedDebt = debt.copy(
            dateEcheance = newDueDate,
            nouvelleDate = newDueDate,
            statut = "Reporté",
            raisonReport = reason
        )
        debtDao.insertDebt(updatedDebt)
    }

    /**
     * Updates an existing debt.
     */
    suspend fun updateDebt(debt: Debt) {
        debtDao.insertDebt(debt)
    }
}
