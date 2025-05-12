package com.example.tempsdeflors

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object PuntRepository {

    private lateinit var database: AppDatabase
    private val puntsVisitats = mutableListOf<PuntsEntity>()

    fun init(context: Context) {
        database = AppDatabase.getInstance(context) ?: throw IllegalStateException("No DB")
        carregarPuntsVisitats()
    }

    fun getPuntByNumero(numero: String): PuntsEntity? {
        return synchronized(puntsVisitats) {
            puntsVisitats.find { it.numero == numero }
        }
    }

    fun getDataByNumero(numero: String): String? {
        return synchronized(puntsVisitats) {
            puntsVisitats.find { it.numero == numero }?.data
        }
    }

    private fun carregarPuntsVisitats() {
        CoroutineScope(Dispatchers.IO).launch {
            val punts = database.puntsDao()?.getAllPunts() ?: mutableListOf()
            synchronized(puntsVisitats) {
                puntsVisitats.clear()
                puntsVisitats.addAll(punts.filter { it.visitat == "si" })
            }
        }
    }

    fun existeixPuntByNumero(numero: String): Boolean {
        return synchronized(puntsVisitats) {
            puntsVisitats.any { it.numero == numero }
        }
    }

    suspend fun marcaPuntComVisitat(ruta: String, numero: String, visitat: String) {
        withContext(Dispatchers.IO) {
            val timestamp = System.currentTimeMillis()
            val punt = PuntsEntity(
                ruta = ruta,
                data = timestamp.toString(),
                numero = numero,
                visitat = visitat
            )
            database.puntsDao()?.insertPunts(punt.copy(visitat = "si"))
            synchronized(puntsVisitats) {
                if (puntsVisitats.none { it.numero == punt.numero }) {
                    puntsVisitats.add(punt.copy(visitat = "si"))
                }
            }
        }
    }

    fun eliminarPunt(punt: String) {
        CoroutineScope(Dispatchers.IO).launch {
            database.puntsDao()?.deleteApuntsByNumero(punt)
            synchronized(puntsVisitats) {
                puntsVisitats.remove(getPuntByNumero(punt))
            }
        }
    }

    fun getPuntsVisitats(): List<PuntsEntity> {
        return synchronized(puntsVisitats) {
            puntsVisitats.toList()
        }
    }
}
