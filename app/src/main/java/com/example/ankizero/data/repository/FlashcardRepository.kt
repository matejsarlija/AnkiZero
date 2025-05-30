package com.example.ankizero.data.repository

import com.example.ankizero.data.dao.FlashCardDao // Updated import
import com.example.ankizero.data.entity.Flashcard
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneOffset // Kept for getCardsDueToday

/**
 * Repository for managing Flashcard data.
 * Provides methods for accessing and manipulating flashcards.
 * This class acts as a mediator between the DAOs (data sources) and ViewModels.
 */
class FlashcardRepository(private val flashCardDao: FlashCardDao) { // Updated DAO type

    /**
     * Get all flashcards, ordered by French word (alphabetically).
     * Mirrors FlashCardDao.getAllCards().
     */
    fun getAllCards(): Flow<List<Flashcard>> {
        return flashCardDao.getAllCards()
    }

    /**
     * Get flashcards that are due for review on or before the current date.
     * The current date is determined at the time of the call.
     */
    fun getDueCards(): Flow<List<Flashcard>> { // Renamed from getCardsDueToday for clarity on when "today" is determined
        val currentDate = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC)
        return flashCardDao.getDueCards(currentDate)
    }

    /**
     * Get the count of flashcards due for review on or before the current date.
     */
    fun getDueCardsCount(): Flow<Int> {
        val currentDate = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC)
        return flashCardDao.getDueCardsCount(currentDate)
    }

    /**
     * Get a flashcard by its ID, observing changes.
     */
    fun getCardById(id: Long): Flow<Flashcard?> { // Updated signature
        return flashCardDao.getCardById(id)
    }

    /**
     * Insert a new flashcard.
     */
    suspend fun insert(flashcard: Flashcard): Long { // Renamed
        return flashCardDao.insert(flashcard)
    }

    /**
     * Update an existing flashcard.
     */
    suspend fun update(flashcard: Flashcard) { // Renamed
        flashCardDao.update(flashcard)
    }

    /**
     * Delete a flashcard.
     */
    suspend fun delete(flashcard: Flashcard) { // Renamed
        flashCardDao.delete(flashcard)
    }

    /**
     * Delete multiple flashcards by their IDs.
     */
    suspend fun deleteCards(cardIds: List<Long>) { // Added method
        flashCardDao.deleteCards(cardIds)
    }

    /**
     * Delete multiple flashcards using a list of Flashcard objects.
     * (Kept from existing repository, useful if objects are already fetched)
     */
    suspend fun deleteFlashcards(flashcards: List<Flashcard>) {
        flashCardDao.deleteFlashcards(flashcards)
    }


    /**
     * Search for flashcards by query (matching French or English words).
     */
    fun searchFlashcards(query: String): Flow<List<Flashcard>> {
        return flashCardDao.searchFlashcards(query)
    }

    /**
     * Get flashcards ordered by difficulty.
     * (Kept from existing repository)
     */
    fun getFlashcardsByDifficulty(): Flow<List<Flashcard>> {
        return flashCardDao.getFlashcardsByDifficulty()
    }

    /**
     * Apply the spaced repetition algorithm when a card is reviewed.
     * @param flashcard The flashcard that was reviewed
     * @param remembered Whether the user remembered the card (true = "Memorized", false = "No")
     * @return The updated flashcard
     */
    suspend fun processReview(flashcard: Flashcard, remembered: Boolean): Flashcard {
        val currentTimeSeconds = System.currentTimeMillis() / 1000 // Current time in seconds

        val updatedFlashcard = if (remembered) {
            // User remembered the card - increase interval
            // Ensure calculations use Double for intervalInDays and easeFactor
            val newInterval = flashcard.intervalInDays * 1.8
            val newEaseFactor = (flashcard.easeFactor + 0.1).coerceAtMost(2.5) // Max easeFactor 2.5

            flashcard.copy(
                intervalInDays = newInterval,
                easeFactor = newEaseFactor,
                lastReviewed = System.currentTimeMillis(), // Use ms for consistency with creationDate
                nextReviewDate = System.currentTimeMillis() + (newInterval * 24 * 60 * 60 * 1000).toLong(), // interval in ms
                reviewCount = flashcard.reviewCount + 1
            )
        } else {
            // User didn't remember - reset interval
            val newEaseFactor = (flashcard.easeFactor - 0.2).coerceAtLeast(1.3) // Min easeFactor 1.3
            val newInterval = 1.0 // Reset interval to 1 day

            flashcard.copy(
                intervalInDays = newInterval,
                easeFactor = newEaseFactor,
                lastReviewed = System.currentTimeMillis(),
                nextReviewDate = System.currentTimeMillis() + (24 * 60 * 60 * 1000), // Review tomorrow (1 day in ms)
                reviewCount = flashcard.reviewCount + 1
            )
        }

        flashCardDao.update(updatedFlashcard) // Use renamed update method
        return updatedFlashcard
    }
}

/*
TODO: Database Query Optimization Notes (for when Room DAO and Entities are finalized):
1.  **Indexes:**
    - Add indexes to `Flashcard` entity for frequently queried columns.
        - `frenchWord` (for searching, alphabetical sorting)
        - `englishTranslation` (for searching)
        - `nextReviewDate` (for fetching due cards)
        - `creationDate` (for "Recent" sorting)
        - `difficulty` (for "Difficulty" sorting)
    - Example in Entity: `@Entity(indices = [Index(value = ["nextReviewDate"])])`

2.  **Efficient Queries:**
    - Select only necessary columns if not all data is needed for a particular view/logic. Create POJOs or use `@ColumnInfo` for partial selections in DAO.
    - Use `LIMIT` and `OFFSET` for pagination if displaying very large lists, though `LazyColumn` handles UI virtualization well.
    - Be mindful of `LIKE` queries with wildcards at the beginning (`%query`), as they are less performant. FTS (Full-Text Search) tables can be an option for advanced search.

3.  **Asynchronous Operations:**
    - All database operations (inserts, updates, deletes, queries) must be performed off the main thread.
    - Room DAOs with `suspend` functions or returning `Flow` already handle this. Ensure ViewModels call these suspend functions from `viewModelScope`.

4.  **Flow for Reactive Updates:**
    - Use `kotlinx.coroutines.flow.Flow` for observing data changes from the database. Room supports returning `Flow` directly from DAOs.
    - This allows the UI to reactively update when underlying data changes without manual refresh logic.

5.  **Transactions:**
    - Use `@Transaction` in DAOs for operations that involve multiple database modifications to ensure atomicity. For example, if updating a card and also logging a review event.

6.  **Analyze Queries:**
    - Use Android Studio's "Database Inspector" to inspect the database and run/analyze queries.
    - Room can output the generated SQL at compile time if configured in build.gradle, which can be useful for verifying query structure.
        ```gradle
        // In app/build.gradle.kts, inside android > defaultConfig or a buildType
        // javaCompileOptions {
        //     annotationProcessorOptions {
        //         arguments["room.schemaLocation"] = "$projectDir/schemas".toString()
        //         arguments["room.expandProjection"] = "true" // To see generated SQL for * queries
        //     }
        // }
        // For KSP:
        // ksp {
        //     arg("room.schemaLocation", "$projectDir/schemas")
        //     arg("room.expandProjection", "true")
        // }
        ```
*/