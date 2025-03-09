package org.jshobbysoft.whitenoiseandtonegenerator

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreManager(private val context: Context) {
    //    https://medium.com/jetpack-composers/android-jetpack-datastore-5dfdfea4a3ea
    companion object {
        val TONE_FREQUENCY = intPreferencesKey("tone_frequency")
        val WAVE_AMP = floatPreferencesKey("wave_amp")
        val WAVE_FREQ = floatPreferencesKey("wave_freq")
        val NOISE_PCT = floatPreferencesKey("noise_pct")
        val NOISE_TYPE = stringPreferencesKey("noise_type")
    }

    suspend fun saveToDataStore(
        frequencyTone: Int,
        amplitudeDelta: Float,
        amplitudeFrequency: Float,
        noisePct: Float,
        noiseType: String
    ) {
        context.dataStore.edit { settings -> settings[TONE_FREQUENCY] = frequencyTone }
        context.dataStore.edit { settings -> settings[WAVE_AMP] = amplitudeDelta }
        context.dataStore.edit { settings -> settings[WAVE_FREQ] = amplitudeFrequency }
        context.dataStore.edit { settings -> settings[NOISE_PCT] = noisePct }
        context.dataStore.edit { settings -> settings[NOISE_TYPE] = noiseType }
    }

    private val preferences = runBlocking { context.dataStore.data.first() }

    //    https://amir-raza.medium.com/preference-datastore-android-an-implementation-guide-610645153696
    fun getFromDataStoreFT(): Int {
        return preferences[TONE_FREQUENCY] ?: "440".toInt()
//        val ft = context.dataStore.data.map { prefs -> prefs[TONE_FREQUENCY] ?: "440".toFloat() }
//        return ft.toString().toFloat()
    }

    fun getFromDataStoreWA(): Float {
        return preferences[WAVE_AMP] ?: "0".toFloat()
    }

    fun getFromDataStoreWF(): Float {
        return preferences[WAVE_FREQ] ?: "0".toFloat()
    }

    fun getFromDataStoreNP(): Float {
        return preferences[NOISE_PCT] ?: "0".toFloat()
    }

    fun getFromDataStoreNT(): String {
        return preferences[NOISE_TYPE] ?: "White"
    }
}