package com.example.memoryminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory class for creating instances of [SudokuViewModel].
 *
 * @param parameter An integer parameter used to customize the ViewModel instance.
 */
class ViewModelFactory(private val parameter: Int) : ViewModelProvider.Factory {
    /**
     * Creates a new instance of the requested ViewModel class.
     *
     * @param modelClass The class of the ViewModel to create.
     * @return A new instance of the requested ViewModel class.
     * @throws IllegalArgumentException if the requested ViewModel class is unknown.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass == SudokuViewModel::class.java) {
            return SudokuViewModel(parameter) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.canonicalName}")
    }
}