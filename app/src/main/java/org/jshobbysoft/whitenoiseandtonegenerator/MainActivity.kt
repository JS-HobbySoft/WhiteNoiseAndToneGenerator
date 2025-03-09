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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jshobbysoft.whitenoiseandtonegenerator.ui.theme.WhiteNoiseAndToneGeneratorTheme
import java.lang.Exception
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
                                var t0 by remember { mutableStateOf("0") }
                                var t1 by remember { mutableStateOf("0") }
                                var t2 by remember { mutableStateOf("0") }
                                var t3 by remember { mutableStateOf("0") }
                                var t4 by remember { mutableStateOf("0") }
                                val plusMinusButtonWidth = 30.dp
                                val freqDigitWidth = 40.sp

                                val initToneFreq =
                                    DataStoreManager(LocalContext.current).getFromDataStoreFT()
                                var numInputFreq by remember { mutableStateOf(initToneFreq.toString()) }
                                val initWaveAmp =
                                    DataStoreManager(LocalContext.current).getFromDataStoreWA()
                                var numInputAmpAmp by remember { mutableStateOf(initWaveAmp.toString()) }
                                val initWaveFreq =
                                    DataStoreManager(LocalContext.current).getFromDataStoreWF()
                                var numInputAmpFreq by remember { mutableStateOf(initWaveFreq.toString()) }
                                val initNoisePct =
                                    DataStoreManager(LocalContext.current).getFromDataStoreNP()
                                val initNoiseType =
                                    DataStoreManager(LocalContext.current).getFromDataStoreNT()
                                val options = listOf("White", "Pink", "Brownian")
