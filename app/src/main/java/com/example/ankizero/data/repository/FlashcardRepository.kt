package com.example.ankizero.data.repository

import com.example.ankizero.data.dao.FlashcardDao
import com.example.ankizero.data.entity.Flashcard
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Repository for managing Flashcard data.
 * Provides methods for accessing and manipulating flashcards.
 */
class FlashcardRepository(private val flashcardDao: FlashcardDao) {

    /**
     * Get all flashcards, ordered by creation date (newest first).
     */
    fun getAllFlashcards(): Flow<List<Flashcard>> {
        return flashcardDao.getAllFlashcards()
    }

    /**
     * Get flashcards that are due for review today.
     */
    fun getCardsDueToday(): Flow<List<Flashcard>> {
        val currentTime = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC)
        return flashcardDao.getCardsDueToday(currentTime)
    }

    /**
     * Get the count of flashcards due for review today.
     */
    fun getDueCardsCount(): Flow<Int> {
        val currentTime = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC)
        return flashcardDao.getDueCardsCount(currentTime)
    }

    /**
     * Get a flashcard by its ID.
     */
    suspend fun getFlashcardById(id: Long): Flashcard? {
        return flashcardDao.getFlashcardById(id)
    }

    /**
     * Insert a new flashcard.
     */
    suspend fun insertFlashcard(flashcard: Flashcard): Long {
        return flashcardDao.insertFlashcard(flashcard)
    }

    /**
     * Update an existing flashcard.
     */
    suspend fun updateFlashcard(flashcard: Flashcard) {
        flashcardDao.updateFlashcard(flashcard)
    }

    /**
     * Delete a flashcard.
     */
    suspend fun deleteFlashcard(flashcard: Flashcard) {
        flashcardDao.deleteFlashcard(flashcard)
    }

    /**
     * Delete multiple flashcards.
     */
    suspend fun deleteFlashcards(flashcards: List<Flashcard>) {
        flashcardDao.deleteFlashcards(flashcards)
    }

    /**
     * Search for flashcards by query.
     */
    fun searchFlashcards(query: String): Flow<List<Flashcard>> {
        return flashcardDao.searchFlashcards(query)
    }

    /**
     * Get flashcards ordered by difficulty.
     */
    fun getFlashcardsByDifficulty(): Flow<List<Flashcard>> {
        return flashcardDao.getFlashcardsByDifficulty()
    }

    /**
     * Get flashcards ordered alphabetically.
     */
    fun getFlashcardsAlphabetically(): Flow<List<Flashcard>> {
        return flashcardDao.getFlashcardsAlphabetically()
    }

    /**
     * Apply the spaced repetition algorithm when a card is reviewed.
     * @param flashcard The flashcard that was reviewed
     * @param remembered Whether the user remembered the card (true = "Memorized", false = "No")
     * @return The updated flashcard
     */
    suspend fun processReview(flashcard: Flashcard, remembered: Boolean): Flashcard {
        val currentTime = System.currentTimeMillis() / 1000 // Current time in seconds
        val updatedFlashcard = if (remembered) {
            // User remembered the card - increase interval
            val newInterval = (flashcard.interval * 1.8f).toInt()
            val newEaseFactor = (flashcard.easeFactor + 0.1f).coerceAtMost(2.5f)
            val newNextReviewDate = currentTime + (newInterval * 86400) // days to seconds

            flashcard.copy(
                interval = newInterval,
                easeFactor = newEaseFactor,
                lastReviewed = currentTime,
                nextReviewDate = newNextReviewDate,
                reviewCount = flashcard.reviewCount + 1
            )
        } else {
            // User didn't remember - reset interval
            val newEaseFactor = (flashcard.easeFactor - 0.2f).coerceAtLeast(1.3f)
            val newNextReviewDate = currentTime + 86400 // Review tomorrow (1 day in seconds)

            flashcard.copy(
                interval = 1,
                easeFactor = newEaseFactor,
                lastReviewed = currentTime,
                nextReviewDate = newNextReviewDate,
                reviewCount = flashcard.reviewCount + 1
            )
        }

        flashcardDao.updateFlashcard(updatedFlashcard)
        return updatedFlashcard
    }
}