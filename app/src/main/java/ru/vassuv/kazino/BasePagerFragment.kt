package ru.vassuv.kazino

import android.app.ProgressDialog
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.support.annotation.Nullable
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.TextUtils
import android.view.Gravity.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import cn.nekocode.badge.BadgeDrawable
import kotlinx.android.synthetic.main.table_base.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.find
import ru.vassuv.kazino.repository.Counter
import ru.vassuv.kazino.repository.SharedData
import java.io.File
import java.util.*

class BasePagerFragment : Fragment() {

    internal var pageNumber: Int = 0
    private lateinit var drawableButton: Drawable
    private lateinit var textViewArray: Array<TextView>

    val testValue = arrayOf(
            27, 24, 29, 26, 5, 11, 7, 12, 7, 10, 25, 1, 22, 2, 13, 24, 1, 15, 15, 24, 25,
            14, 26, 31, 3, 0, 24, 11, 21, 13, 20, 18, 35, 32, 29, 16, 6, 28, 25, 29, 8, 19, 19, 27,
            27, 6, 20, 27, 10, 32, 25, 1, 30, 31, 13, 33, 19, 15, 34, 5, 10, 26, 20, 25, 9, 5, 32,
            20, 5, 1, 20, 26, 2, 6, 25, 3, 20, 19, 3, 13, 36, 12, 12, 25, 34, 22, 22, 31, 25, 34,
            25, 30, 27, 10, 25, 20, 22, 4, 6, 31, 16, 11, 17, 30, 6, 35, 2, 14, 4, 35, 34, 21, 13,
            2, 32, 13, 32, 19, 28, 1, 2, 30, 28, 27, 31, 13, 9, 16, 3, 8, 10, 7, 16, 25, 27, 27, 3,
            13, 12, 13, 3, 27, 33, 8, 14, 5, 6, 24, 10, 30, 35, 5, 12, 6, 12, 12, 0, 10, 18, 20, 1,
            29, 27, 35, 0, 2, 17, 29, 2, 16, 29, 10, 12, 15, 5, 32, 19, 2, 29, 7, 15, 23, 28, 35,
            15, 6, 8, 27, 10, 26, 25, 22, 36, 6, 10, 20, 17, 30, 15, 6, 1, 1, 20, 5, 26, 26, 26, 26,
            12, 30, 32, 26, 5, 14, 26, 12, 29, 23, 27, 23, 29, 26, 31, 34, 10, 18, 25, 0, 28, 5, 22,
            32, 17, 15, 20, 4, 31, 2, 35, 16, 17, 35, 3, 15, 10, 34, 5, 4, 20, 11, 19, 26, 11, 0,
            31, 11, 34, 8, 29, 25, 14, 16, 21, 7, 16, 15, 18, 15, 3, 32, 30, 33, 3, 3, 22, 15, 21,
            33, 23, 17, 34, 0, 15, 35, 8, 0, 25, 21, 36, 22, 14, 27, 7, 13, 25, 20, 9, 5, 14, 11,
            35, 31, 11, 22, 29, 15, 30, 16, 22, 11, 15, 6, 33, 35, 15, 7, 33, 4, 24, 22, 21, 35,
            29, 28, 6, 33, 15, 5, 25, 34, 15, 18, 24, 24, 28, 20, 12, 10, 25, 34, 2, 31, 8, 21, 11,
            3, 3, 4, 27, 23, 14, 32, 29, 33, 25, 9, 12, 7, 31, 0, 15, 6, 21, 9, 12, 4, 26, 33, 9,
            18, 27, 7, 36, 33, 30, 1, 21, 10, 22, 34, 14, 34, 17, 30, 24, 17, 33, 14, 3, 36, 28,
            13, 1, 15, 4, 27, 15, 2, 17, 9, 22, 28, 13, 6, 6, 10, 8, 34, 3, 2, 8, 35, 1, 18, 35,
            1, 34, 10, 2, 32, 12, 27, 17, 17, 23, 13, 30, 24, 31, 33, 11, 35, 20, 6, 35, 14, 13, 20,
            27, 27, 19, 10, 6, 18, 13, 13, 24, 28, 16, 26, 36, 17, 21, 14, 3, 24, 27, 25
    )
    val testValue2 = arrayOf(19, 2, 10, 36, 35, 27, 14, 19, 29, 36, 1, 9, 21, 34, 34, 33, 14,
            32, 31, 35, 23, 32, 26, 1, 4, 6, 22, 28, 22, 22, 0, 16, 10, 34, 2, 24, 36, 8, 36, 24, 12,
            33, 13, 12, 22, 27, 33, 33, 7, 22, 24, 23, 32, 10, 9, 28, 20, 9, 3, 15, 24, 23, 1, 31, 8,
            31, 35, 19, 5, 24, 15, 35, 7, 9, 30, 2, 20, 6, 33, 36, 2, 33, 32, 24, 2, 11, 27, 26, 21,
            26, 31, 32, 15, 4, 33, 21, 30, 14, 29, 36, 14, 7, 14, 5, 10, 14, 6, 32, 16, 0, 16, 3, 32, 30, 28
    )


