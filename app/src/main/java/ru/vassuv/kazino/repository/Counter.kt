package ru.vassuv.kazino.repository

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import ru.vassuv.kazino.R

object Counter {
    var list = ArrayList<Int>()

    val UNDEF = Color.TRANSPARENT

    val ZERO = Color.TRANSPARENT
    val BLACK = Color.BLACK
    val RED = Color.RED

    val HALF_1 = 1
    val HALF_2 = 2

    val ROW_1 = 3
    val ROW_2 = 2
    val ROW_3 = 1

    val DOZEN_1 = 1
    val DOZEN_2 = 2
    val DOZEN_3 = 3

    val COUNT_P = 37
    val COUNT_HOT = 50
    var countNotP = SharedData.COUNT_NOT_P.getInt().let { if (it == 0) 50 else it }

    fun add(num: Int) {
        list.add(num)
    }

    fun getDrawableNums(context: Context): Array<Drawable> {
        val arrayTemp = IntArray(37)
        var position = 0

        if (list.size > 1) {
            arrayTemp.forEachIndexed { index, _ ->
                position = list.lastIndexOf(index)

                arrayTemp[index] = if (position + COUNT_P > list.size)
                    list.slice(getEqOrZero(position - 1, COUNT_P)..getEqOrZero(position - 1, 0))
                            .count { index == it && position != 0 }.let { if (it > 1) -2 else it }
                else if (position < list.size - countNotP) -1 else 0
            }
        }

        return Array(37) {
            context.resources.getDrawable(when(arrayTemp[it]) {
                -1 -> R.drawable.blue_button
                0, -2 -> R.drawable.button
                else -> R.drawable.yellow_button
            })
        }
    }

    private fun getEqOrZero(param1: Int, param2: Int) = if (param1 < param2) 0 else (param1 - param2)

    fun reset() {
        list.clear()
    }

    fun removeLast() {
        list.removeAt(list.size - 1)
    }

    fun undo() = list.removeAt(list.size - 1)

    fun getColor(byte: Int): Int = when (byte) {
        0 -> ZERO
        2, 4, 6, 8, 10, 11, 13, 15, 17, 20, 22, 24, 26, 28, 29, 31, 33, 35 -> BLACK
        in 1..36 -> RED
        else -> UNDEF
    }

    fun getHalf(byte: Int): Int = when (byte) {
        0 -> ZERO
        in 1..18 -> HALF_1
        in 19..36 -> HALF_2
        else -> UNDEF
    }

    fun getDozen(byte: Int): Int = when (byte) {
        0 -> ZERO
        in 1..12 -> DOZEN_1
        in 13..24 -> DOZEN_2
        in 25..36 -> DOZEN_3
        else -> UNDEF
    }

    fun getRow(byte: Int): Int = when {
        byte == 0 -> ZERO
        byte > 36 || byte < 0 -> UNDEF
        byte % 3 == 0 -> ROW_1
        (byte + 1) % 3 == 0 -> ROW_2
        else -> ROW_3
    }

    fun count(num: Int) = list.size - list.lastIndexOf(num) - 1

    fun countInRow(row: Int) = list.size - list.indexOfLast { row != 0 && it % 3 == row % 3 } - 1

    fun countInHalf(half: Int) = list.size - list.indexOfLast { it in (half - 1) * 18 + 1..half * 18 } - 1

    fun countInDozen(third: Int) = list.size - list.indexOfLast { it in (third - 1) * 12 + 1..third * 12 } - 1

    fun countEven(): Int = list.size - list.indexOfLast { it % 2 == 0 } - 1

    fun countNotEven(): Int = list.size - list.indexOfLast { it % 2 != 0 } - 1

    fun countColor(color: Int): Int = list.size - list.indexOfLast { getColor(it) == color } - 1

}

