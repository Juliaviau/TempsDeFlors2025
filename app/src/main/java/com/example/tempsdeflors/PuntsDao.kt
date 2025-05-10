package com.example.tempsdeflors


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PuntsDao {
    @Query("SELECT * FROM punts ORDER BY numero DESC")
    fun getAllPunts(): List<PuntsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPunts(apunts: PuntsEntity)

    @Query("SELECT * FROM punts WHERE numero = :id")
    fun getPuntsById(id: Int): PuntsEntity?

    @Delete
    fun delete(somni: PuntsEntity)

    @Query("DELETE FROM punts WHERE numero = :numero")
    fun deleteApuntsByNumero(numero: String)

}