    val clickListener = View.OnClickListener {
        val num = textViewArray.indexOf(it)
        Counter.add(num)
        setViews()
    }

    override
    fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageNumber = arguments.getInt(ARGUMENT_PAGE_NUMBER)
        drawableButton = resources.getDrawable(R.drawable.button)
        Counter.reset()
        runLoadData()
    }

    private lateinit var disposable: Deferred<Unit>

    private fun runLoadData() {
        if(!SharedData.IS_SAVE_LOG.getBoolean()) {
            val list = ArrayList(TextUtils.split(SharedData.LOG.getString(), ";")
                    .map { it.toIntOrNull() }
                    .filter { it != null }
                    .map { it as Int })
            val pd = ProgressDialog(activity)
            pd.setTitle("Обработка данных")
            pd.setMessage("Подождите пока восстановится ряд, сохраненный в прошлый сеанс")
            pd.setCancelable(false)
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            pd.isIndeterminate = true
            pd.show()

            disposable = async(UI) {
                Counter.reset()
                pd.isIndeterminate = false
                pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)

                pd.max = list.size
                list.forEachIndexed { index, i ->
                    async(CommonPool) {
                        Counter.add(i)
                    }.await()
                    pd.incrementProgressBy(index)
                    countIter.text = "Ход: " + Counter.list.size
                }
                setViews()
                Thread.sleep(1000)
                pd.dismiss()
            }
        }
    }

    override
    fun onCreateView(inflater: LayoutInflater?, @Nullable container: ViewGroup?, @Nullable savedInstanceState: Bundle?): View? {
        val rootView: View = inflater?.inflate(R.layout.table_base, null) as View

        getArrayTextViews(rootView)

        textViewArray.forEach { it.setOnClickListener(clickListener) }

        rootView.find<View>(R.id.fab).setOnClickListener({
            if (Counter.list.size > 0) {
                Counter.removeLast()
                setViews()
            }
        })

        val recyclerView = rootView.find<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, true)
                .apply { reverseLayout = true }
        recyclerView.adapter = NumAdapter()
