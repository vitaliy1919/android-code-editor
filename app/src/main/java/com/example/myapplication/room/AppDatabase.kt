package com.example.myapplication.room

import android.content.Context
import androidx.room.*
import com.example.myapplication.room.dao.TabDao
import com.example.myapplication.room.entities.TabData

//abstract class Database: RoomDatabase() {
//    private object HOLDER {
//        val INSTANCE = Singleton()
//    }
//    companion object {
//        val instance: Database by lazy { HOLDER.INSTANCE }
//    }
//}
private var instance: AppDatabase? = null
private val dbName = "app_db"
@Synchronized
private fun createInstance(context: Context) {
    if (instance == null) {
        instance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, dbName).fallbackToDestructiveMigration().build()
    }
}
@Synchronized
fun getInstance(context: Context): AppDatabase? {
    if (instance == null) createInstance(context)
    return instance
}

@Database(entities = [TabData::class],exportSchema = false, version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {


    abstract fun tabDao(): TabDao;
}