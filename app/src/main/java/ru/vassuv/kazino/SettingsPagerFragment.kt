package ru.vassuv.kazino

import android.os.Bundle
import android.support.annotation.Nullable
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import kotlinx.android.synthetic.main.table_counter.*
import org.jetbrains.anko.find
import ru.vassuv.kazino.repository.Counter
import ru.vassuv.kazino.repository.SharedData
import kotlin.reflect.KFunction0


class SettingsPagerFragment : Fragment() {

    internal var pageNumber: Int = 0
    private lateinit var updateTable: () -> Unit

    override
    fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageNumber = arguments.getInt(ARGUMENT_PAGE_NUMBER)
    }

    override
    fun onCreateView(inflater: LayoutInflater?, @Nullable container: ViewGroup?, @Nullable savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.table_counter, null) as View
        val seekBar = view.find<SeekBar>(R.id.seekBar)
        val seekBarProgress = view.find<TextView>(R.id.seekBarProgress)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                seekBarProgress.text = (p1 + Counter.COUNT_P).toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) = Unit

            override fun onStopTrackingTouch(p0: SeekBar?) {
                Counter.countNotP = (p0?.progress ?: 0) + Counter.COUNT_P
                SharedData.COUNT_NOT_P.saveInt(Counter.countNotP)
                updateTable()
            }
        })
        seekBar.max = 100 - Counter.COUNT_P
        return view
    }

    override fun onStart() {
        super.onStart()
        setViews()
    }

    fun setViews() {
        seekBar.progress = Counter.countNotP - Counter.COUNT_P
        seekBarProgress.text = Counter.countNotP.toString()
    }

    companion object {

        internal val ARGUMENT_PAGE_NUMBER = "arg_page_number"

        internal fun newInstance(page: Int, updateTable: KFunction0<Unit>): SettingsPagerFragment {
            val pageFragment = SettingsPagerFragment()
            val arguments = Bundle()
            arguments.putInt(ARGUMENT_PAGE_NUMBER, page)
            pageFragment.arguments = arguments
            pageFragment.updateTable = updateTable
            return pageFragment
        }
    }

}