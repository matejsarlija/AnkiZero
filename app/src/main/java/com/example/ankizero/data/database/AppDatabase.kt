package com.example.ankizero.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.ankizero.data.dao.FlashCardDao // Changed to FlashCardDao
import com.example.ankizero.data.entity.Flashcard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Room database for the AnkiZero application.
 */
@Database(entities = [Flashcard::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Get the DAO for Flashcard entities.
     */
    abstract fun flashCardDao(): FlashCardDao // Changed to FlashCardDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Get the database instance, creating it if it doesn't exist.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ankizero_database"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate with sample data when the database is created
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    populateDatabase(database.flashCardDao())
                                }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration() // Added
                    .build()

                INSTANCE = instance
                instance
            }
        }

        /**
         * Populate the database with sample flashcards.
         */
        private suspend fun populateDatabase(flashcardDao: FlashCardDao) {
            val currentTimeMs = System.currentTimeMillis()

            val sampleFlashcards = listOf(
                Flashcard(
                    frenchWord = "bonjour",
                    englishTranslation = "hello",
                    creationDate = currentTimeMs,
                    nextReviewDate = currentTimeMs
                ),
                Flashcard(
                    frenchWord = "au revoir",
                    englishTranslation = "goodbye",
                    creationDate = currentTimeMs,
                    nextReviewDate = currentTimeMs
                ),
                Flashcard(
                    frenchWord = "merci",
                    englishTranslation = "thank you",
                    creationDate = currentTimeMs,
                    nextReviewDate = currentTimeMs
                ),
                Flashcard(
                    frenchWord = "s'il vous plaît",
                    englishTranslation = "please",
                    creationDate = currentTimeMs,
                    nextReviewDate = currentTimeMs
                ),
                Flashcard(
                    frenchWord = "excusez-moi",
                    englishTranslation = "excuse me",
                    creationDate = currentTimeMs,
                    nextReviewDate = currentTimeMs
                ),
                Flashcard(
                    frenchWord = "parler",
                    englishTranslation = "to speak",
                    creationDate = currentTimeMs,
                    nextReviewDate = currentTimeMs,
                    exampleSentence = "Je peux parler français."
                ),
                Flashcard(
                    frenchWord = "manger",
                    englishTranslation = "to eat",
                    creationDate = currentTimeMs,
                    nextReviewDate = currentTimeMs,
                    exampleSentence = "J'aime manger du pain."
                ),
                Flashcard(
                    frenchWord = "boire",
                    englishTranslation = "to drink",
                    creationDate = currentTimeMs,
                    nextReviewDate = currentTimeMs,
                    exampleSentence = "Je vais boire de l'eau."
                ),
                Flashcard(
                    frenchWord = "dormir",
                    englishTranslation = "to sleep",
                    creationDate = currentTimeMs,
                    nextReviewDate = currentTimeMs,
                    exampleSentence = "Je dois dormir maintenant."
                ),
                Flashcard(
                    frenchWord = "comprendre",
                    englishTranslation = "to understand",
                    creationDate = currentTimeMs,
                    nextReviewDate = currentTimeMs,
                    exampleSentence = "Je ne comprends pas."
                )
            )

            flashcardDao.insertFlashcards(sampleFlashcards)
        }
    }
}