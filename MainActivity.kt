package com.folderpro.app

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.RadioButton
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var titleInput: EditText
    private lateinit var appList: LinearLayout
    private val checkboxes = mutableListOf<Pair<String, CheckBox>>()
    private lateinit var roundButton: RadioButton
    private lateinit var squareButton: RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        loadSettings()
        loadApps()
    }

    private fun buildUi() {
        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(24))
            setBackgroundColor(Color.rgb(16, 17, 20))
        }
        scroll.addView(root)
        setContentView(scroll)

        root.addView(TextView(this).apply {
            text = "Folder Pro"
            textSize = 28f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        })
        root.addView(TextView(this).apply {
            text = "آفلاین • پوشه‌های صفحهٔ اصلی"
            textSize = 14f
            setTextColor(Color.LTGRAY)
            setPadding(0, dp(4), 0, dp(18))
        })

        titleInput = EditText(this).apply {
            hint = "نام پوشه"
            setSingleLine(true)
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
            setPadding(dp(12), dp(8), dp(12), dp(8))
            setBackgroundColor(Color.rgb(38, 40, 45))
        }
        root.addView(titleInput, matchWrap())

        root.addView(TextView(this).apply {
            text = "شکل پوشه"
            textSize = 17f
            setTextColor(Color.WHITE)
            setPadding(0, dp(18), 0, dp(6))
        })
        val group = RadioGroup(this).apply { orientation = RadioGroup.HORIZONTAL }
        roundButton = RadioButton(this).apply { text = "گرد / دایره‌ای"; setTextColor(Color.WHITE); id = View.generateViewId() }
        squareButton = RadioButton(this).apply { text = "مربعی"; setTextColor(Color.WHITE); id = View.generateViewId() }
        group.addView(roundButton)
        group.addView(squareButton)
        root.addView(group)

        root.addView(TextView(this).apply {
            text = "برنامه‌های داخل پوشه"
            textSize = 17f
            setTextColor(Color.WHITE)
            setPadding(0, dp(18), 0, dp(6))
        })
        appList = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(appList, matchWrap())

        root.addView(Button(this).apply {
            text = "ذخیره و به‌روزرسانی ویجت"
            setOnClickListener { saveSettings() }
        }, matchWrap())

        root.addView(TextView(this).apply {
            text = "برای افزودن ویجت: صفحهٔ اصلی را نگه دار ← Widgets ← Folder Pro"
            textSize = 13f
            setTextColor(Color.LTGRAY)
            setPadding(0, dp(14), 0, 0)
        })
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences(FolderWidgetProvider.PREFS, MODE_PRIVATE)
        titleInput.setText(prefs.getString(FolderWidgetProvider.KEY_TITLE, "My Folder"))
        val isRound = prefs.getBoolean(FolderWidgetProvider.KEY_ROUND, true)
        roundButton.isChecked = isRound
        squareButton.isChecked = !isRound
    }

    @Suppress("DEPRECATION")
    private fun loadApps() {
        val pm = packageManager
        val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(launcher, PackageManager.MATCH_ALL)
            .mapNotNull { info ->
                val packageName = info.activityInfo?.packageName ?: return@mapNotNull null
                if (packageName == packageNameForSelf()) null
                else Triple(packageName, info.loadLabel(pm)?.toString() ?: packageName, info.loadIcon(pm))
            }
            .distinctBy { it.first }
            .sortedBy { it.second.lowercase() }

        val selected = getSharedPreferences(FolderWidgetProvider.PREFS, MODE_PRIVATE)
            .getStringSet(FolderWidgetProvider.KEY_APPS, emptySet()) ?: emptySet()

        appList.removeAllViews()
        checkboxes.clear()
        for ((pkg, label, icon) in apps) {
            val cb = CheckBox(this).apply {
                text = label
                setTextColor(Color.WHITE)
                buttonTintList = android.content.res.ColorStateList.valueOf(Color.LTGRAY)
                isChecked = selected.contains(pkg)
                setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
                compoundDrawablePadding = dp(10)
                setPadding(0, dp(6), 0, dp(6))
            }
            appList.addView(cb, matchWrap())
            checkboxes.add(pkg to cb)
        }
        if (apps.isEmpty()) {
            appList.addView(TextView(this).apply {
                text = "برنامهٔ قابل‌نمایشی پیدا نشد."
                setTextColor(Color.LTGRAY)
            })
        }
    }

    private fun saveSettings() {
        val chosen = checkboxes.filter { it.second.isChecked }.map { it.first }.toSet()
        getSharedPreferences(FolderWidgetProvider.PREFS, MODE_PRIVATE).edit()
            .putString(FolderWidgetProvider.KEY_TITLE, titleInput.text.toString().trim().ifBlank { "My Folder" })
            .putBoolean(FolderWidgetProvider.KEY_ROUND, roundButton.isChecked)
            .putStringSet(FolderWidgetProvider.KEY_APPS, chosen)
            .apply()

        val manager = AppWidgetManager.getInstance(this)
        val ids = manager.getAppWidgetIds(ComponentName(this, FolderWidgetProvider::class.java))
        ids.forEach { FolderWidgetProvider.updateWidget(this, manager, it) }
        Toast.makeText(this, "تنظیمات ذخیره شد", Toast.LENGTH_SHORT).show()
    }

    private fun packageNameForSelf() = applicationContext.packageName
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun matchWrap() = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )
}
