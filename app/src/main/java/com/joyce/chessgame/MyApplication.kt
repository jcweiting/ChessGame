package com.joyce.chessgame

import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger

class MyApplication: Application() {

    private lateinit var mediaPlayer: MediaPlayer
    private var isTurnOnMusic = false

    companion object {
        lateinit var instance: MyApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        //Init FB Login
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)

        //InitMusic
        mediaPlayer = MediaPlayer.create(this, R.raw.bg_music)
        mediaPlayer.isLooping = true
    }

    fun getContext(): Context{
        return applicationContext
    }

    fun turnOnBgMusic(isTurnOn: Boolean){
        when(isTurnOn){
            true -> {
                mediaPlayer.start()
            }
            false -> {
                mediaPlayer.pause()
                mediaPlayer.seekTo(0)
            }
        }
        isTurnOnMusic = isTurnOn
    }
}