package eu.kanade.domain.source.manga.interactor

import eu.kanade.domain.source.manga.model.Source
import eu.kanade.domain.source.manga.repository.MangaSourceRepository
import eu.kanade.domain.source.service.SetMigrateSorting
import eu.kanade.domain.source.service.SourcePreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.Collator
import java.util.Collections
import java.util.Locale

class GetMangaSourcesWithFavoriteCount(
    private val repository: MangaSourceRepository,
    private val preferences: SourcePreferences,
) {

    fun subscribe(): Flow<List<Pair<Source, Long>>> {
        return combine(
            preferences.migrationSortingDirection().changes(),
            preferences.migrationSortingMode().changes(),
            repository.getMangaSourcesWithFavoriteCount(),
        ) { direction, mode, list ->
            list.sortedWith(sortFn(direction, mode))
        }
    }

    private fun sortFn(
        direction: SetMigrateSorting.Direction,
        sorting: SetMigrateSorting.Mode,
    ): java.util.Comparator<Pair<Source, Long>> {
        val locale = Locale.getDefault()
        val collator = Collator.getInstance(locale).apply {
            strength = Collator.PRIMARY
        }
        val sortFn: (Pair<Source, Long>, Pair<Source, Long>) -> Int = { a, b ->
            when (sorting) {
                SetMigrateSorting.Mode.ALPHABETICAL -> {
                    when {
                        a.first.isStub && b.first.isStub.not() -> -1
                        b.first.isStub && a.first.isStub.not() -> 1
                        else -> collator.compare(a.first.name.lowercase(locale), b.first.name.lowercase(locale))
                    }
                }
                SetMigrateSorting.Mode.TOTAL -> {
                    when {
                        a.first.isStub && b.first.isStub.not() -> -1
                        b.first.isStub && a.first.isStub.not() -> 1
                        else -> a.second.compareTo(b.second)
                    }
                }
            }
        }

        return when (direction) {
            SetMigrateSorting.Direction.ASCENDING -> Comparator(sortFn)
            SetMigrateSorting.Direction.DESCENDING -> Collections.reverseOrder(sortFn)
        }
    }
}
