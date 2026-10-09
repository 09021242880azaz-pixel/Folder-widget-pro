package com.folderpro.app

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class FolderWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { updateWidget(context, manager, it) }
    }

    companion object {
        const val PREFS = "folder_pro_preferences"
        const val KEY_TITLE = "folder_title"
        const val KEY_APPS = "folder_apps"
        const val KEY_ROUND = "folder_round"

        fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val title = prefs.getString(KEY_TITLE, "My Folder") ?: "My Folder"
            val round = prefs.getBoolean(KEY_ROUND, true)

            val views = RemoteViews(context.packageName, R.layout.folder_widget)
            views.setTextViewText(R.id.folder_title, title)
            views.setInt(
                R.id.folder_widget_card,
                "setBackgroundResource",
                if (round) R.drawable.widget_background_round else R.drawable.widget_background_square
            )

            val serviceIntent = Intent(context, FolderRemoteViewsService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                data = android.net.Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.folder_grid, serviceIntent)
            val clickIntent = Intent(context, FolderAppClickReceiver::class.java)
            val clickPendingIntent = PendingIntent.getBroadcast(
                context, 2000 + widgetId, clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setPendingIntentTemplate(R.id.folder_grid, clickPendingIntent)
            views.setEmptyView(R.id.folder_grid, R.id.folder_empty)

            val openApp = PendingIntent.getActivity(
                context, 1000 + widgetId,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.folder_title, openApp)
            manager.updateAppWidget(widgetId, views)
            manager.notifyAppWidgetViewDataChanged(widgetId, R.id.folder_grid)
        }
    }
}
