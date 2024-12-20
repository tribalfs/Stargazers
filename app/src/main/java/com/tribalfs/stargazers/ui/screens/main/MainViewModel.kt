package com.tribalfs.stargazers.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tribalfs.stargazers.data.StargazersRepo
import kotlinx.coroutines.flow.SharingStarted.Companion.Lazily
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class MainViewModel (
    private val stargazersRepo: StargazersRepo
): ViewModel() {

    val updateAvailableStateFlow = stargazersRepo.stargazersSettingsFlow.map {
        it.updateAvailable
    }.stateIn(viewModelScope, Lazily, false)

    fun checkUpdate() =viewModelScope.launch {
        stargazersRepo.getUpdate()
    }
}


class MainViewModelFactory(private val stargazersRepo: StargazersRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(stargazersRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
