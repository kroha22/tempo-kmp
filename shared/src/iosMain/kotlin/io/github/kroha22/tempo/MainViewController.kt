package io.github.kroha22.tempo

import androidx.compose.ui.window.ComposeUIViewController
import io.github.kroha22.tempo.model.decodeTempoState
import io.github.kroha22.tempo.model.encodeTempoState
import io.github.kroha22.tempo.ui.TempoApp
import platform.Foundation.NSUserDefaults

fun MainViewController() = ComposeUIViewController {
    val defaults = NSUserDefaults.standardUserDefaults
    TempoApp(
        initialState = decodeTempoState(defaults.stringForKey("tempo.learning.progress")),
        onStateChanged = { state ->
            defaults.setObject(encodeTempoState(state), forKey = "tempo.learning.progress")
        },
    )
}
