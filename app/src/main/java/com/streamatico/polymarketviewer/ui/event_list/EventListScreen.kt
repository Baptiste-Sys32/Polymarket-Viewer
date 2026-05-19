package com.streamatico.polymarketviewer.ui.event_list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.streamatico.polymarketviewer.R
import com.streamatico.polymarketviewer.data.model.gamma_api.BaseEventDto
import com.streamatico.polymarketviewer.data.model.gamma_api.TagDto
import com.streamatico.polymarketviewer.domain.repository.PolymarketEventsSortOrder
import com.streamatico.polymarketviewer.ui.event_list.components.EventListItem
import com.streamatico.polymarketviewer.ui.shared.UiError
import com.streamatico.polymarketviewer.ui.shared.components.AppLogoIcon
import com.streamatico.polymarketviewer.ui.shared.components.ErrorBox
import com.streamatico.polymarketviewer.ui.shared.components.ErrorBoxSecondaryAction
import com.streamatico.polymarketviewer.ui.shared.components.LoadingBox
import com.streamatico.polymarketviewer.ui.shared.components.MyScaffold
import com.streamatico.polymarketviewer.ui.tooling.PreviewMocks
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun EventListScreen(
    viewModel: EventListViewModel,
    onNavigateToEventDetail: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToSearch: () -> Unit,
) {
    InternalEventListScreen(
        viewModel = viewModel,
        onEventClick = {
            onNavigateToEventDetail(it.slug)
        },
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToAbout = onNavigateToAbout,
        onNavigateToSearch = onNavigateToSearch
    )
}

@Composable
fun SelectEventForWidgetScreen(
    onEventSelected: (event: BaseEventDto) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: EventListViewModel = koinViewModel()
) {
    InternalEventListScreen(
        viewModel = viewModel,
        onEventClick = onEventSelected,
        onNavigateToSettings = null,
        onNavigateToAbout = null,
        onNavigateToSearch = null,
        onNavigateBack = onNavigateBack
    )
}

