package com.example.ankizero.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
// import java.time.LocalDate - Will use System.currentTimeMillis() or explicit values
// import java.time.ZoneOffset - Will use System.currentTimeMillis() or explicit values

/**
 * Entity representing a flashcard in the AnkiZero system.
 *
 * @property id Unique identifier for the flashcard
 * @property frenchWord The French word/phrase on the front of the card
 * @property creationDate When the card was created (epoch seconds)
 * @property lastReviewed When the card was last reviewed (epoch seconds)
 * @property reviewCount How many times the card has been reviewed
 * @property easeFactor Multiplier for the interval adjustments (SM-2 algorithm)
 * @property interval Current interval in days
 * @property nextReviewDate When the card is due for review (epoch seconds)
 * @property pronunciation Optional pronunciation guide
 * @property example Optional example usage
 * @property notes Optional additional notes
 * @property difficulty Optional user-perceived difficulty (1-5)
 */
@Entity(tableName = "flashcard_table") // Updated table name
data class Flashcard(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val frenchWord: String,
    val englishTranslation: String,
    val creationDate: Long, // Default will be handled at instantiation or by Room default values if set
    var lastReviewed: Long? = null,
    var reviewCount: Int = 0,
    var easeFactor: Double = 2.5,     // Changed to Double, var
    var intervalInDays: Double = 1.0, // Renamed, changed to Double, var
    var nextReviewDate: Long,       // Default will be handled at instantiation, var
    val pronunciation: String? = null,
    val exampleSentence: String? = null, // Renamed from 'example'
    val notes: String? = null,
    var difficulty: Int? = null     // var
)