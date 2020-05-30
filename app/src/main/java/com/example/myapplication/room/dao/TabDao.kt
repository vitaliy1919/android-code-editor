package com.example.myapplication.room.dao

import androidx.room.*
import com.example.myapplication.room.entities.TabData

@Dao
interface TabDao {

    @Query("SELECT * FROM TABDATA")
    fun findAll():List<TabData>

    @Insert
    fun insertTab(tab: TabData):Long
    @Update
    fun updateTab(tab: TabData)
    @Delete
    fun deleteTab(tab: TabData)
}