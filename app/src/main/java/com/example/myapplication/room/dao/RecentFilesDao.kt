package com.example.myapplication.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.myapplication.room.entities.RecentFile
import com.example.myapplication.room.entities.TabData

@Dao
interface RecentFilesDao {
    @Query("select * from RecentFile")
    fun findAllLiveData():LiveData<List<RecentFile>>

    @Query("select * from RecentFile")
    fun findAll():List<RecentFile>
    @Insert
    fun insert(file: RecentFile)

    @Delete
    fun remove(file: RecentFile)

    @Query("delete from RecentFile")
    fun removeAll()
}
