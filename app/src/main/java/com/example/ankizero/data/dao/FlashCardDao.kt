package com.example.ankizero.data.dao
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.ankizero.data.entity.Flashcard
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the Flashcard entity.
 * Provides methods to interact with the flashcard table in the database.
 */
@Dao
interface FlashcardDao {
    /**
     * Insert a new flashcard into the database.
     * @param flashcard The flashcard to insert
     * @return The ID of the inserted flashcard
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(flashcard: Flashcard): Long // Renamed

    /**
     * Insert multiple flashcards into the database.
     * @param flashcards The list of flashcards to insert
     * @return The list of IDs of the inserted flashcards
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(flashcards: List<Flashcard>): List<Long>

    /**
     * Update an existing flashcard in the database.
     * @param flashcard The flashcard to update
     */
    @Update
    suspend fun update(flashcard: Flashcard) // Renamed

    /**
     * Delete a flashcard from the database.
     * @param flashcard The flashcard to delete
     */
    @Delete
    suspend fun delete(flashcard: Flashcard) // Renamed

    /**
     * Delete multiple flashcards from the database.
     * @param flashcards The list of flashcards to delete
     */
    @Delete
    suspend fun deleteFlashcards(flashcards: List<Flashcard>)

    /**
     * Delete multiple flashcards from the database by their IDs.
     * @param cardIds A list of flashcard IDs to delete.
     */
    @Query("DELETE FROM flashcard_table WHERE id IN (:cardIds)")
    suspend fun deleteCards(cardIds: List<Long>)

    /**
     * Get all flashcards from the database, ordered by creation date (newest first).
     * @return A Flow of all flashcards
     */
    // Removed original getAllFlashcards, will rename getFlashcardsAlphabetically

    /**
     * Get a flashcard by its ID.
     * @param id The ID of the flashcard to get
     * @return The flashcard with the given ID, or null if not found
     */
    @Query("SELECT * FROM flashcard_table WHERE id = :id") // table_name
    fun getCardById(id: Long): Flow<Flashcard?> // Changed return type and removed suspend

    /**
     * Get all flashcards due for review today.
     * @param currentTime The current time in epoch seconds
     * @return A Flow of flashcards due for review
     */
    @Query("SELECT * FROM flashcard_table WHERE nextReviewDate <= :currentTime ORDER BY nextReviewDate ASC") // table_name
    fun getCardsDueToday(currentTime: Long): Flow<List<Flashcard>>

    /**
     * Get the count of flashcards due for review today.
     * @param currentTime The current time in epoch seconds
     * @return The count of flashcards due for review
     */
    @Query("SELECT COUNT(*) FROM flashcard_table WHERE nextReviewDate <= :currentTime") // table_name
    fun getDueCardsCount(currentTime: Long): Flow<Int>

    /**
     * Search for flashcards by French word.
     * @param query The search query
     * @return A Flow of flashcards matching the search query
     */
    @Query("SELECT * FROM flashcard_table WHERE frenchWord LIKE '%' || :query || '%' ORDER BY creationDate DESC") // table_name
    fun searchFlashcards(query: String): Flow<List<Flashcard>>

    /**
     * Get flashcards ordered by difficulty (hardest first).
     * @return A Flow of flashcards ordered by difficulty
     */
    @Query("SELECT * FROM flashcard_table ORDER BY difficulty DESC") // table_name
    fun getFlashcardsByDifficulty(): Flow<List<Flashcard>>

    /**
     * Get flashcards ordered alphabetically by French word.
     * @return A Flow of flashcards ordered alphabetically
     */
    @Query("SELECT * FROM flashcard_table ORDER BY frenchWord ASC") // table_name
    fun getAllCards(): Flow<List<Flashcard>> // Renamed from getFlashcardsAlphabetically
}