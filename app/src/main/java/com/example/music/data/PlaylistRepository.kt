package com.example.music.data

import android.content.Context
import com.example.music.data.db.AppDatabase
import com.example.music.data.db.PlaylistEntity
import com.example.music.data.db.PlaylistSongEntity
import com.example.music.data.db.toSong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlaylistRepository(context: Context) {

    private val dao = AppDatabase.get(context).playlistDao()

    fun playlists(): Flow<List<PlaylistEntity>> = dao.observePlaylists()

    suspend fun create(name: String): Long =
        dao.insert(PlaylistEntity(name = name.trim().ifBlank { "新歌单" }))

    suspend fun rename(id: Long, name: String) =
        dao.rename(id, name.trim().ifBlank { "未命名歌单" })

    suspend fun delete(id: Long) = dao.delete(id)

    fun songs(playlistId: Long): Flow<List<Song>> =
        dao.observeSongs(playlistId).map { list -> list.map { it.toSong() } }

    fun count(playlistId: Long): Flow<Int> = dao.observeCount(playlistId)

    suspend fun add(playlistId: Long, song: Song) = dao.addSong(
        PlaylistSongEntity(
            playlistId = playlistId,
            songId = song.id, source = song.source,
            name = song.name, artist = song.artist,
            album = song.album, sign = song.sign
        )
    )

    suspend fun remove(playlistId: Long, song: Song) =
        dao.removeSong(playlistId, song.id, song.source)

    /** ★ 所有已收藏歌曲的 key 集合（source:songId） */
    fun allFavoritedKeys(): Flow<Set<String>> =
        dao.observeAllFavoritedKeys().map { it.toSet() }

    /** ★ 查询包含指定歌曲的所有歌单 */
    suspend fun findPlaylistsContaining(song: Song): List<PlaylistEntity> =
        dao.findPlaylistsContaining(song.id, song.source)
}