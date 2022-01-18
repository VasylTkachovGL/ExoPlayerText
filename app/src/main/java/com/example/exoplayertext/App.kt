package com.example.exoplayertext

import android.app.Application


/*
* @author Tkachov Vasyl
* @since 17.01.2022
*/
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        if (jniRepository == null) {
            jniRepository = JniRepository()
        }
    }

    companion object {
        var jniRepository: JniRepository? = null
    }
}