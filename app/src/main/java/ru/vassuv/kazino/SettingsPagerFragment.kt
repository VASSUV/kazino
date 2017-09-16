package ru.vassuv.kazino

import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.annotation.Nullable
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.TextView
import com.codekidlabs.storagechooser.Content
import com.codekidlabs.storagechooser.StorageChooser
import kotlinx.android.synthetic.main.table_base.*
import kotlinx.android.synthetic.main.table_settings.*
import org.jetbrains.anko.find
import ru.vassuv.kazino.repository.Counter
import ru.vassuv.kazino.repository.SharedData
import kotlin.reflect.KFunction0
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.io.*
import java.util.*


class SettingsPagerFragment : Fragment() {

    internal var pageNumber: Int = 0
    private lateinit var updateTable: () -> Unit

    private val minColdValue = 15

    override
    fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageNumber = arguments.getInt(ARGUMENT_PAGE_NUMBER)
    }

    override
    fun onCreateView(inflater: LayoutInflater?, @Nullable container: ViewGroup?, @Nullable savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.table_settings, null) as View
        val seekBar = view.find<SeekBar>(R.id.seekBar)
        val seekBarProgress = view.find<TextView>(R.id.seekBarProgress)
//        val checkCold = view.find<CheckBox>(R.id.checkCold)
        val checkHot = view.find<CheckBox>(R.id.checkHot)
        val buttonLoad = view.find<Button>(R.id.buttonLoad)
        val buttonSave = view.find<Button>(R.id.buttonSave)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val num = p1 + minColdValue
                seekBarProgress.text = num.toString()
//                checkCold.text = context.getString(R.string.check_cold, num)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) = Unit

            override fun onStopTrackingTouch(p0: SeekBar?) {
                Counter.countNotP = (p0?.progress ?: 0) + minColdValue
                SharedData.COUNT_NOT_P.saveInt(Counter.countNotP)
                update()
            }
        })

        view.find<Button>(R.id.buttonReset).setOnClickListener {
            Counter.reset()
            update()
        }

        seekBar.max = 100 - minColdValue
//        checkCold.isChecked = !SharedData.CHECK_COLD.getBoolean()
        checkHot.isChecked = !SharedData.CHECK_HOT.getBoolean()
//
//        checkCold.setOnCheckedChangeListener { _, isChecked ->
//            SharedData.CHECK_COLD.saveBoolean(!isChecked)
//            Counter.isViewCold = isChecked
//            update()
//        }
        checkHot.setOnCheckedChangeListener { _, isChecked ->
            SharedData.CHECK_HOT.saveBoolean(!isChecked)
            Counter.isViewHot = isChecked
            update()
        }

        buttonLoad.setOnClickListener {
            val c = Content()
            c.createLabel = "Создать"
            c.internalStorageText = "Внутренняя память"
            c.cancelLabel = "Отмена"
            c.selectLabel = "Выбрать"
            c.newFolderLabel = "Новая папка"
            c.overviewHeading = "Выберите диск"

            c.folderCreatedToastText = "Папка создана"
            c.folderErrorToastText = "Неудалось создать папку. Попробуйте еще раз"
            c.textfieldHintText = "Название папки"
            c.textfieldErrorText = "Пустое название папки"

            val chooser = StorageChooser.Builder()
                    .withActivity(activity)
                    .withFragmentManager(activity.fragmentManager)
                    .withMemoryBar(false)
                    .allowCustomPath(true)
                    .actionSave(false)
                    .setType(StorageChooser.FILE_PICKER)
                    .withContent(c)
                    .build()

            chooser.show()

            chooser.setOnSelectListener { path -> loadLog(path) }
        }

        buttonSave.setOnClickListener {
            val c = Content()
            c.createLabel = "Создать"
            c.internalStorageText = "Внутренняя память"
            c.cancelLabel = "Отмена"
            c.selectLabel = "Выбрать"
            c.newFolderLabel = "Новая папка"
            c.overviewHeading = "Выберите диск"

            c.folderCreatedToastText = "Папка создана"
            c.folderErrorToastText = "Неудалось создать папку. Попробуйте еще раз"
            c.textfieldHintText = "Название папки"
            c.textfieldErrorText = "Пустое название папки"

            val chooser = StorageChooser.Builder()
                    .withActivity(activity)
                    .withFragmentManager(activity.fragmentManager)
                    .withMemoryBar(false)
                    .allowCustomPath(true)
                    .setType(StorageChooser.DIRECTORY_CHOOSER)
                    .allowAddFolder(true)
                    .withContent(c)
                    .build()

            chooser.show()

            chooser.setOnSelectListener { path -> saveLog(path) }
        }
        return view
    }

    private fun loadLog(path: String?) {
        if (path == null) return

        val pd = ProgressDialog(activity)
        pd.setTitle("Обработка данных")
        pd.setMessage("Подождите пока пройде расчет всего ряда чисел из вашего файла - $path")
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        pd.isIndeterminate = true
        pd.setCancelable(false)
        pd.show()
        async(CommonPool) {
            val list = arrayListOf<Int>()
            val file = File(path)
            val scanner = Scanner(file)
            while (scanner.hasNextInt()) {
                list.add(scanner.nextInt())
            }
            Counter.reset()
            pd.max = list.size
            list.forEach {
                Counter.add(it)
            }

            launch(UI) {
                pd.dismiss()
                updateTable()
            }
        }
    }

    private fun saveLog(path: String?) {
        if (path == null) return
        val dir = File(path)
        var destinationFilename = path
        if (dir.exists() && dir.isDirectory) {
            val fileList = dir.listFiles().map { it.name }
            var i = 0
            while (fileList.contains("log$i.txt")) {
                i++
            }
            destinationFilename += "/log$i.txt"
        }

        var bos: BufferedOutputStream? = null

        try {
            bos = BufferedOutputStream(FileOutputStream(destinationFilename, false))
            Counter.list.forEach {
                bos?.write((it.toString() + "\n").toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                bos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun update() {
        val list = Counter.list.clone() as ArrayList<Int>
        val pd = ProgressDialog(activity)
        pd.setTitle("Обработка данных")
        pd.setMessage("Подождите пока пересчитается весь ряд")
        pd.setCancelable(false)
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        pd.isIndeterminate = true
        pd.show()
        async(CommonPool) {
            Counter.reset()
            pd.max = list.size
            list.forEach {
                Counter.add(it)
            }

            launch(UI) {
                pd.dismiss()
                updateTable()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        setViews()
    }

    fun setViews() {
        seekBar.progress = Counter.countNotP - minColdValue
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