package ru.vassuv.kazino

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import androidx.appcompat.app.AppCompatActivity
import android.text.TextUtils
import android.text.TextUtils.*
import android.view.View.*
import kotlinx.android.synthetic.main.activity_fullscreen.*
import ru.vassuv.kazino.repository.Counter
import ru.vassuv.kazino.repository.SharedData
import java.util.*
import kotlin.collections.ArrayList

class FullscreenActivity : AppCompatActivity() {

    private val baseFragment = BasePagerFragment.newInstance(0)
    private val settingsFragment = SettingsPagerFragment.newInstance(1, baseFragment::setViews)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen)
        window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_FULLSCREEN
        this.actionBar?.hide()

        pager.adapter = MyFragmentPagerAdapter(supportFragmentManager)

        pager.setOnPageChangeListener(object : OnPageChangeListener {

            override fun onPageSelected(position: Int): Unit = when (position) {
                0 -> {
//                   val b = Handler().postDelayed(baseFragment::setViews, 1000)
                }
                else -> settingsFragment.setViews()
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit

            override fun onPageScrollStateChanged(state: Int) = Unit
        })

    }

    override fun onStop() {
        super.onStop()

        if(!SharedData.IS_SAVE_LOG.getBoolean()) {
            SharedData.LOG.saveString(Counter.list.joinToString(separator = ";"))
        }
    }

    private inner class MyFragmentPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment = when (position) {
            0 -> baseFragment
            else -> settingsFragment
        }

        override fun getCount(): Int {
            return 2
        }
    }
}
