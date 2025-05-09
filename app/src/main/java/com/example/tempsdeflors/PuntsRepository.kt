package com.example.tempsdeflors

import android.content.Context
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.Marker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.osmdroid.views.MapView
import java.io.File

class PuntsRepository(private val context: Context) {

    private val gson = Gson()
    private val fileName = "punts.json"

    private fun getFile(): File = File(context.filesDir, fileName)

    fun inicialitzarSiCal() {
        val file = getFile()
        if (!file.exists()) {
            context.assets.open(fileName).use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
        }
    }

    fun llegirPunts(): MutableList<Punts> {
        inicialitzarSiCal()
        val json = getFile().readText()
        val type = object : TypeToken<MutableList<Punts>>() {}.type
        return gson.fromJson(json, type)
    }

    fun guardarPunts(punts: List<Punts>) {
        val json = gson.toJson(punts)
        getFile().writeText(json)
    }

    fun marcarComVisitat(numero: String) {
        val punts = llegirPunts()
        val punt = punts.find { it.numero == numero }
        punt?.let {
            it.visitat = "si"
            guardarPunts(punts)
        }
    }

    fun marcarComNoVisitat(numero: String) {
        val punts = llegirPunts()
        val punt = punts.find { it.numero == numero }
        punt?.let {
            it.visitat = "no"
            guardarPunts(punts)
        }
    }

    fun modificarPunt(puntActualitzat: Punts) {
        val punts = llegirPunts()
        val index = punts.indexOfFirst { it.numero == puntActualitzat.numero }
        if (index != -1) {
            punts[index] = puntActualitzat
            guardarPunts(punts)
        }
    }
}
