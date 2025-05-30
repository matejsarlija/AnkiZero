package com.example.ankizero.util.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat // For permission check
import android.Manifest // For permission check
import android.content.pm.PackageManager // For permission check
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ankizero.AnkiZeroApplication // To access repository
import com.example.ankizero.MainActivity
import com.example.ankizero.R
import com.example.ankizero.Screen // For deep link route
import com.example.ankizero.util.AppLogger // Using AppLogger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random // For random notification ID

const val STUDY_REMINDERS_CHANNEL_ID = "STUDY_REMINDERS_CHANNEL_ID" // Already defined
const val STUDY_REMINDER_NOTIFICATION_ID_BASE = 1 // Base for notification ID

class ReviewReminderWorker(
    private val appContext: Context, // Changed to private val
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        AppLogger.i("ReviewReminderWorker", "ReviewReminderWorker started.")
        val repository = (applicationContext as AnkiZeroApplication).repository

        try {
            val currentTimeMillis = System.currentTimeMillis()
            // Use withContext for IO-bound operations like DB access
            val dueCards = withContext(Dispatchers.IO) {
                repository.getDueCards().first() // Pass currentTimeMillis, repo method calculates from LocalDate.now()
                                                     // If getDueCards() needed a param, it would be passed here.
                                                     // The current repository.getDueCards() doesn't take a param.
            }
            AppLogger.d("ReviewReminderWorker", "Found ${dueCards.size} due cards.")

            if (dueCards.isNotEmpty()) {
                val context = applicationContext
                val notificationTitle = "AnkiZero Study Reminder"
                val notificationText = "You have ${dueCards.size} flashcards due for review!"

                // Deep Link Intent
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    // Pass route as extra for MainActivity to handle (conceptual)
                    putExtra("destination_route", Screen.Flashcards)
                }
                val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
                val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, pendingIntentFlags)

                val builder = NotificationCompat.Builder(context, STUDY_REMINDERS_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground) // Ensure this drawable exists
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationText)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)

                // Notification Permission Check (Android 13+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        AppLogger.w("ReviewReminderWorker", "POST_NOTIFICATIONS permission not granted. Cannot show reminder.")
                        // Worker succeeds as it did its "work" of checking, but can't notify.
                        // Alternatively, return Result.failure() if notification is critical.
                        return Result.success()
                    }
                }

                with(NotificationManagerCompat.from(context)) {
                    // Using a random ID for now to ensure multiple notifications can appear if worker runs often.
                    // For a daily reminder, a fixed ID is usually fine like STUDY_REMINDER_NOTIFICATION_ID_BASE.
                    notify(Random.nextInt(), builder.build())
                    AppLogger.i("ReviewReminderWorker", "Notification sent for ${dueCards.size} due cards.")
                }
            } else {
                AppLogger.i("ReviewReminderWorker", "No cards due for review.")
            }
            return Result.success()
        } catch (e: Exception) {
            AppLogger.e("ReviewReminderWorker", "Error in ReviewReminderWorker", e)
            com.example.ankizero.util.GlobalErrorHandler.reportError(e, "ReviewReminderWorker failed")
            return Result.failure()
        }
    }
}
            // 3. Build a system notification
            //    (Notification channel should be created on app startup)

            // Create an explicit intent for an Activity in your app
            // val intent = Intent(appContext, MainActivity::class.java).apply {
            //     flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            //     // Optionally, add extras to navigate to a specific screen (deep linking)
            //     // putExtra("deep_link_route", Screen.Flashcards)
            // }
            // val pendingIntent: PendingIntent = PendingIntent.getActivity(appContext, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

            // val builder = NotificationCompat.Builder(appContext, STUDY_REMINDERS_CHANNEL_ID)
            //     .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with actual app icon
            //     .setContentTitle("AnkiZero Study Reminder")
            //     .setContentText("You have $dueCardsCount cards due for review!")
            //     .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            //     .setContentIntent(pendingIntent)
            //     .setAutoCancel(true) // Dismiss notification when tapped

            // 4. Show the notification
            // with(NotificationManagerCompat.from(appContext)) {
            //     // Ensure permissions are checked if targeting Android 13+ for notifications
            //     // if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            //     //     notify(STUDY_REMINDER_NOTIFICATION_ID, builder.build())
            //     //     Log.d("ReviewReminderWorker", "Notification sent for $dueCardsCount due cards.")
            //     // } else {
            //     //     Log.w("ReviewReminderWorker", "POST_NOTIFICATIONS permission not granted.")
