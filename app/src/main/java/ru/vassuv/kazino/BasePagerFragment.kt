package ru.vassuv.kazino

import android.app.ProgressDialog
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.find
import ru.vassuv.kazino.repository.Counter
import ru.vassuv.kazino.repository.SharedData
import java.util.*

class BasePagerFragment : Fragment() {

    internal var pageNumber: Int = 0
    private lateinit var drawableButton: Drawable
    private lateinit var textViewArray: Array<TextView>

    val clickListener = View.OnClickListener {
        val num = textViewArray.indexOf(it)
        Counter.add(num)
        setViews()
    }

    override
    fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageNumber = arguments?.getInt(ARGUMENT_PAGE_NUMBER) ?: 0
        drawableButton = resources.getDrawable(R.drawable.button)
        Counter.reset()
    }

    private fun runLoadData() {
        if (!SharedData.IS_SAVE_LOG.getBoolean()) {
            val list = ArrayList(TextUtils.split(SharedData.LOG.getString(), ";")
                    .map { it.toIntOrNull() }
                    .filter { it != null }
                    .map { it as Int })
            val pd = ProgressDialog(activity)
            pd.setTitle("Обработка данных")
            pd.setMessage("Подождите пока восстановится ряд, сохраненный в прошлый сеанс")
            pd.setCancelable(false)
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            pd.isIndeterminate = true
            pd.show()
            GlobalScope.launch(Dispatchers.IO) {
                Counter.reset()
                pd.max = list.size
                list.forEachIndexed { index, i ->
                    Counter.add(i)

                    withContext(Dispatchers.Main) {
                        countIter.text = "Ход: $index"
                    }
                }

                withContext(Dispatchers.Main)  {
                    pd.dismiss()
                    setViews()
                }
            }
        } else
            setViews()
    }

    override
    fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View = inflater.inflate(R.layout.table_base, null) as View

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
        Handler().postDelayed(this::runLoadData, 100)
        return rootView
    }

    override fun onResume() {
        super.onResume()
    }

    fun setViews() {
        val field1OrNull = Counter.fieldList1.getOrNull(1)
        val field2OrNull = Counter.fieldList2.firstOrNull()
        field_1.text = field1OrNull?.toString() ?: ' '.toString()
        field_2.text = field2OrNull?.toString() ?: ' '.toString()
        field_3.text = ((field1OrNull ?: 0) + (field2OrNull ?: 0)).toString()

        for (i in 0..36) {
            val count = Counter.count(i)
            textViewArray[i].background = context?.resources?.getDrawable(
                    if (Counter.drawableResIdNums[i] == R.drawable.orange_button && !Counter.isViewHot)
                        if(count > Counter.countNotP) R.drawable.blue_button else R.drawable.button
                    else Counter.drawableResIdNums[i])
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

        recyclerView.adapter?.notifyDataSetChanged()
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
            if (Counter.listState[position] != R.drawable.button) {
                val resIdDrawable = Counter.listState[position]
                val drawable = holder.item.context.resources.getDrawable(if (resIdDrawable == R.drawable.orange_button && !Counter.isViewHot) 0 else resIdDrawable)
                holder.top.setImageDrawable(drawable)
            } else {
                holder.top.setImageDrawable(null)
            }
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
            val field1 = Counter.fieldList1[position] - (Counter.fieldList1.getOrNull(position + 1) ?: 0)
            val i35 = Counter.fieldList2[position] - (Counter.fieldList2.getOrNull(position + 1) ?: 0)

            holder.textField1.text = if (field1 != 0) field1.toString() else ""
            holder.textField2.text = if (i35 > 0) i35.toString() else ""
            holder.textView3.text = (Counter.list.size - position).toString()
        }

        override fun getItemCount() = Counter.list.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder = Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_num, parent, false))

        class Holder(val item: View) : RecyclerView.ViewHolder(item) {
            val textView: TextView = item.find<TextView>(R.id.textView)
            val textField1: TextView = item.find<TextView>(R.id.textField1)
            val textField2: TextView = item.find<TextView>(R.id.textField2)
            val textView3: TextView = item.find<TextView>(R.id.textView3)
            val top: ImageView = item.find<ImageView>(R.id.topIndicator)
        }
    }
}