//        Handler().postDelayed(this::runTest, 100)
        return rootView
    }

    override fun onResume() {
        super.onResume()
        if(disposable.isCompleted) {
            setViews()
        }
    }

    fun runTest() {
        val pd = ProgressDialog(activity)
        pd.setTitle("Обработка данных")
        pd.setMessage("Подождите пока восстановится ряд, сохраненный в прошлый сеанс")
        pd.setCancelable(false)
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        pd.isIndeterminate = true
        pd.show()

        async(UI) {
            Counter.reset()
            pd.isIndeterminate = false
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)

            pd.max = testValue.size
            testValue.forEachIndexed { index, i ->
                async(CommonPool) {
                    Counter.add(i)
                }.await()
                pd.incrementProgressBy(index)
                countIter.text = "Ход: " + Counter.list.size
            }
            setViews()
            Thread.sleep(1000)
            pd.dismiss()
        }
    }

    fun setViews() {
        val field1OrNull = Counter.fieldList1.getOrNull(1)
        val field2OrNull = Counter.fieldList2.firstOrNull()
        field_1.text = field1OrNull?.toString() ?: ' '.toString()
        field_2.text = field2OrNull?.toString() ?: ' '.toString()
        field_3.text = ((field1OrNull ?: 0) + (field2OrNull ?: 0)).toString()

        for (i in 0..36) {
            val count = Counter.count(i)
            textViewArray[i].background = context.resources.getDrawable(Counter.drawableResIdNums[i])
            textViewArray[i].text = numberItemText(i, count.in0tes())
        }

        num_1_12.text = itemText("1-12", Counter.countInDozen(Counter.DOZEN_1).in0tes())
        num_13_24.text = itemText("13-24", Counter.countInDozen(Counter.DOZEN_2).in0tes())
        num_25_36.text = itemText("25-36", Counter.countInDozen(Counter.DOZEN_3).in0tes())

        num_1st.text = itemText("1 ряд", Counter.countInRow(Counter.ROW_1).in0tes())
        num_2nd.text = itemText("2 ряд", Counter.countInRow(Counter.ROW_2).in0tes())
        num_3rd.text = itemText("3 ряд", Counter.countInRow(Counter.ROW_3).in0tes())

        num_1_18.text = itemText("1-18", Counter.countInHalf(Counter.HALF_1).in0tes())
        num_19_36.text = itemText("19-36", Counter.countInHalf(Counter.HALF_2).in0tes())

        num_even.text = itemText("Ч", Counter.countEven().in0tes())
        num_not_even.text = itemText("Н", Counter.countNotEven().in0tes())

        num_red.text = itemText("Красн.", Counter.countColor(Counter.RED).in0tes(), Color.RED)
        num_black.text = itemText("Черн.", Counter.countColor(Counter.BLACK).in0tes(), Color.BLACK)

        recyclerView.adapter.notifyDataSetChanged()
        recyclerView.smoothScrollToPosition(0)

        countIter.text = "Ход: " + Counter.list.size
    }

    private fun getArrayTextViews(view: View) {
        val typedArray = resources.obtainTypedArray(R.array.view_ids)
        textViewArray = Array<TextView>(typedArray.length()) {
            view.find(typedArray.getResourceId(it, 0))
        }
        typedArray.recycle()
    }

    private fun Int.in0tes(): String = "\n" + (if (this == 0) ' ' else this)

    companion object {

        internal val ARGUMENT_PAGE_NUMBER = "arg_page_number"
        internal fun newInstance(page: Int): BasePagerFragment {
            val pageFragment = BasePagerFragment()
            val arguments = Bundle()
            arguments.putInt(ARGUMENT_PAGE_NUMBER, page)
            pageFragment.arguments = arguments
            return pageFragment
        }

        val numSpannableArray = getSpannableNums()

        private fun getSpannableNums(): ArrayList<SpannableString> {
            val list = arrayListOf<SpannableString>()
            list.add(BadgeDrawable.Builder()
                    .type(BadgeDrawable.TYPE_NUMBER)
                    .badgeColor(Color.WHITE)
                    .textColor(Color.BLACK)
                    .number(0)
                    .textSize(18.0f.spToPixels())
                    .build()
                    .toSpannable())
            (1..36).mapTo(list) {
                BadgeDrawable.Builder()
                        .type(BadgeDrawable.TYPE_NUMBER)
                        .badgeColor(Counter.getColor(it))
                        .textColor(Color.WHITE)
                        .number(it)
                        .textSize(18.0f.spToPixels())
                        .build()
                        .toSpannable()
            }
            return list
        }

        fun itemText(s: String = "", s2: String = "", textColor: Int = Color.WHITE) = SpannableString(TextUtils.concat(
                BadgeDrawable.Builder()
                        .type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
                        .badgeColor(Color.TRANSPARENT)
                        .textColor(textColor)
                        .text1(s)
                        .textSize(18.0f.spToPixels())
                        .build()
                        .toSpannable(), s2))

        fun numberItemText(i: Int, s: CharSequence = "") = SpannableString(TextUtils.concat(numSpannableArray[i], s))

        fun Float.spToPixels() = this * Resources.getSystem().displayMetrics.scaledDensity + 0.5f
    }


    class NumAdapter : RecyclerView.Adapter<NumAdapter.Holder>() {
        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.item.setBackgroundColor(if (position == 0) Color.GRAY else Color.parseColor("#737373"))
            val num = Counter.list[Counter.list.size - position - 1]
//            if (Counter.listState[position] != R.drawable.button) {
//                val drawable = holder.item.context.resources.getDrawable(Counter.listState[position])
//                holder.top.setImageDrawable(drawable)
//            } else holder.top.setImageDrawable(null)
            when (num) {
                0 -> {
                    holder.textView.text = numSpannableArray[num]
                    holder.textView.gravity = CENTER_VERTICAL
                }
                in 1..36 -> {
                    holder.textView.text = numberItemText(num)
                    holder.textView.gravity = if (Counter.getColor(num) == Counter.RED) TOP else BOTTOM
                }
                else -> {

                }
            }
            val i35 = Counter.fieldList2[position] -  (Counter.fieldList2.getOrNull(position + 1) ?: 0)

//            holder.textView1.text = if (i35 > 0) i35.toString() else ""
            holder.textView3.text = (Counter.list.size - position).toString()
        }

        override fun getItemCount() = Counter.list.size

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int)
                = Holder(LayoutInflater.from(parent?.context).inflate(R.layout.item_num, parent, false))

        class Holder(val item: View) : RecyclerView.ViewHolder(item) {
            val textView: TextView = item.find<TextView>(R.id.textView)
//            val textView1: TextView = item.find<TextView>(R.id.textView2)
            val textView3: TextView = item.find<TextView>(R.id.textView3)
//            val top: ImageView = item.find<ImageView>(R.id.topIndicator)
        }
    }
}

