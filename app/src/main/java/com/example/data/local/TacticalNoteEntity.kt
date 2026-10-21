package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "tactical_notes")
data class TacticalNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val priority: String = "NORMAL", // "CRITICAL", "HIGH", "NORMAL", "LOW"
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface TacticalNoteDao {
    @Query("SELECT * FROM tactical_notes ORDER BY timestamp DESC")
    fun getAllNotesFlow(): Flow<List<TacticalNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: TacticalNoteEntity): Long

    @Delete
    suspend fun deleteNote(note: TacticalNoteEntity)

    @Query("DELETE FROM tactical_notes")
    suspend fun clearNotes()
}
