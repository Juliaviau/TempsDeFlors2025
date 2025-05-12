package com.example.tempsdeflors

import android.net.Uri

interface FotoCallback {
    fun ferFoto(puntID: String)
    fun onFotoFeta(puntID: String, uri: Uri)
}