@Composable
private fun InternalEventListScreen(
    viewModel: EventListViewModel,
    onEventClick: (event: BaseEventDto) -> Unit,
    onNavigateToSettings: (() -> Unit)?,
    onNavigateToAbout: (() -> Unit)?,
    onNavigateToSearch: (() -> Unit)?,
    onNavigateBack: (() -> Unit)? = null,
) {
    val uiState by viewModel.uiState.collectAsState()
    val tags by viewModel.tagsState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val canLoadMore by viewModel.canLoadMore.collectAsState()
    val selectedTagSlug by viewModel.selectedTagSlug.collectAsState()
    val selectedSortOrder by viewModel.selectedSortOrder.collectAsState()
    val areTagsLoading by viewModel.areTagsLoading.collectAsState()
    val watchlistIds by viewModel.watchlistIds.collectAsState()
    val hiddenFilterSlugs by viewModel.hiddenFilterSlugs.collectAsState()

    var showBottomSheet by remember { mutableStateOf(false) }

    EventListScreenContent(
        uiState = uiState,
        tags = tags,
        isRefreshing = isRefreshing,
        isLoadingMore = isLoadingMore,
        areTagsLoading = areTagsLoading,
        canLoadMore = canLoadMore,
        selectedTagSlug = selectedTagSlug,
        selectedSortOrder = selectedSortOrder,
        hiddenFilterSlugs = hiddenFilterSlugs,
        watchlistIds = watchlistIds,
        onRefresh = viewModel::refreshEvents,
        onLoadMore = viewModel::loadMoreEvents,
        onTagSelected = viewModel::selectTag,
        onBrowseFilterSelected = viewModel::selectBrowseFilter,
        onSortOrderSelected = viewModel::selectSortOrder,
        onToggleHiddenFilter = viewModel::toggleHiddenFilter,
        onClearHiddenFilters = viewModel::clearHiddenFilters,
        onRetry = viewModel::retryLoad,
        onToggleWatchlist = viewModel::toggleWatchlist,
        onEventClick = onEventClick,
        showBottomSheet = showBottomSheet,
        onShowSortOptionsClick = { showBottomSheet = true },
        onDismissRequest = { showBottomSheet = false },
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToAbout = onNavigateToAbout,
        onNavigateToSearch = onNavigateToSearch,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventListScreenContent(
    uiState: EventListUiState,
    tags: List<TagDto>,
    isRefreshing: Boolean,
    isLoadingMore: Boolean,
    areTagsLoading: Boolean,
    canLoadMore: Boolean,
    selectedTagSlug: String,
    selectedSortOrder: PolymarketEventsSortOrder,
    hiddenFilterSlugs: Set<String>,
    watchlistIds: Set<String>,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onTagSelected: (String) -> Unit,
    onBrowseFilterSelected: (String, PolymarketEventsSortOrder?) -> Unit,
    onSortOrderSelected: (PolymarketEventsSortOrder) -> Unit,
    onToggleHiddenFilter: (String) -> Unit,
    onClearHiddenFilters: () -> Unit,
    onRetry: () -> Unit,
    onToggleWatchlist: (String) -> Unit,
    onEventClick: (event: BaseEventDto) -> Unit,
    showBottomSheet: Boolean,
    onShowSortOptionsClick: () -> Unit,
    onDismissRequest: () -> Unit,
    onNavigateToSettings: (() -> Unit)?,
    onNavigateToAbout: (() -> Unit)?,
    onNavigateToSearch: (() -> Unit)?,
    onNavigateBack: (() -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()

    EventListContent(
        uiState = uiState,
        tags = tags,
        isRefreshing = isRefreshing,
        isLoadingMore = isLoadingMore,
        areTagsLoading = areTagsLoading,
        canLoadMore = canLoadMore,
        selectedTagSlug = selectedTagSlug,
        selectedSortOrder = selectedSortOrder,
        hiddenFilterSlugs = hiddenFilterSlugs,
        watchlistIds = watchlistIds,
        onRefresh = onRefresh,
        onLoadMore = onLoadMore,
        onTagSelected = onTagSelected,
        onBrowseFilterSelected = onBrowseFilterSelected,
        onRetry = onRetry,
        onToggleWatchlist = onToggleWatchlist,
        onEventClick = onEventClick,
        onShowSortOptionsClick = onShowSortOptionsClick,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToAbout = onNavigateToAbout,
        onSearchClick = onNavigateToSearch,
        onNavigateBack = onNavigateBack
    )

    if (showBottomSheet) {
        val sheetState = rememberModalBottomSheetState()

        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
        ) {
            MarketFiltersSheet(
                selectedTagSlug = selectedTagSlug,
                selectedSortOrder = selectedSortOrder,
                hiddenFilterSlugs = hiddenFilterSlugs,
                onBrowseFilterSelected = { slug, order ->
                    onBrowseFilterSelected(slug, order)
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) onDismissRequest()
                    }
                },
                onSortOrderSelected = onSortOrderSelected,
                onToggleHiddenFilter = onToggleHiddenFilter,
                onClearHiddenFilters = onClearHiddenFilters,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListContent(
    uiState: EventListUiState,
    tags: List<TagDto>,
    isRefreshing: Boolean,
    isLoadingMore: Boolean,
    areTagsLoading: Boolean,
    canLoadMore: Boolean,
    selectedTagSlug: String,
    selectedSortOrder: PolymarketEventsSortOrder,
    hiddenFilterSlugs: Set<String>,
    watchlistIds: Set<String>,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onTagSelected: (String) -> Unit,
    onBrowseFilterSelected: (String, PolymarketEventsSortOrder?) -> Unit,
    onRetry: () -> Unit,
    onToggleWatchlist: (String) -> Unit,
    onEventClick: (event: BaseEventDto) -> Unit,
    onShowSortOptionsClick: () -> Unit,
    onSearchClick: (() -> Unit)?,
    onNavigateBack: (() -> Unit)?,
    onNavigateToSettings: (() -> Unit)?,
    onNavigateToAbout: (() -> Unit)?
) {
    val gridState = rememberLazyGridState()
    val latestCanLoadMore by rememberUpdatedState(canLoadMore)
    val latestIsLoadingMore by rememberUpdatedState(isLoadingMore)
    val latestIsRefreshing by rememberUpdatedState(isRefreshing)
    val latestEventsCount by rememberUpdatedState((uiState as? EventListUiState.Success)?.events?.size ?: 0)

    var isGridDisplayed by remember { mutableStateOf(false) }
    LaunchedEffect(selectedTagSlug, selectedSortOrder) {
        if (isGridDisplayed) {
            gridState.animateScrollToItem(0)
        } else {
            isGridDisplayed = true
        }
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val drawerScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppMenuDrawer(
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToAbout = onNavigateToAbout,
                onDismiss = { drawerScope.launch { drawerState.close() } },
            )
        },
        gesturesEnabled = onNavigateBack == null,
    ) {
        MyScaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
            TopAppBar(
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(id = R.string.navigate_back)
                            )
                        }
                    } else {
                        AppLogoIcon(52.dp)
                    }
                 },
                title = {
                    val titleText = if (onNavigateBack != null) {
                        stringResource(R.string.widget_select_event)
                    } else {
                        stringResource(R.string.app_name)
                    }

                    Column {
                        Text(
                            titleText,
                            style = MaterialTheme.typography.headlineMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (onNavigateBack == null) {
                            Text(
                                text = "Live markets",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                },
                actions = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onShowSortOptionsClick) {
                            Icon(
                                imageVector = SiteFilterIcon,
                                contentDescription = stringResource(id = R.string.filter_tooltip)
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    TagsRow(
                        tags = tags,
                        areTagsLoading = areTagsLoading,
                        selectedTagSlug = selectedTagSlug,
                        onTagSelected = onTagSelected
                    )

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        when (uiState) {
                    is EventListUiState.Loading -> {
                        // Always show LoadingBox when uiState is Loading,
                        // regardless of tags loading status.
                        LoadingBox()
                    }
                    is EventListUiState.Success -> {
                        if (uiState.events.isEmpty() && !isLoadingMore) {
                            if (selectedTagSlug == POLYMARKET_EVENTS_SLUG_WATCHLIST) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.BookmarkBorder,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .alpha(0.7f)
                                            .size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = stringResource(R.string.watchlist_empty_title),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.watchlist_empty_message),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Text(stringResource(R.string.no_events_found))
                            }
                        } else {
                            PullToRefreshBox(
                                isRefreshing = isRefreshing,
                                onRefresh = onRefresh,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                val adaptiveInfo = currentWindowAdaptiveInfo()

                                // Determine number of columns for the grid
                                val gridCells = if(adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)) {
                                    GridCells.Adaptive(minSize = 300.dp) // Adaptive columns for Expanded width
                                } else {
                                    GridCells.Fixed(1)
                                }

                                LazyVerticalGrid(
                                    columns = gridCells,
                                    state = gridState,
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(
                                        items = uiState.events,
                                        key = { "${uiState.tagSlug}_${uiState.sortOrder}_${it.id}" }
                                    ) { event ->
                                        EventListItem(
                                            event = event,
                                            isInWatchlist = watchlistIds.contains(event.id),
                                            onToggleWatchlist = { onToggleWatchlist(event.id) },
                                            onClick = { onEventClick(event) }
                                        )
                                    }
                                    if (isLoadingMore) {
                                        item(span = { GridItemSpan(maxLineSpan) }) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                                horizontalArrangement = Arrangement.Center
                                            ) { CircularProgressIndicator() }
                                        }
                                    }
                                }

                                LaunchedEffect(gridState) {
                                    snapshotFlow { gridState.layoutInfo }
                                        .map { layoutInfo ->
                                            val visibleItemsInfo = layoutInfo.visibleItemsInfo
                                            if (visibleItemsInfo.isEmpty() || latestEventsCount == 0) {
                                                false
                                            } else {
                                                val lastVisibleItemIndex = visibleItemsInfo.lastOrNull()?.index ?: -1
                                                lastVisibleItemIndex != -1 && lastVisibleItemIndex >= latestEventsCount - LOAD_MORE_THRESHOLD
                                            }
                                        }
                                        .distinctUntilChanged()
                                        .filter { shouldLoadMore -> shouldLoadMore }
                                        .collect {
                                            if (latestCanLoadMore && !latestIsLoadingMore && !latestIsRefreshing) {
                                                onLoadMore()
                                            }
                                        }
                                }
                            }
                        }
                    }
                    is EventListUiState.Error -> {
                        val secondaryAction: ErrorBoxSecondaryAction? = if (uiState.showDnsSettings && onNavigateToSettings != null) {
                            ErrorBoxSecondaryAction(
                                label = stringResource(id = R.string.event_list_open_dns_settings),
                                onClick = onNavigateToSettings
                            )
                        } else {
                            null
                        }

                        ErrorBox(
                            error = uiState.error,
                            onRetry = onRetry,
                            secondaryAction = secondaryAction
                        )
                    }
                    }
                    }
                }

                if (onNavigateBack == null) {
                    PolymarketBottomNavigationBar(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        onHomeClick = { onBrowseFilterSelected(POLYMARKET_EVENTS_SLUG_ALL, PolymarketEventsSortOrder.DEFAULT_SORT_ORDER) },
                        onSearchClick = onSearchClick,
                        onFiltersClick = onShowSortOptionsClick,
                        onMenuClick = { drawerScope.launch { drawerState.open() } },
                    )
                }
            }
        }
    }
}

