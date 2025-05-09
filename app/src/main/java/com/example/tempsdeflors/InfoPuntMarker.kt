package com.example.tempsdeflors

import com.example.tempsdeflors.R
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow

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

        val titleView = mView.findViewById<TextView>(R.id.title)
        val descView = mView.findViewById<TextView>(R.id.description)
        val subDescView = mView.findViewById<TextView>(R.id.snippet)
        val visitatButton = mView.findViewById<Button>(R.id.visitat_button)
        val ruta = mView.findViewById<TextView>(R.id.ruta)

        titleView.text = marker.title
        descView.text = marker.subDescription
        subDescView.text = marker.snippet



        visitatButton.setOnClickListener {
            // Handle visitat button click
        }

        // Handle marker click
        marker.setOnMarkerClickListener { _, _ ->
            // Handle marker click
            true
        }

        // Handle map click
        mapView.setOnClickListener {
            close()
        }


        closeAllInfoWindowsOn(mapView)

        mapView.setOnClickListener {
            close()
        }
    }
}