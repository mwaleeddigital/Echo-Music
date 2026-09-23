package com.music.echo.sharedui.recommendation

import com.music.echo.sharedui.data.UserTasteProfile
import com.music.echo.sharedui.screens.DisplayAlbum
import com.music.echo.sharedui.screens.DisplayArtist
import com.music.echo.sharedui.screens.DisplayPlaylist
import com.music.echo.sharedui.screens.DisplayQuickPick
import com.music.echo.sharedui.screens.DisplayTrack
import com.music.echo.sharedui.screens.HomeChip
import com.music.echo.sharedui.screens.HomePageData
import com.music.echo.sharedui.screens.HomeSectionData
import com.music.echo.sharedui.toDisplayAlbum
import com.music.echo.sharedui.toDisplayArtist
import com.music.echo.sharedui.toDisplayPlaylist
import com.music.echo.sharedui.toDisplayTrack
import com.music.innertube.YouTube
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.PlaylistItem
import com.music.innertube.models.SongItem
import com.music.innertube.models.WatchEndpoint
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.random.Random

object RecommendationEngine {

    suspend fun generatePersonalizedHome(
        tasteProfile: UserTasteProfile,
        selectedChip: HomeChip?,
        historyTracks: List<DisplayTrack>,
        likedTracks: List<DisplayTrack>,
        downloadedTracks: List<DisplayTrack> = emptyList()
    ): HomePageData = coroutineScope {

        val finalSections = mutableListOf<HomeSectionData>()
        val collectedChips = mutableListOf<HomeChip>()

        // 1. Fetch YouTube Native Feed (with chip params if selected)
        val homeFeedDeferred = async {
            try {
                YouTube.home(params = selectedChip?.params).getOrNull()
            } catch (e: Exception) {
                null
            }
        }

        // 2. Fetch Explore / New Releases
        val exploreDeferred = async {
            try {
                YouTube.explore().getOrNull()?.newReleaseAlbums?.map { it.toDisplayAlbum() }.orEmpty()
            } catch (e: Exception) {
                emptyList()
            }
        }

        // 3. Daily Discover Candidates: Seeded from user's liked songs (Mobile Echo Music Algorithm)
        val dailyDiscoverSeeds = (likedTracks.shuffled() + historyTracks.shuffled())
            .distinctBy { it.id }
            .take(4)

        val dailyDiscoverDeferred = async {
            val candidateTracks = mutableListOf<DisplayTrack>()
            for (seed in dailyDiscoverSeeds) {
                try {
                    val nextEndpoint = YouTube.next(WatchEndpoint(videoId = seed.id)).getOrNull()?.relatedEndpoint
                    if (nextEndpoint != null) {
                        YouTube.related(nextEndpoint).onSuccess { relatedPage ->
                            val tracks = relatedPage.songs
                                .filter { it.id != seed.id }
                                .map { it.toDisplayTrack() }
                            candidateTracks.addAll(tracks)
                        }
                    }
                } catch (_: Exception) {}
            }
            candidateTracks.distinctBy { it.id }
        }

        // 4. "Similar to [Top Artist]" Candidates: Seeded from user's top affinity artist
        val topArtist = tasteProfile.artistAffinityScores.maxByOrNull { it.value }?.key
            ?: likedTracks.firstOrNull()?.artist
            ?: historyTracks.firstOrNull()?.artist

        val similarArtistDeferred = async {
            if (topArtist != null && topArtist.isNotBlank()) {
                try {
                    val summary = YouTube.searchSummary("$topArtist hits").getOrNull()
                    val songs = summary?.summaries?.flatMap { it.items }?.filterIsInstance<SongItem>()?.map { it.toDisplayTrack() }.orEmpty()
                    songs.distinctBy { it.id }.take(15)
                } catch (_: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }
        }

        // 5. Context-aware Mood Mixes (if chip selected or popular shelves)
        val contextQueries = if (selectedChip != null) {
            listOf("${selectedChip.title} hits", "${selectedChip.title} playlist")
        } else {
            listOf("Today's Top Hits", "Viral Hits 2026", "Chill Beats")
        }

        val contextShelvesDeferred = contextQueries.map { query ->
            async {
                try {
                    val summary = YouTube.searchSummary(query).getOrNull()
                    if (summary != null) {
                        val songs = summary.summaries.flatMap { it.items }.filterIsInstance<SongItem>().map { it.toDisplayTrack() }
                        val playlists = summary.summaries.flatMap { it.items }.filterIsInstance<PlaylistItem>().map { it.toDisplayPlaylist() }
                        val albums = summary.summaries.flatMap { it.items }.filterIsInstance<AlbumItem>().map { it.toDisplayAlbum() }
                        HomeSectionData(
                            title = query.replace("2026", "").trim().capitalizeWords(),
                            songs = songs.distinctBy { it.id }.take(12),
                            playlists = playlists.distinctBy { it.id }.take(8),
                            albums = albums.distinctBy { it.id }.take(8)
                        )
                    } else null
                } catch (_: Exception) {
                    null
                }
            }
        }

        // Await primary candidates
        val nativeHome = homeFeedDeferred.await()
        val newReleases = exploreDeferred.await()
        val discoveredCandidates = dailyDiscoverDeferred.await()
        val similarArtistCandidates = similarArtistDeferred.await()
        val contextShelves = contextShelvesDeferred.mapNotNull { it.await() }

        // Process native chips
        if (nativeHome?.chips != null) {
            collectedChips.addAll(nativeHome.chips!!.map { HomeChip(it.title, it.endpoint?.params) })
        }
        if (collectedChips.isEmpty()) {
            collectedChips.addAll(
                listOf(
                    HomeChip("Energise"),
                    HomeChip("Feel good"),
                    HomeChip("Relax"),
                    HomeChip("Work out"),
                    HomeChip("Commute"),
                    HomeChip("Party"),
                    HomeChip("Focus"),
                    HomeChip("Romance")
                )
            )
        }

        // Process native sections
        if (nativeHome?.sections != null) {
            for (sec in nativeHome.sections) {
                val sTracks = sec.items.filterIsInstance<SongItem>().map { it.toDisplayTrack() }
                val sAlbums = sec.items.filterIsInstance<AlbumItem>().map { it.toDisplayAlbum() }
                val sPlaylists = sec.items.filterIsInstance<PlaylistItem>().map { it.toDisplayPlaylist() }
                val sArtists = sec.items.filterIsInstance<ArtistItem>().map { it.toDisplayArtist() }

                if (sTracks.isNotEmpty() || sAlbums.isNotEmpty() || sPlaylists.isNotEmpty() || sArtists.isNotEmpty()) {
                    finalSections.add(
                        HomeSectionData(
                            title = sec.title,
                            label = sec.label,
                            thumbnail = sec.thumbnail,
                            songs = sTracks,
                            albums = sAlbums,
                            playlists = sPlaylists,
                            artists = sArtists
                        )
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // SPOTIFY RANKING & SCORING MODEL
        // Score = Taste + Similarity + Context + Recency + Exploration - Penalty
        // -------------------------------------------------------------
        fun scoreTrack(track: DisplayTrack, isDiscovered: Boolean = false): Float {
            var score = 0f
            val artistKey = track.artist.trim().lowercase()

            // 1. User Taste (Artist affinity score)
            val affinity = tasteProfile.artistAffinityScores[artistKey] ?: 0f
            score += affinity.coerceIn(-5f, 10f) * 1.5f

            // 2. Liked bonus
            if (tasteProfile.likedSongIds.contains(track.id)) {
                score += 3.5f
            }

            // 3. Skip Penalty
            if (tasteProfile.skippedSongIds.contains(track.id)) {
                score -= 6.0f
            }

            // 4. Similarity / Discovery
            if (isDiscovered) {
                score += 2.0f
            }

            // 5. Exploration Jitter (15% chance of introducing high-potential novel items)
            if (Random.nextFloat() < 0.18f) {
                score += Random.nextFloat() * 2.5f
            }

            return score
        }

        // Rank discovered tracks
        val rankedDiscovered = discoveredCandidates
            .filter { !tasteProfile.skippedSongIds.contains(it.id) }
            .sortedByDescending { scoreTrack(it, isDiscovered = true) }

        // Section 0: "Downloaded & Offline Songs" (Stored locally in storage)
        if (downloadedTracks.isNotEmpty()) {
            finalSections.add(
                HomeSectionData(
                    title = "Downloaded & Offline Songs",
                    label = "READY TO PLAY OFFLINE",
                    songs = downloadedTracks.take(15)
                )
            )
        }

        // Section A: "Jump back in" (Frequently replayed + recent favorites)
        val jumpBackInTracks = (downloadedTracks + historyTracks + likedTracks)
            .distinctBy { it.id }
            .take(12)

        if (jumpBackInTracks.isNotEmpty()) {
            finalSections.add(
                HomeSectionData(
                    title = "Jump back in",
                    label = if (nativeHome == null) "OFFLINE • SAVED SONGS" else "KEEP LISTENING",
                    songs = jumpBackInTracks
                )
            )
        }

        // Section B: "Daily Discover" (Made for you based on liked songs)
        if (rankedDiscovered.isNotEmpty()) {
            finalSections.add(
                1.coerceAtMost(finalSections.size),
                HomeSectionData(
                    title = "Daily Discover",
                    label = "MADE FOR YOU",
                    songs = rankedDiscovered.take(15)
                )
            )
        }

        // Section C: "Similar to [Top Artist]"
        if (topArtist != null && similarArtistCandidates.isNotEmpty()) {
            finalSections.add(
                2.coerceAtMost(finalSections.size),
                HomeSectionData(
                    title = "Similar to ${topArtist.capitalizeWords()}",
                    label = "MORE OF WHAT YOU LIKE",
                    songs = similarArtistCandidates.take(12)
                )
            )
        }

        // Add Context-aware shelves
        finalSections.addAll(contextShelves)

        // Deduplicate sections by normalized title
        val distinctSections = finalSections
            .distinctBy { it.title.trim().lowercase() }
            .filter { !it.isEmpty }

        // -------------------------------------------------------------
        // Construct 2x4 Quick Access Grid (Top 8 Algorithmic Picks)
        // -------------------------------------------------------------
        val heroCandidates = mutableListOf<DisplayTrack>()
        // Mix: 4 recent favorites + 4 top recommendations
        heroCandidates.addAll(jumpBackInTracks.take(4))
        heroCandidates.addAll(rankedDiscovered.take(4))
        if (heroCandidates.size < 8) {
            val remaining = distinctSections.flatMap { it.songs }.distinctBy { it.id }
            heroCandidates.addAll(remaining.take(8 - heroCandidates.size))
        }

        val quickPicks = heroCandidates.distinctBy { it.id }.take(8).map { track ->
            DisplayQuickPick(
                id = track.id,
                title = track.title,
                subtitle = track.artist,
                thumbnailUrl = track.thumbnailUrl,
                track = track
            )
        }

        HomePageData(
            chips = collectedChips.distinctBy { it.title },
            quickPicks = quickPicks,
            sections = distinctSections,
            newReleases = newReleases
        )
    }

    private fun String.capitalizeWords(): String {
        return split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
}