private const val LOAD_MORE_THRESHOLD = 5

@Composable
private fun PolymarketBottomNavigationBar(
    modifier: Modifier = Modifier,
    onHomeClick: () -> Unit,
    onSearchClick: (() -> Unit)?,
    onFiltersClick: () -> Unit,
    onMenuClick: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.26f)),
        tonalElevation = 0.dp,
        shadowElevation = 10.dp,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 32.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IslandNavIconButton(
                onClick = onHomeClick,
                icon = Icons.Filled.Home,
                contentDescription = "Home"
            )
            IslandNavIconButton(
                onClick = { onSearchClick?.invoke() },
                icon = Icons.Filled.Search,
                contentDescription = stringResource(id = R.string.search_tooltip),
                enabled = onSearchClick != null,
            )
            IslandNavIconButton(
                onClick = onFiltersClick,
                icon = SiteFilterIcon,
                contentDescription = stringResource(id = R.string.filter_tooltip)
            )
            IslandNavIconButton(
                onClick = onMenuClick,
                icon = Icons.Filled.Menu,
                contentDescription = stringResource(id = R.string.more_options_tooltip)
            )
        }
    }
}

@Composable
private fun IslandNavIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    enabled: Boolean = true,
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .alpha(if (enabled) 1f else 0.38f),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(21.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

private val SiteFilterIcon: ImageVector = ImageVector.Builder(
    name = "PolymarketSliders18",
    defaultWidth = 18.dp,
    defaultHeight = 18.dp,
    viewportWidth = 18f,
    viewportHeight = 18f
).apply {
    path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(13.25f, 5.25f)
        lineTo(16.25f, 5.25f)
        moveTo(1.75f, 5.25f)
        lineTo(8.75f, 5.25f)
        moveTo(4.75f, 12.75f)
        lineTo(1.75f, 12.75f)
        moveTo(16.25f, 12.75f)
        lineTo(9.25f, 12.75f)
        moveTo(13.25f, 5.25f)
        curveTo(13.25f, 6.49f, 12.24f, 7.5f, 11f, 7.5f)
        curveTo(9.76f, 7.5f, 8.75f, 6.49f, 8.75f, 5.25f)
        curveTo(8.75f, 4.01f, 9.76f, 3f, 11f, 3f)
        curveTo(12.24f, 3f, 13.25f, 4.01f, 13.25f, 5.25f)
        close()
        moveTo(9.25f, 12.75f)
        curveTo(9.25f, 13.99f, 8.24f, 15f, 7f, 15f)
        curveTo(5.76f, 15f, 4.75f, 13.99f, 4.75f, 12.75f)
        curveTo(4.75f, 11.51f, 5.76f, 10.5f, 7f, 10.5f)
        curveTo(8.24f, 10.5f, 9.25f, 11.51f, 9.25f, 12.75f)
        close()
    }
}.build()

