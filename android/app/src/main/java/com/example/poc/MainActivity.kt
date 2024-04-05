package com.example.poc

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.poc.ui.theme.PoCTheme
import com.facebook.hermes.reactexecutor.HermesExecutorFactory
import com.facebook.react.BuildConfig
import com.facebook.react.ReactInstanceManager
import com.facebook.react.ReactPackage
import com.facebook.react.ReactRootView
import com.facebook.react.PackageList
import com.facebook.react.common.LifecycleState
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler
import com.facebook.soloader.SoLoader

class NotesStore(initialNotes: List<Note>) {
    var notes = initialNotes
        set(updatedNotes: List<Note>) {
            field = updatedNotes
            notifySubscribers()
        }


    fun subscribe(subscriber: (notes: List<Note>) -> Unit) {
        subscribers.add(subscriber)
    }

    fun unsubscribe(subscriber: (notes: List<Note>) -> Unit) {
        subscribers.remove(subscriber)
    }

    private fun notifySubscribers() {
        subscribers.forEach {
            it(notes)
        }
    }

    private var subscribers: MutableSet<(notes: List<Note>) -> Unit> = mutableSetOf()
}

class MainActivity : ComponentActivity() {
    companion object {
        const val OVERLAY_PERMISSION_REQ_CODE = 1  // Choose any value
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package: $packageName")
                )
                startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE)
            }
        }

        val initialNotes = listOf<Note>(
            Note(id = "1", title = "First note", text = "First note text"),
            Note(id = "2", title = "Second note", text = "Second note text"),
            Note(id = "3", title = "Third note", text = "Third note text"),
        )

        val notesStore = NotesStore(initialNotes)

        val selectedNoteActivityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val intent = it.data
                val extras = intent?.extras

                val event = extras?.getString("event")

                when (event) {
                    "CLOSED" -> return@registerForActivityResult
                    "NOTE_UPDATED" -> {
                        val updatedNote = Note(
                            id = extras.getString("id")!!,
                            title = extras.getString("title")!!,
                            text = extras.getString("text")!!,
                        )

                        val updatedNotes = notesStore.notes.map { note ->
                            if (note.id == updatedNote.id) updatedNote else note
                        }

                        notesStore.notes = updatedNotes
                    }

                    else -> return@registerForActivityResult
                }
            }

        setContent {
            DisplayNotes(notesStore, onSelectNote = { it ->
                val intent = Intent(this, MyReactActivity::class.java)
                intent.putExtra("id", it.id)
                intent.putExtra("title", it.title)
                intent.putExtra("text", it.text)
                selectedNoteActivityLauncher.launch(intent)
            })
        }
    }
}

@Composable
fun DisplayNotes(
    notesStore: NotesStore,
    onSelectNote: (note: Note) -> Unit
) {

    val (notes, setNotes) = remember { mutableStateOf(notesStore.notes) }

    DisposableEffect("notes") {
        notesStore.subscribe(setNotes)

        onDispose { notesStore.unsubscribe(setNotes) }
    }

    PoCTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
        ) {
            Column {
                notes.forEach {
                    Box(modifier = Modifier
                        .padding(top = 12.dp)
                        .background(Color(0xFFFFF8DE))
                        .clickable { onSelectNote(it) }) {
                        Text(
                            modifier = Modifier
                                .padding(vertical = 20.dp, horizontal = 12.dp)
                                .fillMaxWidth(),
                            text = it.title,
                            color = Color(0xFF222222)
                        )
                    }
                }
            }
        }
    }
}


class SelectedNoteActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = intent.extras!!

        val selectedNote = Note(
            id = extras.getString("id")!!,
            title = extras.getString("title")!!,
            text = extras.getString("text")!!
        )

        val onClose = {
            val intent = Intent()
            intent.putExtra("event", "CLOSED")

            setResult(RESULT_OK, intent)
            finish()
        }

        val onUpdate = { updatedNote: Note ->
            val intent = Intent()
            intent.putExtra("event", "NOTE_UPDATED")
            intent.putExtra("id", updatedNote.id)
            intent.putExtra("title", updatedNote.title)
            intent.putExtra("text", updatedNote.text)

            setResult(RESULT_OK, intent)
            finish()
        }

        setContent {
            SelectedNote(selectedNote, onClose, onUpdate)
        }
    }
}

@Composable
fun SelectedNote(note: Note, onClose: () -> Unit, onUpdate: (updateNote: Note) -> Unit) {
    val (title, setTitle) = remember { mutableStateOf(note.title) }
    val (text, setText) = remember { mutableStateOf(note.text) }

    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            TextField(
                value = title,
                onValueChange = { value -> setTitle(value) }
            )


            Button(onClick = onClose) {
                Text(text = "X")
            }
        }

        Row {
            TextField(value = text, onValueChange = { value -> setText(value) })
        }

        Row {
            Button(onClick = { onUpdate(Note(note.id, title, text)) }) {
                Text(text = "Save")
            }
        }
    }
}

class MyReactActivity : Activity(), DefaultHardwareBackBtnHandler {
    private lateinit var reactRootView: ReactRootView
    private lateinit var reactInstanceManager: ReactInstanceManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = intent.extras

        SoLoader.init(this, false)
        reactRootView = ReactRootView(this)
        val packages: List<ReactPackage> = PackageList(application).packages.apply {
            add(AppPackage())
        }
        // Packages that cannot be autolinked yet can be added manually here, for example:
        // packages.add(MyReactNativePackage())
        // Remember to include them in `settings.gradle` and `app/build.gradle` too.
        reactInstanceManager =
            ReactInstanceManager.builder().setJavaScriptExecutorFactory(HermesExecutorFactory())
                .setApplication(application).setCurrentActivity(this)
                .setBundleAssetName("index.android.bundle").setJSMainModulePath("index")
                .addPackages(packages).setUseDeveloperSupport(BuildConfig.DEBUG)
                .setInitialLifecycleState(LifecycleState.RESUMED).build()
        // The string here (e.g. "MyReactNativeApp") has to match
        // the string in AppRegistry.registerComponent() in index.js
        reactRootView?.startReactApplication(reactInstanceManager, "MyReactNativeApp", extras)

        setContentView(reactRootView)
    }

    override fun invokeDefaultOnBackPressed() {
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        reactInstanceManager.onHostPause(this)
    }

    override fun onResume() {
        super.onResume()
        reactInstanceManager.onHostResume(this, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        reactInstanceManager.onHostDestroy(this)
        reactRootView.unmountReactApplication()
    }

    override fun onBackPressed() {
        reactInstanceManager.onBackPressed()
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == MainActivity.OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    // SYSTEM_ALERT_WINDOW permission not granted
                }
            }
        }
        reactInstanceManager?.onActivityResult(this, requestCode, resultCode, data)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU && reactInstanceManager != null) {
            reactInstanceManager.showDevOptionsDialog()
            return true
        }
        return super.onKeyUp(keyCode, event)
    }
}