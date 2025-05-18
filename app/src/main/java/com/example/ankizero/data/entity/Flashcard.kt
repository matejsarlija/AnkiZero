package com.example.ankizero.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.ZoneOffset

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
@Entity(tableName = "flashcard")
data class Flashcard(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val frenchWord: String,
    val creationDate: Long = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC),
    val lastReviewed: Long? = null,
    val reviewCount: Int = 0,
    val easeFactor: Float = 2.5f,
    val interval: Int = 1,
    val nextReviewDate: Long = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC),
    val pronunciation: String? = null,
    val example: String? = null,
    val notes: String? = null,
    val difficulty: Int? = null
)