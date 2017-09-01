package ru.vassuv.kazino

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
import android.widget.TextView
import cn.nekocode.badge.BadgeDrawable
import kotlinx.android.synthetic.main.table_base.*
import org.jetbrains.anko.find
import ru.vassuv.kazino.repository.Counter

class BasePagerFragment : Fragment() {

    internal var pageNumber: Int = 0
    private lateinit var drawableButton: Drawable
    private lateinit var textViewArray: Array<TextView>

    val clickListener = View.OnClickListener {
        Counter.add(textViewArray.indexOf(it))
        setViews()
    }

    override
    fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageNumber = arguments.getInt(ARGUMENT_PAGE_NUMBER)
        drawableButton = resources.getDrawable(R.drawable.button)
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

        rootView.find<View>(R.id.fabReset).setOnClickListener({
            if (Counter.list.size > 0) {
                Counter.reset()
                setViews()
            }
        })

        val recyclerView = rootView.find<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, true)
                .apply { reverseLayout = true }
        recyclerView.adapter = NumAdapter()
        return rootView
    }

    override fun onStart() {
        super.onStart()
        Handler().postDelayed(this::setViews, 1000)
    }

    fun setViews() {
        val drawables: Array<Drawable> = Counter.getDrawableNums(context)
        for (i in 0..36) {
            val count = Counter.count(i)
            textViewArray[i].background = drawables[i]
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

        fun  Float.spToPixels() = this * Resources.getSystem().displayMetrics.scaledDensity + 0.5f
    }


    class NumAdapter : RecyclerView.Adapter<NumAdapter.Holder>() {
        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.item.setBackgroundColor(if (position == 0) Color.GRAY else Color.parseColor("#737373"))
            val num = Counter.list[Counter.list.size - position - 1]
            when (num) {
                0 -> {
                    holder.item.text = numSpannableArray[num]
                    holder.item.gravity = CENTER_VERTICAL
                }
                in 1..36 -> {
                    holder.item.text = numberItemText(num)
                    holder.item.gravity = if (Counter.getColor(num) == Counter.RED) TOP else BOTTOM
                }
                else -> {

                }
            }
        }

        override fun getItemCount() = Counter.list.size

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int)
                = Holder(LayoutInflater.from(parent?.context).inflate(R.layout.item_num, parent, false) as TextView)

        class Holder(val item: TextView) : RecyclerView.ViewHolder(item)
    }
}

