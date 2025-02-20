package eu.kanade.presentation.entries.anime

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import eu.kanade.domain.entries.TriStateFilter
import eu.kanade.domain.entries.anime.model.Anime
import eu.kanade.presentation.components.RadioItem
import eu.kanade.presentation.components.SortItem
import eu.kanade.presentation.components.TabbedDialog
import eu.kanade.presentation.components.TabbedDialogPaddings
import eu.kanade.presentation.components.TriStateItem
import eu.kanade.tachiyomi.R

@Composable
fun EpisodeSettingsDialog(
    onDismissRequest: () -> Unit,
    anime: Anime? = null,
    onDownloadFilterChanged: (TriStateFilter) -> Unit,
    onUnseenFilterChanged: (TriStateFilter) -> Unit,
    onBookmarkedFilterChanged: (TriStateFilter) -> Unit,
    onSortModeChanged: (Long) -> Unit,
    onDisplayModeChanged: (Long) -> Unit,
    onSetAsDefault: (applyToExistingAnime: Boolean) -> Unit,
) {
    var showSetAsDefaultDialog by rememberSaveable { mutableStateOf(false) }
    if (showSetAsDefaultDialog) {
        SetAsDefaultDialog(
            onDismissRequest = { showSetAsDefaultDialog = false },
            onConfirmed = onSetAsDefault,
        )
    }

    TabbedDialog(
        onDismissRequest = onDismissRequest,
        tabTitles = listOf(
            stringResource(R.string.action_filter),
            stringResource(R.string.action_sort),
            stringResource(R.string.action_display),
        ),
        tabOverflowMenuContent = { closeMenu ->
            DropdownMenuItem(
                text = { Text(stringResource(R.string.set_chapter_settings_as_default)) },
                onClick = {
                    showSetAsDefaultDialog = true
                    closeMenu()
                },
            )
        },
    ) { contentPadding, page ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(vertical = TabbedDialogPaddings.Vertical)
                .verticalScroll(rememberScrollState()),
        ) {
            when (page) {
                0 -> {
                    val forceDownloaded = anime?.forceDownloaded() == true
                    FilterPage(
                        downloadFilter = if (forceDownloaded) {
                            TriStateFilter.ENABLED_NOT
                        } else {
                            anime?.downloadedFilter
                        } ?: TriStateFilter.DISABLED,
                        onDownloadFilterChanged = onDownloadFilterChanged.takeUnless { forceDownloaded },
                        unseenFilter = anime?.unseenFilter ?: TriStateFilter.DISABLED,
                        onUnseenFilterChanged = onUnseenFilterChanged,
                        bookmarkedFilter = anime?.bookmarkedFilter ?: TriStateFilter.DISABLED,
                        onBookmarkedFilterChanged = onBookmarkedFilterChanged,
                    )
                }
                1 -> {
                    SortPage(
                        sortingMode = anime?.sorting ?: 0,
                        sortDescending = anime?.sortDescending() ?: false,
                        onItemSelected = onSortModeChanged,
                    )
                }
                2 -> {
                    DisplayPage(
                        displayMode = anime?.displayMode ?: 0,
                        onItemSelected = onDisplayModeChanged,
                    )
                }
            }
        }
    }
}

@Composable
private fun SetAsDefaultDialog(
    onDismissRequest: () -> Unit,
    onConfirmed: (optionalChecked: Boolean) -> Unit,
) {
    var optionalChecked by rememberSaveable { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = stringResource(R.string.episode_settings)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(text = stringResource(R.string.confirm_set_chapter_settings))

                Row(
                    modifier = Modifier
                        .clickable { optionalChecked = !optionalChecked }
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = optionalChecked,
                        onCheckedChange = null,
                    )
                    Text(text = stringResource(R.string.also_set_episode_settings_for_library))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(android.R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmed(optionalChecked)
                    onDismissRequest()
                },
            ) {
                Text(text = stringResource(android.R.string.ok))
            }
        },
    )
}

@Composable
private fun ColumnScope.FilterPage(
    downloadFilter: TriStateFilter,
    onDownloadFilterChanged: ((TriStateFilter) -> Unit)?,
    unseenFilter: TriStateFilter,
    onUnseenFilterChanged: (TriStateFilter) -> Unit,
    bookmarkedFilter: TriStateFilter,
    onBookmarkedFilterChanged: (TriStateFilter) -> Unit,
) {
    TriStateItem(
        label = stringResource(R.string.label_downloaded),
        state = downloadFilter,
        onClick = onDownloadFilterChanged,
    )
    TriStateItem(
        label = stringResource(R.string.action_filter_unseen),
        state = unseenFilter,
        onClick = onUnseenFilterChanged,
    )
    TriStateItem(
        label = stringResource(R.string.action_filter_bookmarked),
        state = bookmarkedFilter,
        onClick = onBookmarkedFilterChanged,
    )
}

@Composable
private fun ColumnScope.SortPage(
    sortingMode: Long,
    sortDescending: Boolean,
    onItemSelected: (Long) -> Unit,
) {
    SortItem(
        label = stringResource(R.string.sort_by_source),
        sortDescending = sortDescending.takeIf { sortingMode == Anime.EPISODE_SORTING_SOURCE },
        onClick = { onItemSelected(Anime.EPISODE_SORTING_SOURCE) },
    )
    SortItem(
        label = stringResource(R.string.sort_by_number),
        sortDescending = sortDescending.takeIf { sortingMode == Anime.EPISODE_SORTING_NUMBER },
        onClick = { onItemSelected(Anime.EPISODE_SORTING_NUMBER) },
    )
    SortItem(
        label = stringResource(R.string.sort_by_upload_date),
        sortDescending = sortDescending.takeIf { sortingMode == Anime.EPISODE_SORTING_UPLOAD_DATE },
        onClick = { onItemSelected(Anime.EPISODE_SORTING_UPLOAD_DATE) },
    )
}

@Composable
private fun ColumnScope.DisplayPage(
    displayMode: Long,
    onItemSelected: (Long) -> Unit,
) {
    RadioItem(
        label = stringResource(R.string.show_title),
        selected = displayMode == Anime.EPISODE_DISPLAY_NAME,
        onClick = { onItemSelected(Anime.EPISODE_DISPLAY_NAME) },
    )
    RadioItem(
        label = stringResource(R.string.show_episode_number),
        selected = displayMode == Anime.EPISODE_DISPLAY_NUMBER,
        onClick = { onItemSelected(Anime.EPISODE_DISPLAY_NUMBER) },
    )
}
