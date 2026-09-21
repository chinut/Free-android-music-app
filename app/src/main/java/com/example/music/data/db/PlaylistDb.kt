package com.example.music.data.db

import android.content.Context
import androidx.room.*
import com.example.music.data.Song
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sortOrder: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId", "source"],
    foreignKeys = [ForeignKey(
        entity = PlaylistEntity::class,
        parentColumns = ["id"],
        childColumns = ["playlistId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("playlistId")]
)
data class PlaylistSongEntity(
    val playlistId: Long,
    val songId: String,
    val source: String,
    val name: String,
    val artist: String,
    val album: String,
    val sign: String,
    val addedAt: Long = System.currentTimeMillis(),
)

fun PlaylistSongEntity.toSong() = Song(
    id = songId, source = source, name = name,
    artist = artist, album = album, sign = sign
)

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY sortOrder ASC, id ASC")
    fun observePlaylists(): Flow<List<PlaylistEntity>>

    @Insert
    suspend fun insert(p: PlaylistEntity): Long

    @Query("UPDATE playlists SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM playlist_songs WHERE playlistId = :id ORDER BY addedAt DESC")
    fun observeSongs(id: Long): Flow<List<PlaylistSongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSong(s: PlaylistSongEntity)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :pid AND songId = :sid AND source = :src")
    suspend fun removeSong(pid: Long, sid: String, src: String)

    @Query("SELECT COUNT(*) FROM playlist_songs WHERE playlistId = :id")
    fun observeCount(id: Long): Flow<Int>

    // ★ 新增：所有已收藏歌曲的 key（source:songId）
    @Query("SELECT source || ':' || songId FROM playlist_songs")
    fun observeAllFavoritedKeys(): Flow<List<String>>

    // ★ 新增：查询包含指定歌曲的所有歌单
    @Query("""
        SELECT p.* FROM playlists p
        INNER JOIN playlist_songs ps ON ps.playlistId = p.id
        WHERE ps.songId = :songId AND ps.source = :source
        ORDER BY p.sortOrder ASC, p.id ASC
    """)
    suspend fun findPlaylistsContaining(songId: String, source: String): List<PlaylistEntity>
}

@Database(
    entities = [PlaylistEntity::class, PlaylistSongEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "music.db"
            ).build().also { INSTANCE = it }
        }
    }
}