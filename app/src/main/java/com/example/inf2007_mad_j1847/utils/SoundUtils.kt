package com.example.inf2007_mad_j1847.utils

import android.content.Context
import android.media.MediaPlayer
import com.example.inf2007_mad_j1847.R

/**
 * Utility object for playing sound effects throughout the app.
 * Handles success and error sounds for feedback during user interactions.
 */

object SoundUtils {
    private var mediaPlayer: MediaPlayer? = null // MediaPlayer instance reused to avoid memory leaks

    /**
     * Plays a predefined success sound.
     * Make sure 'success.mp3' exists in the res/raw folder.
     */

    fun playSuccessSound(context: Context) {
        playSound(context, R.raw.success)
    }

    /**
     * Plays a predefined error sound.
     * Make sure 'error.mp3' exists in the res/raw folder.
     */

    fun playErrorSound(context: Context) {
        playSound(context, R.raw.error)
    }

    /**
     * General method to play any raw sound file.
     * It ensures the previous MediaPlayer instance is released before starting a new one.
     */
    private fun playSound(context: Context, soundResId: Int) {
        mediaPlayer?.release()  // Avoid memory leaks by releasing any existing player
        mediaPlayer = MediaPlayer.create(context, soundResId)
        mediaPlayer?.start()
    }
}