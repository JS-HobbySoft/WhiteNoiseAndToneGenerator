@file:OptIn(ExperimentalMaterial3Api::class)

package org.jshobbysoft.whitenoiseandtonegenerator

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jshobbysoft.whitenoiseandtonegenerator.ui.theme.WhiteNoiseAndToneGeneratorTheme
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    private val fs: Int = 44100
    private var isPlaying = false
    private val scope = CoroutineScope(Job() + Dispatchers.Main)

    private val buffLength = AudioTrack.getMinBufferSize(
        fs,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    private val trackAttrib = AudioAttributes.Builder()
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .build()

    private val trackFormat = AudioFormat.Builder()
        .setSampleRate(fs)
        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
        .build()

    private val noiseTrack = AudioTrack(
        trackAttrib,
        trackFormat,
        buffLength, AudioTrack.MODE_STREAM,
        AudioManager.AUDIO_SESSION_ID_GENERATE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WhiteNoiseAndToneGeneratorTheme {
//              https://stackoverflow.com/questions/72926359/show-snackbar-in-material-design-3-using-scaffold
                val snackBarHostState = remember { SnackbarHostState() }
                Scaffold(
                    snackbarHost = { SnackbarHost(snackBarHostState) },
                    content = { contentPadding ->
//                      https://medium.com/jetpack-composers/what-does-the-paddingvalues-parameter-in-a-compose-scaffold-do-3bd5592b9c6b
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(contentPadding)
                        )

                        // A surface container using the 'background' color from the theme
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(all = 40.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val initToneFreq =
                                    DataStoreManager(LocalContext.current).getFromDataStoreFT()
                                var numInputFreq by remember { mutableStateOf(initToneFreq.toString()) }
                                TextField(
                                    value = numInputFreq,
                                    onValueChange = { numInputFreq = it },
                                    label = { Text("Enter tone frequency\n(20-20000)") }
                                )

                                val initWaveAmp =
                                    DataStoreManager(LocalContext.current).getFromDataStoreWA()
                                var numInputAmpAmp by remember { mutableStateOf(initWaveAmp.toString()) }
                                TextField(
                                    value = numInputAmpAmp,
                                    onValueChange = { numInputAmpAmp = it },
                                    label = { Text("Enter volume wave effect amplitude\n(0-32767, 0 to disable)") }
                                )

                                val initWaveFreq =
                                    DataStoreManager(LocalContext.current).getFromDataStoreWF()
                                var numInputAmpFreq by remember { mutableStateOf(initWaveFreq.toString()) }
                                TextField(
                                    value = numInputAmpFreq,
                                    onValueChange = { numInputAmpFreq = it },
                                    label = { Text("Enter volume wave effect frequency\n(0-5, 0 to disable)") }
                                )

//                              https://stackoverflow.com/questions/74248340/changing-the-jetpack-compose-remember-variable-from-within-another-function
//                              https://stackoverflow.com/questions/67111020/exposed-drop-down-menu-for-jetpack-compose
                                val options = listOf("White", "Pink", "Brownian")
                                var expanded by remember { mutableStateOf(false) }
                                val initNoiseType =
                                    DataStoreManager(LocalContext.current).getFromDataStoreNT()
                                var noiseType by remember { mutableStateOf(initNoiseType) }

                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = {
                                        expanded = !expanded
                                    }
                                ) {
                                    TextField(
                                        readOnly = true,
                                        value = noiseType,
                                        onValueChange = { },
                                        label = { Text("Select noise type") },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(
                                                expanded = expanded
                                            )
                                        },
                                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                                        modifier = Modifier.menuAnchor()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = {
                                            expanded = false
                                        }
                                    ) {
                                        options.forEach { selectionOption ->
                                            DropdownMenuItem(
                                                text = { Text(text = selectionOption) },
                                                onClick = {
                                                    noiseType = selectionOption
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.size(10.dp))
                                Text(text = "Percent of noise in the tone\n(0 for pure tone, 100 for pure noise)")

                                val initNoisePct =
                                    DataStoreManager(LocalContext.current).getFromDataStoreNP()
                                var noisePct by remember {
                                    mutableStateOf(
                                        initNoisePct.toString().toFloat()
                                    )
                                }
                                Text(text = noisePct.toInt().toString())
                                Slider(
                                    value = noisePct,
                                    onValueChange = { noisePct = it },
                                    valueRange = 0f..100f,
                                    steps = 100
                                )

                                Row(
                                    modifier = Modifier
                                        .padding(all = 10.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
//                                  https://www.geeksforgeeks.org/play-audio-in-android-using-jetpack-compose/
//                                  https://stackoverflow.com/questions/72926359/show-snackbar-in-material-design-3-using-scaffold
                                    IconButton(onClick = {
                                        if (!isPlaying) {
                                            if (numInputFreq.toFloat() > 20000 || numInputFreq.toFloat() < 20) {
                                                scope.launch {
                                                    snackBarHostState.showSnackbar("Frequency must be between 20-20000")
                                                }
                                            } else if (numInputAmpAmp.toFloat() > 32767 || numInputAmpAmp.toFloat() < 0) {
                                                scope.launch {
                                                    snackBarHostState.showSnackbar("Wave effect amplitude value must be between 0-32767")
                                                }
                                            } else if (numInputAmpFreq.toFloat() > 5 || numInputAmpFreq.toFloat() < 0) {
                                                scope.launch {
                                                    snackBarHostState.showSnackbar("Wave effect frequency value must be between 0-5")
                                                }
                                            } else if (noisePct > 100 || noisePct < 0) {
                                                scope.launch {
                                                    snackBarHostState.showSnackbar("Noise percent must be between 0-100")
                                                }
                                            } else {
                                                scope.launch {
                                                    startPlaying()
                                                    DataStoreManager(baseContext).saveToDataStore(
                                                        numInputFreq.toFloat(),
                                                        numInputAmpAmp.toFloat(),
                                                        numInputAmpFreq.toFloat(),
                                                        noisePct,
                                                        noiseType
                                                    )
                                                    playback(
                                                        numInputFreq.toFloat(),
                                                        numInputAmpAmp.toFloat(),
                                                        numInputAmpFreq.toFloat(),
                                                        noisePct,
                                                        noiseType
                                                    )
                                                }
                                            }
                                        }
                                    }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.baseline_play_arrow_24),
                                            contentDescription = "",
                                            Modifier
                                                .size(100.dp)
                                                .background(Color.Green)
                                        )
                                    }
                                    IconButton(onClick = { stopPlaying() }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.baseline_pause_24),
                                            contentDescription = "",
                                            Modifier
                                                .size(100.dp)
                                                .background(Color.Red)
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        noiseTrack.release()
        scope.cancel()
    }

    //  https://rajat-r-bapuri.github.io/DSP-Lab-Android-Demos/Android_Demos/kotlin_implementations/Sine_Wave_Demo1/
//  https://stackoverflow.com/questions/26963342/generating-colors-of-noise-in-java
    private suspend fun playback(
        frequencyTone: Float,
        amplitudeDelta: Float,
        amplitudeFrequency: Float,
        noisePct: Float,
        noiseType: String
    ) =
        withContext(Dispatchers.IO) {
            // simple sine wave generator
            val frameOut = ShortArray(buffLength)
            val amplitudeMax = 32767
            val twoPi = 2 * PI
            var phaseTone = 0.0
            var phaseAmplitude = 0.0

            var b0 = 0.0
            var b1 = 0.0
            var b2 = 0.0
            var b3 = 0.0
            var b4 = 0.0
            var b5 = 0.0
            var b6 = 0.0
            var lastOut = 0.0

            while (isPlaying) {
                for (i in 0 until buffLength) {
                    val amplitude = (amplitudeMax
                            + 0.5 * amplitudeDelta * (sin(phaseAmplitude) - 1)).toInt()
                    val noiseGaussian = Random.nextFloat()
                    val noiseOutput = when (noiseType) {
                        "White" -> {
                            noiseGaussian
                        }

                        "Pink" -> {
                            b0 = 0.99886 * b0 + noiseGaussian * 0.0555179
                            b1 = 0.99332 * b1 + noiseGaussian * 0.0750759
                            b2 = 0.96900 * b2 + noiseGaussian * 0.1538520
                            b3 = 0.86650 * b3 + noiseGaussian * 0.3104856
                            b4 = 0.55000 * b4 + noiseGaussian * 0.5329522
                            b5 = -0.7616 * b5 - noiseGaussian * 0.0168980
                            var output = b0 + b1 + b2 + b3 + b4 + b5 + b6 + noiseGaussian * 0.5362
                            b6 = noiseGaussian * 0.115926
                            output /= 40  // (roughly) compensate for gain
                            output.toFloat()
                        }

                        "Brownian" -> {
                            var output = (lastOut + (0.02 * noiseGaussian)) / 1.02
                            lastOut = output
                            output *= 1.2 // (roughly) compensate for gain
                            output.toFloat()
                        }

                        else -> {
                            noiseGaussian
                        }
                    }

                    frameOut[i] =
                        (amplitude * ((1 - noisePct / 100) * sin(phaseTone) + noisePct / 100 * noiseOutput))
                            .toInt().toShort()
                    phaseTone += twoPi * frequencyTone / fs
                    phaseTone %= twoPi
                    phaseAmplitude += twoPi * amplitudeFrequency / fs
                    phaseAmplitude %= twoPi
                }
                noiseTrack.write(frameOut, 0, buffLength)
            }
        }

    private suspend fun startPlaying() =
        withContext(Dispatchers.IO) {
            noiseTrack.play()
            isPlaying = true
        }

    private fun stopPlaying() {
        if (isPlaying) {
            isPlaying = false
            noiseTrack.stop()
        }
    }
}
