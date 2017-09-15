package ru.vassuv.kazino.repository

import android.graphics.Color
import ru.vassuv.kazino.R

object Counter {
    var list = ArrayList<Int>()
    var listState = ArrayList<Int>()
    var fieldList1 = ArrayList<Int>()
    var fieldList2 = ArrayList<Int>()
    var drawableResIdNums = IntArray(37) { R.drawable.button }

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
    var countNotP = SharedData.COUNT_NOT_P.getInt().let { if (it == 0) COUNT_HOT else it }

    var isViewCold: Boolean = !SharedData.CHECK_COLD.getBoolean()
    var isViewHot: Boolean = !SharedData.CHECK_HOT.getBoolean()

    fun add(num: Int) {
        list.add(num)
        listState.add(0,0)
        reCount(true, num)
        listState[0] = drawableResIdNums[num]
    }

    fun reCount(isAddCounters: Boolean, num: Int?) {
        var countResetYellow: Int = 0
        var i1: Int = -1
        var i2: Int = -1
        var i3: Int = -1

        val arrayIndex = Array(37, { ArrayList<Int>() })

        val countP = minOf(maxOf(countNotP, COUNT_HOT), list.size - 1)
        val sizeList = list.size - 1
        for (i in 0..countP) {
            arrayIndex[list[sizeList - i]].add(i)
        }

        arrayIndex.forEachIndexed { i, arrayList ->
            drawableResIdNums[i] = when (arrayList.size) {
                0 -> {
                    if (isViewCold && list.size > countNotP)
                        R.drawable.blue_button
                    else
                        R.drawable.button
                }
                1 -> {
                    i1 = arrayList[0]
                    if (isViewCold && i1 > countNotP)
                        R.drawable.blue_button
                    else
                        R.drawable.button
                }
                2 -> {
                    i1 = arrayList[0]
                    i2 = arrayList[1]
                    if (i1 < COUNT_P && i2 < i1 + COUNT_P) {
                        R.drawable.yellow_button
                    } else if (isViewCold && i1 > countNotP)
                        R.drawable.blue_button
                    else
                        R.drawable.button
                }
                else -> {
                    i1 = arrayList[0]
                    i2 = arrayList[1]
                    i3 = arrayList[2]
                    if (isViewHot && i3 < COUNT_HOT) {
                        if (i == num && listState[i2] == R.drawable.yellow_button && i2 - i1 < countP) {
                            countResetYellow++
//                            println ("YELLOW{$i1,$i2,$i3}: $i на ${list.size} ходу")
                        }
                        R.drawable.orange_button
                    } else if (i1 < COUNT_P && i2 < i1 + COUNT_P && i3 > i1 + COUNT_P) {
                        R.drawable.yellow_button
                    } else if (isViewCold && i1 > countNotP)
                        R.drawable.blue_button
                    else
                        R.drawable.button
                }
            }
        }

        if (isAddCounters)
            addCounters(countResetYellow)
    }

    private fun addCounters(countResetYellow: Int) {
        val countYellow = drawableResIdNums.count { it == R.drawable.yellow_button }
        fieldList1.add(0, (fieldList1.firstOrNull() ?: 0) - countYellow)
        fieldList2.add(0, (fieldList2.firstOrNull() ?: 0) + countResetYellow * 35)
    }

    private fun getEqOrZero(param1: Int, param2: Int) = if (param1 < param2) 0 else (param1 - param2)

    fun reset() {
        list.clear()
        drawableResIdNums = IntArray(37) { R.drawable.button }
        fieldList1.clear()
        fieldList2.clear()
        listState.clear()
    }

    fun removeLast() {
        list.removeAt(list.size - 1)
        listState.removeAt(0)
        if (fieldList1.size > 0) {
            fieldList1.removeAt(0)
            fieldList2.removeAt(0)
        }
        reCount(false, list.lastOrNull())
    }

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

