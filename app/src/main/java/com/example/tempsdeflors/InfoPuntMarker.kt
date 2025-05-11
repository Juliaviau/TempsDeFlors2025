package com.example.tempsdeflors

import android.content.res.ColorStateList
import android.os.Build
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.PendingIntentCompat.getActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow

class InfoPuntMarker(private val mapView: MapView) :
    InfoWindow(R.layout.info_punt_marker, mapView) {
    val context = mapView.context
    //val database = AppDatabase.getInstance(context)
    //val repositoryEnlaces = database?.puntsDao()?.let { PuntsRepository(it) }
    //val viewmodel = AppViewModel(repositoryEnlaces!!)


    //val puntsRepo = PuntsRepository(context)

    override fun onClose() {
        // Do something
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOpen(item: Any?) {
        val marker = item as? Marker ?: return
        val punt = marker.relatedObject as? Punts ?: return

        val visitat = PuntRepository.existeixPuntByNumero(punt.numero) //true es que existeix


        val titleView = mView.findViewById<TextView>(R.id.title)
        val descView = mView.findViewById<TextView>(R.id.description)
        val subDescView = mView.findViewById<TextView>(R.id.snippet)
        val visitatButton = mView.findViewById<Button>(R.id.visitat_button)
        val ruta = mView.findViewById<TextView>(R.id.ruta)
        val visita = mView.findViewById<TextView>(R.id.visitatono)

        titleView.text = marker.title
        descView.text = marker.subDescription
        subDescView.text = marker.snippet
        visita.text = if (/*punt.visitat.equals("no")*/!visitat) "No visitat" else "Visitat a " + punt.data
        visitatButton.text = if (/*punt.visitat.equals("no")*/!visitat) "Marcar com a visitat" else "Eliminar de visitats"
        ruta.text = "RUTA " + punt.ruta

        when (punt.ruta) {
            "1" -> {
                    ruta.setTextColor(ContextCompat.getColor(mapView.context, R.color.ruta1))
                    visitatButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(mapView.context, R.color.ruta1))
                }
            "2" -> {
                    ruta.setTextColor(ContextCompat.getColor(mapView.context, R.color.ruta2))
                    visitatButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(mapView.context, R.color.ruta2))
            }
            "3" -> {
                    ruta.setTextColor(ContextCompat.getColor(mapView.context, R.color.ruta3))
                    visitatButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(mapView.context, R.color.ruta3))
                }
            "ACCESSIBLE" -> {
                    ruta.setTextColor(ContextCompat.getColor(mapView.context, R.color.accessible))
                    visitatButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(mapView.context, R.color.accessible))
                }
        }

        mapView.setOnClickListener {
            close()
        }

        //nomes guardo els que estan visitats
        visitatButton.setOnClickListener {
            // Handle visitat button click
            if (/*punt.visitat.equals("no")*/!visitat) {
               // punt.visitat = "si"
                //punt.visitat = "si"
                //puntsRepo.marcarComVisitat(punt.numero)

                CoroutineScope(Dispatchers.IO).launch {
                    PuntRepository.marcaPuntComVisitat(ruta.text.toString(), punt.numero, "si")
                    withContext(Dispatchers.Main) {
                        punt.visitat = "si"
                        val color = getColorPerRuta(punt.ruta, visitat = true)
                        marker.icon = createNumberedMarkerDrawable(mapView.context, punt.numero.toInt(), color)
                        mapView.invalidate() // refresca el mapa

                        close() // tanca la finestra
                    }
                }

                //viewmodel.marcarComVisitat(punt.ruta, punt.numero, "si")
            } else {
                //punt.visitat = "no"
                //punt.data = ""
                //puntsRepo.marcarComNoVisitat(punt.numero)
                //viewmodel.deletePuntById(punt.numero)
                CoroutineScope(Dispatchers.IO).launch {
                    PuntRepository.eliminarPunt(punt.numero)
                    withContext(Dispatchers.Main) {
                        punt.visitat = "no"
                        val color = getColorPerRuta(punt.ruta, visitat = false)
                        marker.icon = createNumberedMarkerDrawable(mapView.context, punt.numero.toInt(), color)
                        mapView.invalidate() // refresca el mapa

                        close() // tanca la finestra
                    }
                }
            }
            mapView.invalidate()
            close()
        }
        mView.setOnClickListener{
            close()
        }

        // Handle map click
        mapView.setOnClickListener { close() }

        closeAllInfoWindowsOn(mapView)

    }
    private fun getColorPerRuta(ruta: String, visitat: Boolean): Int {
        val context = mapView.context
        return when (ruta) {
            "1" -> if (visitat) ContextCompat.getColor(context, R.color.ruta1clar) else ContextCompat.getColor(context, R.color.ruta1)
            "2" -> if (visitat) ContextCompat.getColor(context, R.color.ruta2clar) else ContextCompat.getColor(context, R.color.ruta2)
            "3" -> if (visitat) ContextCompat.getColor(context, R.color.ruta3clar) else ContextCompat.getColor(context, R.color.ruta3)
            "ACCESSIBLE" -> if (visitat) ContextCompat.getColor(context, R.color.accessibleclar) else ContextCompat.getColor(context, R.color.accessible)
            else -> ContextCompat.getColor(context, R.color.white)
        }
    }

}