//                                var expanded by remember { mutableStateOf(false) }
                                var noiseType by remember { mutableStateOf(initNoiseType) }
                                var selectedIndex by remember { mutableIntStateOf(0) }
                                var noisePct by remember {
                                    mutableFloatStateOf(
                                        initNoisePct.toString().toFloat()
                                    )
                                }

                                when (numInputFreq.length) {
                                    5 -> {
                                        t0 = numInputFreq[0].toString()
                                        t1 = numInputFreq[1].toString()
                                        t2 = numInputFreq[2].toString()
                                        t3 = numInputFreq[3].toString()
                                        t4 = numInputFreq[4].toString()
                                    }
                                    4 -> {
                                        t0 = "0"
                                        t1 = numInputFreq[0].toString()
                                        t2 = numInputFreq[1].toString()
                                        t3 = numInputFreq[2].toString()
                                        t4 = numInputFreq[3].toString()
                                    }
                                    3 -> {
                                        t0 = "0"
                                        t1 = "0"
                                        t2 = numInputFreq[0].toString()
                                        t3 = numInputFreq[1].toString()
                                        t4 = numInputFreq[2].toString()
                                    }
                                    2 -> {
                                        t0 = "0"
                                        t1 = "0"
                                        t2 = "0"
                                        t3 = numInputFreq[0].toString()
                                        t4 = numInputFreq[1].toString()
                                    }
                                    1 -> {
                                        t0 = "0"
                                        t1 = "0"
                                        t2 = "0"
                                        t3 = "0"
                                        t4 = numInputFreq[0].toString()
                                    }
                                    else -> {
                                        t0 = "9"
                                        t1 = "9"
                                        t2 = "9"
                                        t3 = "9"
                                        t4 = "9"
                                    }
                                }

                                Text(
                                    text = "Enter tone frequency (20-20000)",
                                    fontSize = 20.sp
                                )

                                Row(
                                    modifier = Modifier
                                        .padding(all = 4.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
//                                    t0
                                    Column(
                                        modifier = Modifier
//                                            .fillMaxSize()
                                            .padding(all = 4.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Button(
                                            modifier = Modifier
                                                .padding(all = 1.dp)
                                                .size(size = plusMinusButtonWidth),
                                            onClick = {
                                                if (t0.toInt() < 2) {
                                                    t0 = (t0.toInt() + 1).toString()
                                                    numInputFreq = (t0+t1+t2+t3+t4)
                                                    if (isPlaying) {
                                                        restartPlaying(numInputFreq,numInputAmpAmp,numInputAmpFreq,noisePct,noiseType)
                                                    }
                                                }
                                            },
                                            shape = RoundedCornerShape(size = 8.dp),
                                            contentPadding = PaddingValues(all = 1.dp)
                                        ) {
                                            Text(
                                                text = "+",
                                                fontSize = 25.sp
                                            )
                                        }

                                        Text(
                                            text = t0,
                                            fontSize = freqDigitWidth
                                        )

                                        Button(
                                            modifier = Modifier
                                                .padding(all = 1.dp)
                                                .size(size = plusMinusButtonWidth),
                                            onClick = {
                                                if (t0.toInt() > 0){
                                                    t0 = (t0.toInt() - 1).toString()
                                                    numInputFreq = (t0+t1+t2+t3+t4)
                                                    if (isPlaying) {
                                                        restartPlaying(numInputFreq,numInputAmpAmp,numInputAmpFreq,noisePct,noiseType)
                                                    }
                                                }
                                            },
                                            shape = RoundedCornerShape(size = 8.dp),
                                            contentPadding = PaddingValues(all = 1.dp)
                                        ) {
                                            Text(
                                                text = "-",
                                                fontSize = 25.sp
                                            )
                                        }
                                    }

//                                    t1
                                    Column(
                                        modifier = Modifier
//                                            .fillMaxSize()
                                            .padding(all = 4.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Button(
                                            modifier = Modifier
                                                .padding(all = 1.dp)
                                                .size(size = plusMinusButtonWidth),
                                            onClick = {
                                                t1 = ((t1.toInt() + 1) % 10).toString()
                                                numInputFreq = (t0+t1+t2+t3+t4)
                                                if (isPlaying) {
                                                    restartPlaying(numInputFreq,numInputAmpAmp,numInputAmpFreq,noisePct,noiseType)
                                                }
                                            },
                                            shape = RoundedCornerShape(size = 8.dp),
                                            contentPadding = PaddingValues(all = 1.dp)
                                        ) {
                                            Text(
                                                text = "+",
                                                fontSize = 25.sp
                                            )
                                        }

                                        Text(
                                            text = t1,
                                            fontSize = freqDigitWidth
                                        )

                                        Button(
                                            modifier = Modifier
                                                .padding(all = 1.dp)
                                                .size(size = plusMinusButtonWidth),
                                            onClick = {
                                                t1 = if (t1 == "0") "9" else { (t1.toInt() - 1).toString() }
                                                numInputFreq = (t0+t1+t2+t3+t4)
                                                if (isPlaying) {
                                                    restartPlaying(numInputFreq,numInputAmpAmp,numInputAmpFreq,noisePct,noiseType)
                                                }
                                            },
                                            shape = RoundedCornerShape(size = 8.dp),
                                            contentPadding = PaddingValues(all = 1.dp)
                                        ) {
                                            Text(
                                                text = "-",
                                                fontSize = 25.sp
                                            )
                                        }
                                    }

//                                    t2
                                    Column(
                                        modifier = Modifier
//                                            .fillMaxSize()
                                            .padding(all = 4.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Button(
                                            modifier = Modifier
                                                .padding(all = 1.dp)
                                                .size(size = plusMinusButtonWidth),
                                            onClick = {
                                                t2 = ((t2.toInt() + 1) % 10).toString()
                                                numInputFreq = (t0+t1+t2+t3+t4)
                                                if (isPlaying) {
                                                    restartPlaying(numInputFreq,numInputAmpAmp,numInputAmpFreq,noisePct,noiseType)
                                                }
                                            },
                                            shape = RoundedCornerShape(size = 8.dp),
                                            contentPadding = PaddingValues(all = 1.dp)
                                        ) {
                                            Text(
                                                text = "+",
                                                fontSize = 25.sp
                                            )
                                        }

                                        Text(
                                            text = t2,
                                            fontSize = freqDigitWidth
                                        )

                                        Button(
                                            modifier = Modifier
                                                .padding(all = 1.dp)
                                                .size(size = plusMinusButtonWidth),
                                            onClick = {
                                                t2 = if (t2 == "0") "9" else { (t2.toInt() - 1).toString() }
                                                numInputFreq = (t0+t1+t2+t3+t4)
                                                if (isPlaying) {
                                                    restartPlaying(numInputFreq,numInputAmpAmp,numInputAmpFreq,noisePct,noiseType)
                                                }
                                            },
                                            shape = RoundedCornerShape(size = 8.dp),
                                            contentPadding = PaddingValues(all = 1.dp)
                                        ) {
                                            Text(
                                                text = "-",
                                                fontSize = 25.sp
                                            )
                                        }
                                    }

//                                    t3
                                    Column(
                                        modifier = Modifier
//                                            .fillMaxSize()
                                            .padding(all = 4.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Button(
                                            modifier = Modifier
                                                .padding(all = 1.dp)
                                                .size(size = plusMinusButtonWidth),
                                            onClick = {
                                                t3 = ((t3.toInt() + 1) % 10).toString()
                                                numInputFreq = (t0+t1+t2+t3+t4)
                                                if (isPlaying) {
                                                    restartPlaying(numInputFreq,numInputAmpAmp,numInputAmpFreq,noisePct,noiseType)
                                                }
                                            },
                                            shape = RoundedCornerShape(size = 8.dp),
                                            contentPadding = PaddingValues(all = 1.dp)
                                        ) {
                                            Text(
                                                text = "+",
                                                fontSize = 25.sp
                                            )
                                        }

                                        Text(
                                            text = t3,
                                            fontSize = freqDigitWidth
                                        )

                                        Button(
                                            modifier = Modifier
                                                .padding(all = 1.dp)
                                                .size(size = plusMinusButtonWidth),
                                            onClick = {
                                                t3 = if (t3 == "0") "9" else { (t3.toInt() - 1).toString() }
                                                numInputFreq = (t0+t1+t2+t3+t4)
                                                if (isPlaying) {
                                                    restartPlaying(numInputFreq,numInputAmpAmp,numInputAmpFreq,noisePct,noiseType)
                                                }
                                            },
                                            shape = RoundedCornerShape(size = 8.dp),
                                            contentPadding = PaddingValues(all = 1.dp)
                                        ) {
                                            Text(
                                                text = "-",
                                                fontSize = 25.sp
                                            )
                                        }
                                    }

//                                    t4
                                    Column(
                                        modifier = Modifier
//                                            .fillMaxSize()
                                            .padding(all = 4.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Button(
                                            modifier = Modifier
                                                .padding(all = 1.dp)
                                                .size(size = plusMinusButtonWidth),
                                            onClick = {
                                                t4 = ((t4.toInt() + 1) % 10).toString()
                                                numInputFreq = (t0+t1+t2+t3+t4)
                                                if (isPlaying) {
                                                    restartPlaying(numInputFreq,numInputAmpAmp,numInputAmpFreq,noisePct,noiseType)
                                                }
                                            },
                                            shape = RoundedCornerShape(size = 8.dp),
                                            contentPadding = PaddingValues(all = 1.dp)
                                        ) {
                                            Text(
                                                text = "+",
                                                fontSize = 25.sp
                                            )
                                        }

                                        Text(
                                            text = t4,
                                            fontSize = freqDigitWidth
                                        )

                                        Button(
                                            modifier = Modifier
                                                .padding(all = 1.dp)
                                                .size(size = plusMinusButtonWidth),
                                            onClick = {
                                                t4 = if (t4 == "0") "9" else { (t4.toInt() - 1).toString() }
                                                numInputFreq = (t0+t1+t2+t3+t4)
                                                if (isPlaying) {
                                                    restartPlaying(numInputFreq,numInputAmpAmp,numInputAmpFreq,noisePct,noiseType)
                                                }
                                            },
                                            shape = RoundedCornerShape(size = 8.dp),
                                            contentPadding = PaddingValues(all = 1.dp)
                                        ) {
                                            Text(
                                                text = "-",
                                                fontSize = 25.sp
                                            )
                                        }
                                    }
                                }

                                TextField(
                                    value = numInputAmpAmp,
                                    onValueChange = {
                                        numInputAmpAmp = it
                                        if (isPlaying) {
                                            restartPlaying(numInputFreq,numInputAmpAmp,numInputAmpFreq,noisePct,noiseType)
                                        }
                                    },
                                    label = { Text("Enter volume wave effect amplitude\n(0-32767, 0 to disable)") }
                                )

                                TextField(
                                    value = numInputAmpFreq,
                                    onValueChange = {
                                        numInputAmpFreq = it
                                        if (isPlaying) {
                                            restartPlaying(numInputFreq,numInputAmpAmp,numInputAmpFreq,noisePct,noiseType)
                                        }
                                    },
                                    label = { Text("Enter volume wave effect frequency\n(0-5, 0 to disable)") }
                                )

                                Text(
                                    text = "Choose noise type",
                                    fontSize = 20.sp
                                )

                                SingleChoiceSegmentedButtonRow {
                                    options.forEachIndexed { index, label ->
                                        SegmentedButton(
                                            shape = SegmentedButtonDefaults.itemShape(
                                                index = index,
                                                count = options.size
                                            ),
                                            onClick = {
                                                selectedIndex = index
                                                noiseType = options[selectedIndex]
                                                if (isPlaying) {
                                                    restartPlaying(numInputFreq,numInputAmpAmp,numInputAmpFreq,noisePct,noiseType)
                                                }
                                            },
                                            selected = index == selectedIndex,
                                            label = { Text(label) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.size(5.dp))
                                Text(
                                    text = "Percent of noise in the tone\n(0 = pure tone, 100 = pure noise)\nRestart playing after moving slider",
                                    fontSize = 20.sp
                                )

                                Text(text = noisePct.toInt().toString())
                                Slider(
                                    value = noisePct,
                                    onValueChange = {
                                        noisePct = it
//                                        if (isPlaying) {
//                                            restartPlaying(numInputFreq,numInputAmpAmp,numInputAmpFreq,noisePct,noiseType)
//                                        }
                                    },
                                    valueRange = 0f..100f,
                                    steps = 100,
                                    modifier = Modifier.height(20.dp)
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
                                        var numFormatError = true
                                        try {
                                            numInputFreq.toFloat()
                                            numFormatError = false
                                        } catch (e: Exception) {
                                            scope.launch {
                                                snackBarHostState.showSnackbar("Format error: frequency must be a number (optionally with a decimal point)")
                                            }
                                        }
                                        if (!numFormatError) {
                                            numFormatError =
                                                try {
                                                    numInputAmpAmp.toFloat()
                                                    false
                                                } catch (e: Exception) {
                                                    scope.launch {
                                                        snackBarHostState.showSnackbar("Format error: wave effect amplitude value must be a number (optionally with a decimal point)")
                                                    }
                                                    true
                                                }
                                        }
                                        if (!numFormatError) {
                                            numFormatError =
                                                try {
                                                    numInputAmpFreq.toFloat()
                                                    false
                                                } catch (e: Exception) {
                                                    scope.launch {
                                                        snackBarHostState.showSnackbar("Format error: wave effect frequency value must be a number (optionally with a decimal point)")
                                                    }
                                                    true
                                                }
                                        }
                                        if (!isPlaying && !numFormatError) {
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
                                                        numInputFreq.toInt(),
                                                        numInputAmpAmp.toFloat(),
                                                        numInputAmpFreq.toFloat(),
                                                        noisePct,
                                                        noiseType
                                                    )
                                                    playback(
                                                        numInputFreq.toInt(),
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
        frequencyTone: Int,
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

    private fun restartPlaying(
        numInputFreq: String,
        numInputAmpAmp: String,
        numInputAmpFreq: String,
        noisePct: Float,
        noiseType: String
    ) {
        scope.launch {
            DataStoreManager(baseContext).saveToDataStore(
                numInputFreq.toInt(),
                numInputAmpAmp.toFloat(),
                numInputAmpFreq.toFloat(),
                noisePct,
                noiseType
            )
            stopPlaying()
            startPlaying()
            playback(
                numInputFreq.toInt(),
                numInputAmpAmp.toFloat(),
                numInputAmpFreq.toFloat(),
                noisePct,
                noiseType
            )
        }
    }
}