@Composable
private fun AppMenuDrawer(
    onNavigateToSettings: (() -> Unit)?,
    onNavigateToAbout: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        if (onNavigateToAbout != null) {
            NavigationDrawerItem(
                icon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                label = { Text(stringResource(id = R.string.about_app_title)) },
                selected = false,
                onClick = {
                    onDismiss()
                    onNavigateToAbout.invoke()
                },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
        if (onNavigateToSettings != null) {
            NavigationDrawerItem(
                icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                label = { Text(stringResource(id = R.string.settings_menu_title)) },
                selected = false,
                onClick = {
                    onDismiss()
                    onNavigateToSettings.invoke()
                },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}

private fun getIconForSortOrder(orderKey: PolymarketEventsSortOrder): ImageVector {
    return when (orderKey) {
        PolymarketEventsSortOrder.VOLUME_24H -> Icons.AutoMirrored.Filled.TrendingUp
        PolymarketEventsSortOrder.VOLUME_TOTAL -> Icons.Filled.LocalFireDepartment
        PolymarketEventsSortOrder.LIQUIDITY -> Icons.Filled.WaterDrop
        PolymarketEventsSortOrder.NEWEST -> Icons.Filled.NewReleases
        PolymarketEventsSortOrder.ENDING_SOON -> Icons.Filled.HourglassBottom
        PolymarketEventsSortOrder.COMPETITIVE -> Icons.Filled.StarOutline
    }
}

@Composable
private fun MarketFiltersSheet(
    selectedTagSlug: String,
    selectedSortOrder: PolymarketEventsSortOrder,
    hiddenFilterSlugs: Set<String>,
    onBrowseFilterSelected: (String, PolymarketEventsSortOrder?) -> Unit,
    onSortOrderSelected: (PolymarketEventsSortOrder) -> Unit,
    onToggleHiddenFilter: (String) -> Unit,
    onClearHiddenFilters: () -> Unit,
) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        FilterSheetSectionTitle("Browse")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                FilterActionChip(
                    label = "New",
                    selected = selectedTagSlug == POLYMARKET_EVENTS_SLUG_NEW,
                    onClick = { onBrowseFilterSelected(POLYMARKET_EVENTS_SLUG_NEW, PolymarketEventsSortOrder.NEWEST) }
                )
            }
            item {
                FilterActionChip(
                    label = "Trending",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    selected = selectedTagSlug == POLYMARKET_EVENTS_SLUG_TRENDING,
                    onClick = { onBrowseFilterSelected(POLYMARKET_EVENTS_SLUG_TRENDING, PolymarketEventsSortOrder.VOLUME_24H) }
                )
            }
            item {
                FilterActionChip(
                    label = "Popular",
                    icon = Icons.Filled.LocalFireDepartment,
                    selected = selectedSortOrder == PolymarketEventsSortOrder.VOLUME_TOTAL && selectedTagSlug !in siteFeedSlugs,
                    onClick = { onSortOrderSelected(PolymarketEventsSortOrder.VOLUME_TOTAL) }
                )
            }
            item {
                FilterActionChip(
                    label = "Liquid",
                    icon = Icons.Filled.WaterDrop,
                    selected = selectedSortOrder == PolymarketEventsSortOrder.LIQUIDITY,
                    onClick = { onSortOrderSelected(PolymarketEventsSortOrder.LIQUIDITY) }
                )
            }
            item {
                FilterActionChip(
                    label = "Ending Soon",
                    icon = Icons.Filled.HourglassBottom,
                    selected = selectedSortOrder == PolymarketEventsSortOrder.ENDING_SOON,
                    onClick = { onSortOrderSelected(PolymarketEventsSortOrder.ENDING_SOON) }
                )
            }
            item {
                FilterActionChip(
                    label = "Competitive",
                    icon = Icons.Filled.StarOutline,
                    selected = selectedSortOrder == PolymarketEventsSortOrder.COMPETITIVE,
                    onClick = { onSortOrderSelected(PolymarketEventsSortOrder.COMPETITIVE) }
                )
            }
        }

        FilterSheetSectionTitle("Quick filters")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                FilterActionChip(
                    label = "All",
                    selected = selectedTagSlug == POLYMARKET_EVENTS_SLUG_ALL && hiddenFilterSlugs.isEmpty(),
                    onClick = {
                        onClearHiddenFilters()
                        onBrowseFilterSelected(POLYMARKET_EVENTS_SLUG_ALL, PolymarketEventsSortOrder.DEFAULT_SORT_ORDER)
                    }
                )
            }
            item {
                FilterActionChip(
                    label = "Active",
                    selected = true,
                    onClick = {}
                )
            }
            siteHiddenFilters.forEach { filter ->
                item {
                    FilterActionChip(
                        label = "Hide ${filter.label.lowercase()}",
                        selected = hiddenFilterSlugs.contains(filter.slug),
                        onClick = { onToggleHiddenFilter(filter.slug) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterSheetSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 14.dp, bottom = 8.dp)
    )
}

@Composable
private fun FilterActionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        leadingIcon = icon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
private fun TagsRow(
    tags: List<TagDto>,
    areTagsLoading: Boolean,
    selectedTagSlug: String,
    onTagSelected: (String) -> Unit
) {
    val watchlistTag = TagDto(id = POLYMARKET_EVENTS_SLUG_WATCHLIST, label = stringResource(R.string.tag_watchlist), slug = POLYMARKET_EVENTS_SLUG_WATCHLIST, forceShow = true)
    val allTag = TagDto(id = POLYMARKET_EVENTS_SLUG_ALL, label = stringResource(R.string.tag_all), slug = POLYMARKET_EVENTS_SLUG_ALL, forceShow = true)
    val siteFeedTags = listOf(
        TagDto(id = "hardcoded-trending", label = "Trending", slug = POLYMARKET_EVENTS_SLUG_TRENDING, forceShow = true),
        TagDto(id = "hardcoded-breaking", label = "Breaking", slug = POLYMARKET_EVENTS_SLUG_BREAKING, forceShow = true),
        TagDto(id = "hardcoded-new", label = "New", slug = POLYMARKET_EVENTS_SLUG_NEW, forceShow = true),
    )
    val hardcodedTags = buildList {
        add(TagDto(id = "hardcoded-politics", label = "Politics", slug = "politics", forceShow = true))
        add(TagDto(id = "hardcoded-sports", label = "Sports", slug = "sports", forceShow = true))
        add(TagDto(id = "hardcoded-crypto", label = "Crypto", slug = "crypto", forceShow = true))
        add(TagDto(id = "hardcoded-tech", label = "Tech", slug = "tech", forceShow = true))
        val aiTag = tags.firstOrNull { it.slug.equals("ai", ignoreCase = true) }
        add(TagDto(id = "hardcoded-ai", label = aiTag?.label ?: "AI", slug = aiTag?.slug ?: "ai", forceShow = true))
        tags.firstOrNull { it.slug.equals("dead-by-daylight", ignoreCase = true) }?.let {
            add(TagDto(id = "hardcoded-dead-by-daylight", label = "Dead by Daylight", slug = it.slug, forceShow = true))
        }
    }
    val pinnedTagSlugs = (listOf(watchlistTag, allTag) + siteFeedTags + hardcodedTags).map { it.slug.lowercase() }.toSet()
    val uniqueTags = tags.filter { it.slug.lowercase() !in pinnedTagSlugs }
    val displayTags = listOf(watchlistTag, allTag) + siteFeedTags + hardcodedTags + uniqueTags

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(FilterChipDefaults.Height + 12.dp * 2),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (areTagsLoading) {
            item { // Use item within LazyRow to place the indicator
                Box(
                    modifier = Modifier
                        .fillParentMaxWidth() // Take full width available in LazyRow
                        .height(FilterChipDefaults.Height + 8.dp * 2), // Match height
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        } else {
            // Existing items call when not loading
            items(items = displayTags, key = { it.slug }) { tag ->
                TagChip(
                    tag = tag,
                    isSelected = selectedTagSlug == tag.slug,
                    onClick = { onTagSelected(tag.slug) },
                )
            }
        }
    }
}

@Composable
private fun TagChip(
    tag: TagDto,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val leadingIcon = tagIconFor(tag.slug, isSelected)

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        label = {
            Text(tag.label)
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

private fun tagIconFor(tagSlug: String, isSelected: Boolean): ImageVector? {
    return when (tagSlug) {
        POLYMARKET_EVENTS_SLUG_WATCHLIST -> if (isSelected) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder
        POLYMARKET_EVENTS_SLUG_TRENDING -> Icons.AutoMirrored.Filled.TrendingUp
        POLYMARKET_EVENTS_SLUG_BREAKING -> Icons.Filled.LocalFireDepartment
        else -> null
    }
}

private data class HiddenSiteFilter(val label: String, val slug: String)

private val siteFeedSlugs = setOf(
    POLYMARKET_EVENTS_SLUG_TRENDING,
    POLYMARKET_EVENTS_SLUG_BREAKING,
    POLYMARKET_EVENTS_SLUG_NEW,
)

private val siteHiddenFilters = listOf(
    HiddenSiteFilter(label = "Sports", slug = "sports"),
    HiddenSiteFilter(label = "Crypto", slug = "crypto"),
    HiddenSiteFilter(label = "Earnings", slug = "earnings"),
)


// ====== Preview ========

@Composable
private fun TemplateEventListScreenPreview_Success(isSelectWidgetMode: Boolean) {
    val sampleEvents = listOf(
        PreviewMocks.sampleEvent1.copy(id = "1", title = "Will event 1 happen?", slug = "event-1", volume = 100.0, liquidity = 50.0, featured = false),
        PreviewMocks.sampleBinaryEvent.copy(id = "2", title = "What about event 2?", slug = "event-2", volume = 200.0, liquidity = 100.0, featured = true, featuredOrder = 1),
        PreviewMocks.sampleEvent1.copy(id = "3", title = "A third event?", slug = "event-3", volume = 300.0, liquidity = 150.0, featured = false)
    )
    val sampleTags = listOf(
        TagDto(id = "tag1", label = "Politics", slug = "politics", forceShow = false),
        TagDto(id = "tag2", label = "Crypto", slug = "crypto", forceShow = false),
        TagDto(id = "tag3", label = "Sports", slug = "sports", forceShow = false)
    )

    val onNavigateToSearch: (() -> Unit)?
    val onNavigateBack: (() -> Unit)?
    val onNavigateToSettings: (() -> Unit)?
    val onNavigateToAbout: (() -> Unit)?

    if (isSelectWidgetMode) {
        onNavigateToSearch = null
        onNavigateBack = {}
        onNavigateToSettings = null
        onNavigateToAbout = null
    } else {
        onNavigateToSearch = {}
        onNavigateBack = null
        onNavigateToSettings = {}
        onNavigateToAbout = {}
    }

    MaterialTheme {
        EventListScreenContent(
            uiState = EventListUiState.Success(sampleEvents),
            tags = sampleTags,
            isRefreshing = false,
            isLoadingMore = false,
            areTagsLoading = false,
            canLoadMore = true,
            selectedTagSlug = "crypto",
            selectedSortOrder = PolymarketEventsSortOrder.NEWEST,
            hiddenFilterSlugs = emptySet(),
            watchlistIds = emptySet(),
            onRefresh = {},
            onLoadMore = {},
            onTagSelected = {},
            onBrowseFilterSelected = { _, _ -> },
            onSortOrderSelected = {},
            onToggleHiddenFilter = {},
            onClearHiddenFilters = {},
            onRetry = {},
            onToggleWatchlist = {},
            onEventClick = {},
            showBottomSheet = false,
            onShowSortOptionsClick = {},
            onDismissRequest = {},
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToAbout = onNavigateToAbout,
            onNavigateToSearch = onNavigateToSearch,
            onNavigateBack = onNavigateBack
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EventListScreenPreview_Success() {
    TemplateEventListScreenPreview_Success(false)
}

@Preview(showBackground = true)
@Composable
private fun WidgetSelectEventListScreenPreview_Success() {
    TemplateEventListScreenPreview_Success(true)
}

@Preview(showBackground = true, name = "Loading State")
@Composable
private fun EventListScreenPreview_Loading() {
    MaterialTheme {
        EventListScreenContent(
            uiState = EventListUiState.Loading,
            tags = emptyList(),
            isRefreshing = false,
            isLoadingMore = false,
            areTagsLoading = false,
            canLoadMore = false,
            selectedTagSlug = POLYMARKET_EVENTS_SLUG_ALL,
            selectedSortOrder = PolymarketEventsSortOrder.DEFAULT_SORT_ORDER,
            hiddenFilterSlugs = emptySet(),
            watchlistIds = emptySet(),
            onRefresh = {},
            onLoadMore = {},
            onTagSelected = {},
            onBrowseFilterSelected = { _, _ -> },
            onSortOrderSelected = {},
            onToggleHiddenFilter = {},
            onClearHiddenFilters = {},
            onRetry = {},
            onToggleWatchlist = {},
            onEventClick = {},
            showBottomSheet = false,
            onShowSortOptionsClick = {},
            onDismissRequest = {},
            onNavigateToSettings = {},
            onNavigateToAbout = {},
            onNavigateToSearch = {},
        )
    }
}

@Preview(showBackground = true, name = "Empty Watchlist")
@Composable
private fun EventListScreenPreview_EmptyWatchlist() {
    MaterialTheme {
        EventListScreenContent(
            uiState = EventListUiState.Success(
                emptyList()
            ),
            tags = emptyList(),
            isRefreshing = false,
            isLoadingMore = false,
            areTagsLoading = false,
            canLoadMore = false,
            selectedTagSlug = POLYMARKET_EVENTS_SLUG_WATCHLIST,
            selectedSortOrder = PolymarketEventsSortOrder.DEFAULT_SORT_ORDER,
            hiddenFilterSlugs = emptySet(),
            watchlistIds = emptySet(),
            onRefresh = {},
            onLoadMore = {},
            onTagSelected = {},
            onBrowseFilterSelected = { _, _ -> },
            onSortOrderSelected = {},
            onToggleHiddenFilter = {},
            onClearHiddenFilters = {},
            onRetry = {},
            onToggleWatchlist = {},
            onEventClick = {},
            showBottomSheet = false,
            onShowSortOptionsClick = {},
            onDismissRequest = {},
            onNavigateToSettings = {},
            onNavigateToAbout = {},
            onNavigateToSearch = {},
        )
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
private fun EventListScreenPreview_Error() {
    MaterialTheme {
        EventListScreenContent(
            uiState = EventListUiState.Error(
                UiError(
                    title = "Failed to load events",
                    details = "Please check your connection and try again."
                )
            ),
            tags = emptyList(),
            isRefreshing = false,
            isLoadingMore = false,
            areTagsLoading = false,
            canLoadMore = false,
            selectedTagSlug = POLYMARKET_EVENTS_SLUG_ALL,
            selectedSortOrder = PolymarketEventsSortOrder.DEFAULT_SORT_ORDER,
            hiddenFilterSlugs = emptySet(),
            watchlistIds = emptySet(),
            onRefresh = {},
            onLoadMore = {},
            onTagSelected = {},
            onBrowseFilterSelected = { _, _ -> },
            onSortOrderSelected = {},
            onToggleHiddenFilter = {},
            onClearHiddenFilters = {},
            onRetry = {},
            onToggleWatchlist = {},
            onEventClick = {},
            showBottomSheet = false,
            onShowSortOptionsClick = {},
            onDismissRequest = {},
            onNavigateToSettings = {},
            onNavigateToAbout = {},
            onNavigateToSearch = {},
        )
    }
}

@Preview(showBackground = true, name = "Error State With DNS Hint")
@Composable
private fun EventListScreenPreview_ErrorWithDnsHint() {
    MaterialTheme {
        EventListScreenContent(
            uiState = EventListUiState.Error(
                UiError(
                    title = "Failed to load events",
                    details = "Please check your connection and try again."
                )
            ),
            tags = emptyList(),
            isRefreshing = false,
            isLoadingMore = false,
            areTagsLoading = false,
            canLoadMore = false,
            selectedTagSlug = POLYMARKET_EVENTS_SLUG_ALL,
            selectedSortOrder = PolymarketEventsSortOrder.DEFAULT_SORT_ORDER,
            hiddenFilterSlugs = emptySet(),
            watchlistIds = emptySet(),
            onRefresh = {},
            onLoadMore = {},
            onTagSelected = {},
            onBrowseFilterSelected = { _, _ -> },
            onSortOrderSelected = {},
            onToggleHiddenFilter = {},
            onClearHiddenFilters = {},
            onRetry = {},
            onToggleWatchlist = {},
            onEventClick = {},
            showBottomSheet = false,
            onShowSortOptionsClick = {},
            onDismissRequest = {},
            onNavigateToSettings = {},
            onNavigateToAbout = {},
            onNavigateToSearch = {},
        )
    }
}
