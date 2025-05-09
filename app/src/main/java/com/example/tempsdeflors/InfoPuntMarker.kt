package com.example.tempsdeflors

import android.content.res.ColorStateList
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow


/*
class InfoPuntMarker(mapView: MapView?) : MarkerInfoWindow(R.layout.splash_screen, mapView) {
    override fun onOpen(item: Any) {
        val m: Marker = item as Marker


        val title = mView.findViewById<View>(R.id.title) as TextView
        title.setText(m.getTitle())


        val snippet = mView
            .findViewById<View>(R.id.description) as TextView
        snippet.setText(m.getSnippet())
    }
}*/

/*
class InfoPuntMarker(layoutResId: Int, mapView: MapView?) :
    InfoWindow(layoutResId, mapView) {
    override fun onClose() {
    }

    override fun onOpen(arg0: Any) {
        val marker = arg0 as? Marker ?: return

     //   val layout = mView.findViewById<View>(R.id.layout) as LinearLayout
        //val btnMoreInfo = mView.findViewById<View>(R.id.bubble_moreinfo) as Button
        val txtTitle = mView.findViewById<View>(R.id.title) as TextView
        val txtDescription = mView.findViewById<View>(R.id.description) as TextView
        val txtSubdescription = mView.findViewById<View>(R.id.subdescription) as TextView

        txtTitle.text = marker.title
        txtDescription.text = marker.subDescription
        txtSubdescription.text = marker.snippet
        /*layout.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                // Override Marker's onClick behaviour here
            }
        })*/
    }
}*/
/*
class InfoPuntMarker(mapView: MapView) : MarkerInfoWindow(R.layout.info_punt_marker, mapView) {
    override fun onOpen(item: Any?) {
        super.onOpen(item)
        val marker = item as? Marker ?: return

        val titleView = mView.findViewById<TextView>(R.id.title)
        val descView = mView.findViewById<TextView>(R.id.description)

        titleView.text = marker.title
        descView.text = marker.snippet
    }
}*/

class InfoPuntMarker(private val mapView: MapView) :
    InfoWindow(R.layout.info_punt_marker, mapView) {

    override fun onClose() {
        // Do something
    }

    override fun onOpen(item: Any?) {
        val marker = item as? Marker ?: return
        val punt = marker.relatedObject as? Punts ?: return

        val titleView = mView.findViewById<TextView>(R.id.title)
        val descView = mView.findViewById<TextView>(R.id.description)
        val subDescView = mView.findViewById<TextView>(R.id.snippet)
        val visitatButton = mView.findViewById<Button>(R.id.visitat_button)
        val ruta = mView.findViewById<TextView>(R.id.ruta)
        val visita = mView.findViewById<TextView>(R.id.visitatono)

        titleView.text = marker.title
        descView.text = marker.subDescription
        subDescView.text = marker.snippet
        visita.text = if (punt.visitat.equals("no")) "No visitat" else "Visitat a " + punt.data
        visitatButton.text = if (punt.visitat.equals("no")) "Marcar com a visitat" else "Eliminar de visitats"
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
        visitatButton.setOnClickListener {
            // Handle visitat button click
            close()
        }
        mView.setOnClickListener{
            close()
        }

        // Handle map click



        closeAllInfoWindowsOn(mapView)

    }
}