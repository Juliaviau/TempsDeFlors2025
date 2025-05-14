package com.example.tempsdeflors

import android.net.Uri

interface FotoCallback {
    fun ferFoto(puntID: String, onFotoFeta: (Uri) -> Unit)
    fun onFotoFeta(puntID: String, uri: Uri)
}