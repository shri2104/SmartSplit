package com.example.smartsplit.data

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val DARK_MODE_KEY = stringPreferencesKey("dark_mode")
    }

    val darkModeFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_MODE_KEY] ?: "Automatic"
        }

    suspend fun setDarkMode(option: String) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = option
        }
    }

    suspend fun getDarkMode(): String {
        return context.dataStore.data.map { preferences ->
            preferences[DARK_MODE_KEY] ?: "Automatic"
        }.first()
    }
}


@HiltViewModel
class DarkModeViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    val darkModeFlow = dataStoreManager.darkModeFlow
    val darkModeLiveData = darkModeFlow.asLiveData()

    fun setDarkMode(option: String) {
        viewModelScope.launch {
            dataStoreManager.setDarkMode(option)
        }
    }

    suspend fun getDarkMode(): String {
        return dataStoreManager.getDarkMode()
    }
}

@Composable
fun isDarkModeEnabled(): Boolean {
    val viewModel: DarkModeViewModel = hiltViewModel()
    val darkModeOption by viewModel.darkModeLiveData.observeAsState("Automatic")

    return when (darkModeOption) {
        "On" -> true
        "Off" -> false
        "Automatic" -> isSystemInDarkTheme()
        else -> false
    }
}