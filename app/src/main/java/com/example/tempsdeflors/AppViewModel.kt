package com.example.tempsdeflors


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Console
import java.time.LocalDate
import java.time.ZoneId

class AppViewModel (private val puntsRepository: PuntsRepository) : ViewModel() {
    private val _punts = MutableStateFlow<List<PuntsEntity>>(emptyList())
    val punts: StateFlow<List<PuntsEntity>> = _punts
    init {
        loadData()
    }
    private fun loadData() {
        loadpunts()

    }
    fun loadpunts() {
        viewModelScope.launch(Dispatchers.IO) {
            val allpunts = puntsRepository.getAllApunts()
            _punts.value = allpunts
        }
    }

    fun deletePuntById(numero: String) {
        viewModelScope.launch(Dispatchers.IO) {
            puntsRepository.deleteApuntsByNumero(numero)
            loadpunts() // Recargar la lista después de eliminar
        }
    }

    fun deleteApunt(apunt: PuntsEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            puntsRepository.deleteApunts(apunt)
            loadpunts() // Recargar la lista después de eliminar
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun marcarComVisitat(ruta: String, numero: String, visitat: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val timestamp = System.currentTimeMillis()

            val newApunt = PuntsEntity(
                ruta = ruta,
                data = timestamp.toString(),
                numero = numero,
                visitat = visitat

            )

            puntsRepository.insertApunts(newApunt)
            loadpunts()
        }
    }

    private val _puntsVisitats = MutableStateFlow<Set<String>>(emptySet())
    val puntsVisitats: StateFlow<Set<String>> get() = _puntsVisitats

    fun carregarPuntsVisitats() {
        viewModelScope.launch(Dispatchers.IO) {
            val punts = puntsRepository.getAllApunts() // Retorna List<Punt>
            _puntsVisitats.value = punts.map { it.numero }.toSet()
        }
    }



    /*fun existeixPuntByNumero(numero: String): Boolean {
        var result = true
        viewModelScope.launch(Dispatchers.IO) {
            result = puntsRepository.existeixPuntByNumero(numero)
        }
        println("existeixPuntByNumero: $result")
        return result
    }*/

    var ap : PuntsEntity? = null
    fun getApuntByIdAsync(somniId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = puntsRepository.getApuntsById(somniId)
            ap = result
        }
    }
}