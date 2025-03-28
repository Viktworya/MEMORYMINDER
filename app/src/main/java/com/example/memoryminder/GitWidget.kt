package com.example.memoryminder

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews

/**
 * Implementation of App Widget functionality.
 */
class GitWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }


}
/**
 * Updates the Git widget with a click listener to open the GitHub repository.
 *
 * @param context The context of the application or activity.
 * @param appWidgetManager The AppWidgetManager instance.
 * @param appWidgetId The ID of the app widget to be updated.
 */
@SuppressLint("UnspecifiedImmutableFlag")
private fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val remoteViews = RemoteViews(context.packageName, R.layout.git_widget)

    // Create an intent to open the GitHub repository
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse("https://github.com/peterkrajkovic/SudokuGame.git")

    // Create a pending intent to handle the click event
    val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

    // Set the click listener on the widget button
    remoteViews.setOnClickPendingIntent(R.id.gitButton, pendingIntent)

    // Update the app widget with the modified RemoteViews
    appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
}