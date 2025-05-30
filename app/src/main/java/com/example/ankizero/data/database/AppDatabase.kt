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
                                    populateDatabase(database.flashcardDao())
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
        private suspend fun populateDatabase(flashcardDao: FlashcardDao) {
            val currentTime = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC)

            val sampleFlashcards = listOf(
                Flashcard(
                    frenchWord = "bonjour",
                    creationDate = currentTime,
                    nextReviewDate = currentTime,
                    pronunciation = "bohn-ZHOOR"
                ),
                Flashcard(
                    frenchWord = "au revoir",
                    creationDate = currentTime,
                    nextReviewDate = currentTime,
                    pronunciation = "oh ruh-VWAHR"
                ),
                Flashcard(
                    frenchWord = "merci",
                    creationDate = currentTime,
                    nextReviewDate = currentTime,
                    pronunciation = "mehr-SEE"
                ),
                Flashcard(
                    frenchWord = "s'il vous plaît",
                    creationDate = currentTime,
                    nextReviewDate = currentTime,
                    pronunciation = "seel voo PLEH"
                ),
                Flashcard(
                    frenchWord = "excusez-moi",
                    creationDate = currentTime,
                    nextReviewDate = currentTime,
                    pronunciation = "ex-kew-ZAY mwah"
                ),
                Flashcard(
                    frenchWord = "parler",
                    creationDate = currentTime,
                    nextReviewDate = currentTime,
                    pronunciation = "par-LAY",
                    example = "Je peux parler français."
                ),
                Flashcard(
                    frenchWord = "manger",
                    creationDate = currentTime,
                    nextReviewDate = currentTime,
                    pronunciation = "mahn-ZHAY",
                    example = "J'aime manger du pain."
                ),
                Flashcard(
                    frenchWord = "boire",
                    creationDate = currentTime,
                    nextReviewDate = currentTime,
                    pronunciation = "bwahr",
                    example = "Je vais boire de l'eau."
                ),
                Flashcard(
                    frenchWord = "dormir",
                    creationDate = currentTime,
                    nextReviewDate = currentTime,
                    pronunciation = "dor-MEER",
                    example = "Je dois dormir maintenant."
                ),
                Flashcard(
                    frenchWord = "comprendre",
                    creationDate = currentTime,
                    nextReviewDate = currentTime,
                    pronunciation = "kom-PRAHN-druh",
                    example = "Je ne comprends pas."
                )
            )

            flashcardDao.insertFlashcards(sampleFlashcards)
        }
    }
}