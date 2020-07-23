package org.mozilla.reference.browser.storage.history

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.VisibleForTesting
import mozilla.components.concept.storage.*
import mozilla.components.support.utils.StorageUtils.levenshteinDistance
import mozilla.components.support.utils.segmentAwareDomainMatch
import java.io.*
import java.util.*

data class Visit(val timestamp: Long, val type: VisitType): Serializable
data class PageObservation(val title: String?) : Serializable

const val AUTOCOMPLETE_SOURCE_NAME = "history"

class History(val context: Context) : HistoryStorage {

    @VisibleForTesting
    internal var pages: HashMap<String, MutableList<Visit>> = linkedMapOf()
    @VisibleForTesting
    internal var pageMeta: HashMap<String, PageObservation> = hashMapOf()

    override suspend fun recordVisit(uri: String, visit: PageVisit) {
        val now = System.currentTimeMillis()
        if (visit.redirectSource != RedirectSource.NOT_A_SOURCE) {
            return
        }

        if (uri.startsWith("https://www.qwant.com")) {
            return
        }

        synchronized(pages) {
            if (!pages.containsKey(uri)) {
                pages[uri] = mutableListOf(Visit(now, visit.visitType))
            } else {
                pages[uri]!!.add(Visit(now, visit.visitType))
            }
        }
    }

    override suspend fun recordObservation(uri: String, observation: mozilla.components.concept.storage.PageObservation) = synchronized(pageMeta) {
        pageMeta[uri] = PageObservation(observation.title)
    }

    override suspend fun getVisited(uris: List<String>): List<Boolean> = synchronized(pages) {
        return uris.map {
            if (pages[it] != null && pages[it]!!.size > 0) {
                return@map true
            }
            return@map false
        }
    }

    override suspend fun getVisited(): List<String> = synchronized(pages) {
        return pages.keys.toList()
    }

    override suspend fun getVisitsPaginated(offset: Long, count: Long, excludeTypes: List<VisitType>): List<VisitInfo> {
        val visits = mutableListOf<VisitInfo>()

        var i = 0
        val endIndex = offset + count

        val sortedPages = pages.toList().sortedByDescending { (_, allVisits) ->
            allVisits.sortByDescending { visit -> visit.timestamp }
            allVisits[0].timestamp
        }.toMap()

        sortedPages.forEach {
            if (i in offset until endIndex) {
                it.value.sortByDescending { visit -> visit.timestamp }
                visits.add(VisitInfo(
                    url = it.key,
                    title = pageMeta[it.key]?.title,
                    visitTime = it.value[0].timestamp,
                    visitType = it.value[0].type
                ))
            } else if (i >= endIndex) {
                return visits
            }
            i++
        }

        return visits
    }

    override suspend fun getDetailedVisits(
            start: Long,
            end: Long,
            excludeTypes: List<VisitType>
    ): List<VisitInfo> = synchronized(pages + pageMeta) {

        val visits = mutableListOf<VisitInfo>()

        pages.forEach {
            it.value.forEach { visit ->
                if (visit.timestamp >= start && visit.timestamp <= end && !excludeTypes.contains(visit.type)) {
                    visits.add(VisitInfo(
                            url = it.key,
                            title = pageMeta[it.key]?.title,
                            visitTime = visit.timestamp,
                            visitType = visit.type
                    ))
                }
            }
        }

        return visits
    }

    override fun getSuggestions(query: String, limit: Int): List<SearchResult> = synchronized(pages + pageMeta) {
        data class Hit(val url: String, val score: Int)

        val urlMatches = pages.asSequence().map {
            Hit(it.key, levenshteinDistance(it.key, query))
        }
        val titleMatches = pageMeta.asSequence().map {
            Hit(it.key, levenshteinDistance(it.value.title ?: "", query))
        }
        val matchedUrls = mutableMapOf<String, Int>()
        urlMatches.plus(titleMatches).forEach {
            if (matchedUrls.containsKey(it.url) && matchedUrls[it.url]!! < it.score) {
                matchedUrls[it.url] = it.score
            } else {
                matchedUrls[it.url] = it.score
            }
        }
        // Calculate maxScore so that we can invert our scoring.
        // Lower Levenshtein distance should produce a higher score.
        val maxScore = urlMatches.maxBy { it.score }?.score ?: return@synchronized listOf()

        // TODO exclude non-matching results entirely? Score that implies complete mismatch.
        matchedUrls.asSequence().sortedBy { it.value }.map {
            SearchResult(id = it.key, score = maxScore - it.value, url = it.key, title = pageMeta[it.key]?.title)
        }.take(limit).toList()
    }

