package com.example.poc

import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class UpdateNoteModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {
    override fun getName() = "UpdateNoteModule"

    @ReactMethod
    fun updateNote(id: String, updatedTitle: String, updatedText: String) {
        val activity = reactContext.currentActivity
        val intent = Intent()
        intent.putExtra("event", "NOTE_UPDATED")
        intent.putExtra("id", id)
        intent.putExtra("title", updatedTitle)
        intent.putExtra("text", updatedText)

        activity!!.setResult(ComponentActivity.RESULT_OK, intent)
        activity!!.finish()

    }
}