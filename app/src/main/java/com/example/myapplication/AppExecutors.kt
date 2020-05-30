package com.example.myapplication

import java.util.concurrent.Executor
import java.util.concurrent.Executors

object AppExecutors {
    val diskIO: Executor = Executors.newSingleThreadExecutor()
    val dbIO: Executor =  Executors.newSingleThreadExecutor()
}