package io.github.kroha22.tempo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.kroha22.tempo.model.decodeTempoState
import io.github.kroha22.tempo.model.encodeTempoState
import io.github.kroha22.tempo.ui.TempoApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val preferences = getSharedPreferences("tempo_learning", MODE_PRIVATE)
        val initialState = decodeTempoState(preferences.getString("progress", null))
        setContent {
            TempoApp(
                initialState = initialState,
                onStateChanged = { state ->
                    preferences.edit().putString("progress", encodeTempoState(state)).apply()
                },
            )
        }
    }
}
