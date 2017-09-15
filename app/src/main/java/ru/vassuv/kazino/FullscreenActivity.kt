package ru.vassuv.kazino

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager.OnPageChangeListener
import android.support.v7.app.AppCompatActivity
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
