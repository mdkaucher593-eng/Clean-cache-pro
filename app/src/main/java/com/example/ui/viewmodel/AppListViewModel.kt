package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppFilter
import com.example.data.model.AppInfo
import com.example.data.model.SortOrder
import com.example.data.preferences.SettingsManager
import com.example.data.repository.AppRepository
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppListUiState(
    val isLoading: Boolean = false,
    val allApps: List<AppInfo> = emptyList(),
    val filteredApps: List<AppInfo> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: AppFilter = AppFilter.USER,
    val selectedSort: SortOrder = SortOrder.SIZE_DESC,
    val selectedApp: AppInfo? = null,
    val totalStorageBytes: Long = 0L,
    val totalUserAppsCount: Int = 0,
    val totalSystemAppsCount: Int = 0,
    val errorMessage: String? = null
)

class AppListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)
    private val settingsManager = SettingsManager(application)

    private val _uiState = MutableStateFlow(AppListUiState())
    val uiState: StateFlow<AppListUiState> = _uiState.asStateFlow()

    val themeMode: StateFlow<ThemeMode> = settingsManager.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemeMode.SYSTEM
    )

    init {
        scanInstalledApps()
    }

    fun scanInstalledApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val apps = repository.getInstalledApps()
                val totalStorage = apps.sumOf { it.totalEstimatedStorageBytes }
                val userAppsCount = apps.count { !it.isSystemApp }
                val systemAppsCount = apps.count { it.isSystemApp }

                _uiState.update { state ->
                    val newState = state.copy(
                        isLoading = false,
                        allApps = apps,
                        totalStorageBytes = totalStorage,
                        totalUserAppsCount = userAppsCount,
                        totalSystemAppsCount = systemAppsCount
                    )
                    applyFilterAndSort(newState)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to scan apps: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            val newState = state.copy(searchQuery = query)
            applyFilterAndSort(newState)
        }
    }

    fun onFilterSelected(filter: AppFilter) {
        _uiState.update { state ->
            val newState = state.copy(selectedFilter = filter)
            applyFilterAndSort(newState)
        }
    }

    fun onSortSelected(sort: SortOrder) {
        _uiState.update { state ->
            val newState = state.copy(selectedSort = sort)
            applyFilterAndSort(newState)
        }
    }

    fun selectApp(app: AppInfo?) {
        _uiState.update { it.copy(selectedApp = app) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsManager.setThemeMode(mode)
        }
    }

    private fun applyFilterAndSort(state: AppListUiState): AppListUiState {
        val query = state.searchQuery.trim().lowercase()

        var filtered = state.allApps.filter { app ->
            when (state.selectedFilter) {
                AppFilter.ALL -> true
                AppFilter.USER -> !app.isSystemApp
                AppFilter.SYSTEM -> app.isSystemApp
            }
        }

        if (query.isNotEmpty()) {
            filtered = filtered.filter { app ->
                app.appName.lowercase().contains(query) ||
                        app.packageName.lowercase().contains(query)
            }
        }

        val sorted = when (state.selectedSort) {
            SortOrder.NAME_ASC -> filtered.sortedBy { it.appName.lowercase() }
            SortOrder.NAME_DESC -> filtered.sortedByDescending { it.appName.lowercase() }
            SortOrder.SIZE_DESC -> filtered.sortedByDescending { it.totalEstimatedStorageBytes }
            SortOrder.DATE_DESC -> filtered.sortedByDescending { it.lastUpdateTime }
        }

        return state.copy(filteredApps = sorted)
    }
}
