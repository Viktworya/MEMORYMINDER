package com.example.memoryminder

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

/**
 * ViewModel class for the Sudoku game.
 *
 * @param mode The mode of the game (1, 2, or any other value).
 */
class SudokuViewModel(mode: Int) : ViewModel() {
    var selectedCell = MutableLiveData<Pair<Int, Int>>()
    var boardNumbers = MutableLiveData<List<Pair<Int, Int>?>>()
    var minutes = MutableLiveData<Int>()
    var seconds = MutableLiveData<Int>()
    private var timer: Timer? = null
    private var elapsedTimeInSeconds = 0
    private var solution = ArrayList<Int>()
    var mistakes = 0
    private var finished = false

    init {
        selectedCell.postValue(Pair(10, 10))
        createSolution()
        when (mode) {
            1 -> makeBoard(30)
            2 -> makeBoard(40)
            else -> makeBoard(45)
        }
        startTimer()
    }

    /**
     * Creates the solution for the Sudoku board.
     */
    private fun createSolution() {
        val numbers = (1..9).shuffled(Random).toList()
        solution.addAll(numbers)

        var shiftedNumbers = numbers

        repeat(2) {
            shiftedNumbers = shiftedNumbers.subList(3, shiftedNumbers.size) + shiftedNumbers.subList(0, 3)
            solution.addAll(shiftedNumbers)
        }

        repeat(2) {
            shiftedNumbers = shiftedNumbers.subList(1, shiftedNumbers.size) + shiftedNumbers.subList(0, 1)
            solution.addAll(shiftedNumbers)

            repeat(2) {
                shiftedNumbers = shiftedNumbers.subList(3, shiftedNumbers.size) + shiftedNumbers.subList(0, 3)
                solution.addAll(shiftedNumbers)
            }
        }
    }

    /**
     * Generates the Sudoku board with a given number of empty cells.
     *
     * @param number The number of empty cells in the board.
     */
    private fun makeBoard(number: Int) {
        var reps = number
        val array = ArrayList<Pair<Int, Int>?>()
        for (i in 0 until solution.size) {
            array.add(Pair(solution[i], 0))
        }

        while (reps != 0) {
            val randomNumber = Random.nextInt(0, 81)
            if (array[randomNumber] != null) {
                array[randomNumber] = null
                reps--
            }
        }
        boardNumbers.postValue(array.toList())
    }

    /**
     * Updates the selected cell in the Sudoku board.
     *
     * @param row The row of the selected cell.
     * @param column The column of the selected cell.
     */
    fun updateSelectedCell(row: Int, column: Int) {
        if (finished) return
        if (boardNumbers.value!!.toMutableList()[row * 9 + column] == null ||
            (boardNumbers.value!!.toMutableList()[row * 9 + column]!!.second != 0 &&
                    boardNumbers.value!!.toMutableList()[row * 9 + column]!!.second != 3)
        ) {
            selectedCell.postValue(Pair(row, column))
        }
    }

    /**
     * Inputs a number into the selected cell.
     *
     * @param number The number to be input.
     */
    fun numberInput(number: Int) {
        if (selectedCell.value!!.first == 10 || finished) return
        val currentList = boardNumbers.value?.toMutableList()
        currentList?.set(selectedCell.value!!.first * 9 + selectedCell.value!!.second, Pair(number, 1))
        selectedCell.value = Pair(10, 10)
        boardNumbers.value = currentList
    }

    /**
     * Inputs a number into the selected cell and checks if it's correct.
     *
     * @param number The number to be input.
     * @return 1 if the cell is correct, -1 otherwise.
     */
    fun numberInputWithCheck(number: Int): Int {
        if (selectedCell.value!!.first == 10 || finished) return 0
        val row = selectedCell.value!!.first
        val column = selectedCell.value!!.second
        numberInput(number)
        if (checkCell(row, column)) {
            return 1
        }
        return -1
    }

    /**
     * Accepts the number input in the selected cell.
     */
    fun acceptNumber() {
        if (selectedCell.value!!.first == 10 || finished) return
        val currentList = boardNumbers.value?.toMutableList()
        if (currentList!![selectedCell.value!!.first * 9 + selectedCell.value!!.second] == null) return

        currentList[selectedCell.value!!.first * 9 + selectedCell.value!!.second] =
            Pair(currentList[selectedCell.value!!.first * 9 + selectedCell.value!!.second]!!.first, 2)

        selectedCell.value = Pair(10, 10)
        boardNumbers.value = currentList
    }

    /**
     * Removes the number from the selected cell.
     */
    fun removeNumber() {
        if (selectedCell.value!!.first == 10 || finished) return
        val currentList = boardNumbers.value?.toMutableList()
        if (currentList!![selectedCell.value!!.first * 9 + selectedCell.value!!.second] == null) return

        currentList[selectedCell.value!!.first * 9 + selectedCell.value!!.second] = null

        selectedCell.value = Pair(10, 10)
        boardNumbers.value = currentList
    }

    /**
     * Finishes the Sudoku game and checks the correctness of each cell.
     *
     * @return true if the game is finished, false otherwise.
     */
    fun finish(): Boolean {
        if (finished) return false

        boardNumbers.value?.toMutableList()?.forEach { pair ->
            if (pair == null) return false
        }
        stopTimer()
        for (row in 0 until 9) {
            for (column in 0 until 9) {
                if (!checkCell(row, column)) {
                    mistakes++
                }
            }
        }
        finished = true
        return true
    }

    /**
     * Checks if the cell at the given row and column is correct.
     *
     * @param row The row of the cell.
     * @param column The column of the cell.
     * @return true if the cell is correct, false otherwise.
     */
    private fun checkCell(row: Int, column: Int): Boolean {
        val currentList = boardNumbers.value?.toMutableList()

        return if (currentList?.get(row * 9 + column)!!.first == solution[row * 9 + column]) {
            currentList[row * 9 + column] = Pair(currentList[row * 9 + column]!!.first, 3)
            boardNumbers.value = currentList
            true
        } else {
            currentList[row * 9 + column] = Pair(currentList[row * 9 + column]!!.first, 4)
            boardNumbers.value = currentList
            false
        }

    }

    /**
     * Starts the timer to track the elapsed time of the game.
     */
    private fun startTimer() {
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                updateTime()
            }
        }, 0L, 1000L)
    }

    /**
     * Stops the timer.
     */
    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }

    /**
     * Updates the elapsed time and notifies the observers.
     */
    private fun updateTime() {
        elapsedTimeInSeconds++
        val minutes = elapsedTimeInSeconds / 60
        val seconds = elapsedTimeInSeconds % 60
        this.minutes.postValue(minutes)
        this.seconds.postValue(seconds)
    }

    /**
     * Clears the resources when the ViewModel is no longer used.
     */
    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}