    override suspend fun getTopFrecentSites(numItems: Int): List<TopFrecentSiteInfo> {
        throw UnsupportedOperationException("Pagination is not yet supported by the in-memory history storage")
    }

    override fun getAutocompleteSuggestion(query: String): HistoryAutocompleteResult? = synchronized(pages) {
        segmentAwareDomainMatch(query, pages.keys)?.let { urlMatch ->
            return HistoryAutocompleteResult(
                    query, urlMatch.matchedSegment, urlMatch.url, AUTOCOMPLETE_SOURCE_NAME, pages.size)
        }
    }

    override suspend fun deleteEverything() = synchronized(pages + pageMeta) {
        pages.clear()
        pageMeta.clear()
    }

    override suspend fun deleteVisitsSince(since: Long) = synchronized(pages) {
        pages.entries.forEach {
            it.setValue(it.value.filterNot { visit -> visit.timestamp >= since }.toMutableList())
        }
        pages = pages.filter { it.value.isNotEmpty() } as HashMap<String, MutableList<Visit>>
    }

    override suspend fun deleteVisitsBetween(startTime: Long, endTime: Long) = synchronized(pages) {
        pages.entries.forEach {
            it.setValue(it.value.filterNot { visit ->
                visit.timestamp >= startTime && visit.timestamp <= endTime
            }.toMutableList())
        }
        pages = pages.filter { it.value.isNotEmpty() } as HashMap<String, MutableList<Visit>>
    }

    override suspend fun deleteVisitsFor(url: String) = synchronized(pages + pageMeta) {
        pages.remove(url)
        pageMeta.remove(url)
        Unit
    }

    override suspend fun deleteVisit(url: String, timestamp: Long) = synchronized(pages) {
        if (pages.containsKey(url)) {
            pages[url] = pages[url]!!.filter { it.timestamp != timestamp }.toMutableList()
        }
    }

    override suspend fun prune() {
        // Not applicable.
    }

    override suspend fun runMaintenance() {
        // Not applicable.
    }

    override suspend fun warmUp() {
        this.restore()
    }

    override fun cleanup() {
        this.persist()
        // GC will take care of our internal data structures, so there's nothing to do here.
    }

    fun setupAutoPersist(delayMs: Long = 20000) {
        Log.d("QWANT_BROWSER", "autopersist history setup")
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                Log.d("QWANT_BROWSER", "autopersist history")
                this@History.persist()
                mainHandler.postDelayed(this, delayMs)
            }
        })
    }

    fun persist() {
        Log.d("QWANT_BROWSER", "persist history: ${pages.size} pages / ${pageMeta.size} metas")
        try {
            val fileOutputStream: FileOutputStream = context.openFileOutput(QWANT_HISTORY_FILENAME, Context.MODE_PRIVATE)
            val objectOutputStream = ObjectOutputStream(fileOutputStream)
            objectOutputStream.writeObject(this.pages)
            objectOutputStream.writeObject(this.pageMeta)
            objectOutputStream.flush()
            objectOutputStream.close()
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun restore() {
        Log.d("QWANT_BROWSER", "restore history !")
        try {
            val fileInputStream: FileInputStream = context.openFileInput(QWANT_HISTORY_FILENAME)
            val objectInputStream = ObjectInputStream(fileInputStream)
            this.pages = objectInputStream.readObject() as HashMap<String, MutableList<Visit>>
            this.pageMeta = objectInputStream.readObject() as HashMap<String, PageObservation>
            objectInputStream.close()
            fileInputStream.close()
            Log.d("QWANT_BROWSER", "history restored: ${pages.size} pages / ${pageMeta.size} metas")
        } catch (e: IOException) {
            Log.e("QWANT_BROWSER", "Failed reading history file: IO exception: " + e.message)
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            Log.e("QWANT_BROWSER", "Failed reading history file: Class not found: " + e.message)
            e.printStackTrace()
        } catch (e: Exception) {
            Log.e("QWANT_BROWSER", "Failed reading history file: " + e.message)
            e.printStackTrace()
        }
    }

    companion object {
        const val QWANT_HISTORY_FILENAME = "qwant_history"
    }
}
