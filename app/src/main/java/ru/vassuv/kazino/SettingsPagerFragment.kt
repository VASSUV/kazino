package ru.vassuv.kazino

import android.app.ProgressDialog
import android.os.Bundle
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.TextView
import com.codekidlabs.storagechooser.Content
import com.codekidlabs.storagechooser.StorageChooser
import kotlinx.android.synthetic.main.table_settings.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.find
import ru.vassuv.kazino.repository.Counter
import ru.vassuv.kazino.repository.SharedData
import kotlin.reflect.KFunction0
import java.io.*
import java.util.*


class SettingsPagerFragment : Fragment() {

    internal var pageNumber: Int = 0
    private lateinit var updateTable: () -> Unit

    private val minColdValue = 15

    override
    fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageNumber = arguments?.getInt(ARGUMENT_PAGE_NUMBER) ?: 0
    }

    override
    fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.table_settings, null)
        val seekBar = view.find<SeekBar>(R.id.seekBar)
        val seekBarProgress = view.find<TextView>(R.id.seekBarProgress)
        val check2_37 = view.find<CheckBox>(R.id.check2_37)
        val checkHot = view.find<CheckBox>(R.id.checkHot)
        val buttonLoad = view.find<Button>(R.id.buttonLoad)
        val buttonSave = view.find<Button>(R.id.buttonSave)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val num = p1 + minColdValue
                seekBarProgress.text = num.toString()
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
        check2_37.isChecked = !SharedData.CHECK_2_37.getBoolean()
        checkHot.isChecked = !SharedData.CHECK_HOT.getBoolean()

        check2_37.setOnCheckedChangeListener { _, isChecked ->
            SharedData.CHECK_2_37.saveBoolean(!isChecked)
            Counter.isView2_37 = isChecked
            update()
        }
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
                    .withFragmentManager(activity?.fragmentManager)
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
                    .withFragmentManager(activity?.fragmentManager)
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
        GlobalScope.launch {
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

            withContext(Dispatchers.Main) {
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
        GlobalScope.launch {
            Counter.reset()
            pd.max = list.size
            list.forEach {
                Counter.add(it)
            }

            withContext(Dispatchers.Main) {
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