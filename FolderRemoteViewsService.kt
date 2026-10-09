package com.folderpro.app

import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import android.widget.TextView

class FolderRemoteViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory = FolderFactory(applicationContext)

    private class FolderFactory(private val context: android.content.Context) : RemoteViewsFactory {
        private data class AppItem(val packageName: String, val label: String, val icon: android.graphics.drawable.Drawable)
        private val items = mutableListOf<AppItem>()

        override fun onCreate() = load()
        override fun onDataSetChanged() = load()
        override fun onDestroy() { items.clear() }
        override fun getCount() = items.size
        override fun getViewAt(position: Int): RemoteViews? {
            if (position !in items.indices) return null
            val item = items[position]
            val views = RemoteViews(context.packageName, R.layout.folder_grid_item)
            views.setImageViewBitmap(R.id.app_icon, drawableToBitmap(item.icon))
            views.setTextViewText(R.id.app_label, item.label)
            val launch = context.packageManager.getLaunchIntentForPackage(item.packageName)
            if (launch != null) {
                views.setOnClickFillInIntent(R.id.app_item, Intent().putExtra("package_name", item.packageName))
            }
            return views
        }
        override fun getLoadingView(): RemoteViews? = null
        override fun getViewTypeCount() = 1
        override fun getItemId(position: Int) = items.getOrNull(position)?.packageName?.hashCode()?.toLong() ?: position.toLong()
        override fun hasStableIds() = true

        @Suppress("DEPRECATION")
        private fun load() {
            items.clear()
            val prefs = context.getSharedPreferences(FolderWidgetProvider.PREFS, android.content.Context.MODE_PRIVATE)
            val selected = prefs.getStringSet(FolderWidgetProvider.KEY_APPS, emptySet()) ?: emptySet()
            val pm = context.packageManager
            selected.forEach { pkg ->
                try {
                    val launch = pm.getLaunchIntentForPackage(pkg) ?: return@forEach
                    val appInfo = pm.getApplicationInfo(pkg, 0)
                    items.add(AppItem(pkg, pm.getApplicationLabel(appInfo).toString(), pm.getApplicationIcon(appInfo)))
                } catch (_: Exception) { }
            }
            items.sortBy { it.label.lowercase() }
        }

        private fun drawableToBitmap(drawable: android.graphics.drawable.Drawable): android.graphics.Bitmap {
            val width = 96
            val height = 96
            val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            drawable.setBounds(0, 0, width, height)
            drawable.draw(canvas)
            return bitmap
        }
    }
}
