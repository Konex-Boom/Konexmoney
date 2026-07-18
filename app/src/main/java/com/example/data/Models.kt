package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val montant: Double,
    val type: String, // "Entrée" or "Sortie"
    val description: String,
    val categorie: String, // "Salaire", "Alimentation", "Transport", "Loisirs", "Santé", "Remboursement dette", "Autre"
    val date: Long, // timestamp
    val moyenPaiement: String, // "Espèces", "Mobile Money", "Carte", "Virement"
    val soldeApres: Double
)

@Entity(tableName = "dettes")
data class Debt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personne: String,
    val type: String, // "Je dois" or "On me doit"
    val montant: Double,
    val dateEmprunt: Long, // timestamp
    val dateEcheance: Long, // timestamp
    val statut: String, // "En attente", "Payé", "Récupéré", "Reporté"
    val reglee: Boolean = false,
    val dateReglement: Long? = null,
    val nouvelleDate: Long? = null,
    val raisonReport: String? = null,
    val notes: String? = null
)
