package com.streamatico.polymarketviewer.ui.event_list.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.streamatico.polymarketviewer.R
import com.streamatico.polymarketviewer.data.model.gamma_api.BaseEventDto
import com.streamatico.polymarketviewer.data.model.gamma_api.BaseMarketDto
import com.streamatico.polymarketviewer.data.model.gamma_api.EventDto
import com.streamatico.polymarketviewer.data.model.gamma_api.MarketResolutionStatus
import com.streamatico.polymarketviewer.domain.model.EventType
import com.streamatico.polymarketviewer.ui.shared.MarketBadgeState
import com.streamatico.polymarketviewer.ui.shared.MarketDisplayRow
import com.streamatico.polymarketviewer.ui.shared.UiFormatter
import com.streamatico.polymarketviewer.ui.shared.toDisplayRows
import com.streamatico.polymarketviewer.ui.theme.PolymarketAppTheme
import com.streamatico.polymarketviewer.ui.tooling.PreviewMocks

// Constant for maximum number of visible markets
private const val MAX_VISIBLE_MARKETS = 3
private val OutcomeOrange = Color(0xFFFF8A00)
private val OutcomeBlue = Color(0xFF4EA8FF)
private val NewYellow = Color(0xFFF6F100)
private val YesPillBackground = Color(0xFF0B3D2E)
private val YesPillText = Color(0xFF00C878)
private val NoPillBackground = Color(0xFF48131E)
private val NoPillText = Color(0xFFFF3158)
private val LiveRed = Color(0xFFFF2438)

/**
 * Composable for displaying one Event (Event) and its nested Markets.
 */
@Composable
fun EventListItem(
    event: BaseEventDto,
    isInWatchlist: Boolean = false,
    onToggleWatchlist: () -> Unit = {},
    onClick: () -> Unit // New parameter for clicking on the entire card
) {
    // State to track whether the market/outcome list is expanded
    var isMarketListExpanded by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .alpha(if (event.closed) 0.7f else 1f)
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.34f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(start = 14.dp, top = 14.dp, end = 50.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    if (event.imageUrl != null) {
                        AsyncImage(
                            model = event.imageUrl,
                            contentDescription = event.title,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            event.volume?.let {
                                Text(
                                    text = UiFormatter.formatLargeValueUsd(it, suffix = stringResource(R.string.volume_suffix)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))

                // --- Market Content (choose display style) --- //
                when (event.eventType) {
                    EventType.BinaryEvent -> {
                        val market = remember(event) { event.toDisplayRows().first() }
                        if (event.isLiveCryptoUpDownEvent()) {
                            LiveCryptoBinaryContent(
                                event = event,
                                market = event.baseMarkets.firstOrNull(),
                                displayRow = market
                            )
                        } else {
                            BinaryMarketContent(market = market)
                        }
                    }

                    EventType.CategoricalMarket -> {
                        val rows = remember(event) { event.toDisplayRows() }
                        CategoricalMarketContent(
                            rows = rows,
                            isExpanded = isMarketListExpanded,
                            onToggleExpand = { isMarketListExpanded = !isMarketListExpanded }
                        )
                    }

                    EventType.MultiMarket -> {
                        val sortedMarkets = remember(event, isMarketListExpanded) {
                            if (isMarketListExpanded || event.baseMarkets.size <= (MAX_VISIBLE_MARKETS + 1)) {
                                //event.baseMarkets.sortedByViewPriority(event.sortByEnum)
                                event.toDisplayRows()
                            } else {
                                //event.baseMarkets
                                //    .sortedForShortView(MAX_VISIBLE_MARKETS)
                                event.toDisplayRows(MAX_VISIBLE_MARKETS)
                            }
                        }

                        MultiMarketContent(
                            markets = sortedMarkets,
                            totalMarketsSize = event.baseMarkets.size,
                            isExpanded = isMarketListExpanded, // Pass state
                            onToggleExpand = {
                                isMarketListExpanded = !isMarketListExpanded
                            }
                        )
                    }
                }

                EventMetaPills(event = event)
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Watchlist Button
            IconButton(
                onClick = onToggleWatchlist,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = if (isInWatchlist) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = stringResource(if (isInWatchlist) R.string.cd_remove_from_watchlist else R.string.cd_add_to_watchlist),
                    tint = if (isInWatchlist) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EventMetaPills(event: BaseEventDto) {
    val isNew = (event as? EventDto)?.new == true

    if (!isNew) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = SparkleIcon,
            contentDescription = null,
            tint = NewYellow,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "NOUVEAU",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = NewYellow,
            maxLines = 1
        )
    }
}

private val SparkleIcon: ImageVector = ImageVector.Builder(
    name = "PolymarketSparkle",
    defaultWidth = 12.dp,
    defaultHeight = 12.dp,
    viewportWidth = 12f,
    viewportHeight = 12f
).apply {
    path(fill = androidx.compose.ui.graphics.SolidColor(Color.Black)) {
        moveTo(6f, 0.8f)
        lineTo(7.2f, 4.8f)
        lineTo(11.2f, 6f)
        lineTo(7.2f, 7.2f)
        lineTo(6f, 11.2f)
        lineTo(4.8f, 7.2f)
        lineTo(0.8f, 6f)
        lineTo(4.8f, 4.8f)
        close()
    }
}.build()

@Composable
private fun MarketBadgeState.toEventListBadgeText(): String {
    return UiFormatter.getMarketBadgeText(LocalContext.current, this)
}

@Composable
private fun MarketBadgeState.toEventListBadgeColor() = when (this) {
    MarketBadgeState.Resolved -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
    MarketBadgeState.Locked -> MaterialTheme.colorScheme.error
    MarketBadgeState.Resolving,
    is MarketBadgeState.ActiveEndsOnDate,
    MarketBadgeState.Unknown -> MaterialTheme.colorScheme.outline
}

// --- Composable for "Binary market" style --- //
@Composable
private fun BinaryMarketContent(
    market: MarketDisplayRow
) {
    val yesPrice = market.price
    val noPrice = yesPrice?.let { 1.0 - it }
    val isYesLeading = yesPrice != null && noPrice != null && yesPrice >= noPrice

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutcomeTextRow(
                title = market.title,
                marketResolutionStatus = market.resolutionStatus,
                leadingColor = OutcomeOrange,
                isEmphasized = isYesLeading,
                modifier = Modifier.weight(1f) // Takes remaining space
            )
            yesPrice?.let {
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = UiFormatter.formatPriceAsPercentage(it),
                    style = if (isYesLeading) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
                    fontWeight = if (isYesLeading) FontWeight.ExtraBold else FontWeight.Bold,
                    color = OutcomeOrange
                )
            }
        }
        yesPrice?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutcomeTextRow(
                    title = "No",
                    marketResolutionStatus = market.resolutionStatus,
                    leadingColor = OutcomeBlue,
                    isEmphasized = !isYesLeading,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = UiFormatter.formatPriceAsPercentage(1.0 - it),
                    style = if (isYesLeading) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
                    fontWeight = if (isYesLeading) FontWeight.Bold else FontWeight.ExtraBold,
                    color = OutcomeBlue
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            DualDistributionBar(
                firstFraction = it,
                secondFraction = 1.0 - it,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// --- Composable for SINGLE CATEGORICAL market --- //
@Composable
private fun CategoricalMarketContent(
    rows: List<MarketDisplayRow>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        val visibleRows = if (isExpanded || rows.size == MAX_VISIBLE_MARKETS + 1) {
            rows
        } else {
            rows.take(MAX_VISIBLE_MARKETS)
        }
        val leadingIndex = if (rows.size == 2) {
            rows.indices.maxByOrNull { rows[it].price ?: -1.0 }
        } else {
            null
        }

        visibleRows.forEachIndexed { index, row ->
            MarketRow(
                title = row.title,
                price = row.price,
                marketResolutionStatus = row.resolutionStatus,
                resolvedOutcome = row.resolvedOutcome,
                leadingColor = if (rows.size == 2) distributionColorFor(index) else null,
                isLeadingOutcome = index == leadingIndex,
                showOutcomePills = false
            )
        }

        if (rows.size == 2) {
            TwoOutcomeDistributionBar(rows = rows)
        }

        if (rows.size > MAX_VISIBLE_MARKETS + 1) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isExpanded) stringResource(R.string.action_show_less) else {
                    val count = rows.size - MAX_VISIBLE_MARKETS
                    pluralStringResource(R.plurals.more_outcomes_count, count, count)
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onToggleExpand() }
            )
        }
    }
}

// --- Composable for MULTIPLE BINARY markets (elections) --- //
@Composable
private fun MultiMarketContent(
    markets: List<MarketDisplayRow>,
    totalMarketsSize: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    if(markets.isEmpty()){
        Text(stringResource(R.string.error_no_market_data), Modifier.padding(16.dp))
        return
    }

    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        val leadingIndex = if (totalMarketsSize == 2 && markets.size == 2) {
            markets.indices.maxByOrNull { markets[it].price ?: -1.0 }
        } else {
            null
        }

        markets.forEachIndexed { index, market ->
            // Use universal MarketRow
            MarketRow(
                title = market.title,
                price = market.price,
                marketResolutionStatus = market.resolutionStatus,
                resolvedOutcome = market.resolvedOutcome,
                leadingColor = if (totalMarketsSize == 2 && markets.size == 2) distributionColorFor(index) else null,
                isLeadingOutcome = index == leadingIndex,
                showOutcomePills = isExpanded && market.resolutionStatus != MarketResolutionStatus.RESOLVED
            )
        }

        if (totalMarketsSize == 2 && markets.size == 2) {
            TwoOutcomeDistributionBar(rows = markets)
        }

        // Show button only if there are more than 4 markets (more than 1 hidden)
        if (isExpanded || markets.size < totalMarketsSize) {
            val moreText: String
            if(isExpanded) {
                moreText = stringResource(R.string.action_show_less)
            } else {
                val remainingCount = totalMarketsSize - markets.size
                moreText = pluralStringResource(R.plurals.more_outcomes_count, remainingCount, remainingCount)
            }

            // Display "+ X more markets"
             Spacer(modifier = Modifier.height(4.dp))
             Text(
                text = moreText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary, // Make color clickable
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onToggleExpand() } // Make clickable
             )
        }
    }
}

@Composable
private fun LiveCryptoBinaryContent(
    event: BaseEventDto,
    market: BaseMarketDto?,
    displayRow: MarketDisplayRow,
) {
    val upPrice = displayRow.price
    val downPrice = upPrice?.let { 1.0 - it }
    val upLabel = market?.outcomes?.firstOrNull { it.equals("up", ignoreCase = true) } ?: "Up"
    val downLabel = market?.outcomes?.firstOrNull { it.equals("down", ignoreCase = true) } ?: "Down"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOfNotNull(
                    market?.oneDayPriceChange,
                    market?.oneWeekPriceChange,
                    market?.oneMonthPriceChange,
                ).take(3).forEach { change ->
                    Text(
                        text = UiFormatter.formatSignedUsd(change),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = YesPillText
                    )
                }
            }
            upPrice?.let {
                LiveChanceDial(chance = it, label = upLabel)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutcomeButton(
                label = upLabel,
                background = YesPillBackground,
                contentColor = YesPillText,
                modifier = Modifier.weight(1f)
            )
            OutcomeButton(
                label = downLabel,
                background = NoPillBackground,
                contentColor = NoPillText,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(LiveRed)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "LIVE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = LiveRed
            )
            event.tags?.firstOrNull()?.label?.let {
                Text(
                    text = " · $it",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LiveChanceDial(chance: Double, label: String) {
    Box(
        modifier = Modifier.size(58.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { chance.toFloat().coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxSize(),
            color = YesPillText,
            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
            strokeWidth = 4.dp
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = UiFormatter.formatPriceAsPercentage(chance),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun OutcomeButton(
    label: String,
    background: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = contentColor,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(background)
            .padding(vertical = 12.dp)
    )
}

private fun BaseEventDto.isLiveCryptoUpDownEvent(): Boolean {
    val market = baseMarkets.firstOrNull() ?: return false
    val hasUpDownOutcomes = market.outcomes.any { it.equals("up", ignoreCase = true) } &&
        market.outcomes.any { it.equals("down", ignoreCase = true) }
    val slugLower = slug.lowercase()
    val titleLower = title.lowercase()
    return active &&
        !closed &&
        hasUpDownOutcomes &&
        (slugLower.contains("updown") || titleLower.contains("up or down"))
}

private fun UiFormatter.formatSignedUsd(value: Double): String {
    val sign = if (value >= 0.0) "+" else "-"
    return "$sign ${formatLargeValueUsd(kotlin.math.abs(value))}"
}

// --- MarketRow (universal row for outcome) --- //
// Displays a row for one outcome with its name and price.
@Composable
private fun MarketRow(
    title: String,
    price: Double?,
    marketResolutionStatus: MarketResolutionStatus?,
    resolvedOutcome: String? = null,
    leadingColor: Color? = null,
    isLeadingOutcome: Boolean = false,
    showOutcomePills: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isResolved = marketResolutionStatus == MarketResolutionStatus.RESOLVED
        val emphasizeOutcome = isLeadingOutcome && !isResolved

        OutcomeTextRow(
            modifier = Modifier.weight(1f),
            title = title,
            marketResolutionStatus = marketResolutionStatus,
            leadingColor = leadingColor,
            isEmphasized = emphasizeOutcome,
        )
        Spacer(modifier = Modifier.width(8.dp))

        val chanceText: String = if (isResolved && resolvedOutcome != null) {
            resolvedOutcome
        } else {
            UiFormatter.formatPriceAsPercentage(price)
        }

        // Chance
        Text(
            modifier = Modifier.priceAlpha(price, isResolved),
            text = chanceText,
            style = if (emphasizeOutcome) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
            fontWeight = if (emphasizeOutcome) FontWeight.ExtraBold else FontWeight.Bold,
            color = if (isResolved) MaterialTheme.colorScheme.onSurfaceVariant else leadingColor ?: MaterialTheme.colorScheme.primary,
            textDecoration = if (isResolved) TextDecoration.LineThrough else TextDecoration.None,
        )
        if (showOutcomePills && !isResolved) {
            Spacer(modifier = Modifier.width(8.dp))
            OutcomeDecisionPills()
        }
    }
}

@Composable
private fun OutcomeDecisionPills() {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        OutcomeDecisionPill(
            label = "Yes",
            background = YesPillBackground,
            contentColor = YesPillText
        )
        OutcomeDecisionPill(
            label = "No",
            background = NoPillBackground,
            contentColor = NoPillText
        )
    }
}

@Composable
private fun OutcomeDecisionPill(
    label: String,
    background: Color,
    contentColor: Color,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = contentColor,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 5.dp)
    )
}

internal fun Modifier.priceAlpha(price: Double?, isResolved: Boolean): Modifier {
    if (!isResolved && UiFormatter.isPriceLow1Percent(price)) return this.alpha(0.6f)
    return this
}

@Composable
private fun OutcomeTextRow(
    title: String,
    marketResolutionStatus: MarketResolutionStatus?,
    leadingColor: Color? = null,
    isEmphasized: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        leadingColor?.let {
            OutcomeColorDot(color = it)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            modifier = Modifier.weight(1f, fill = false),
            text = title, // Show passed outcome text
            style = if (isEmphasized) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isEmphasized) FontWeight.Bold else FontWeight.Normal,
            color = if (marketResolutionStatus == MarketResolutionStatus.RESOLVED) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            textDecoration = if (marketResolutionStatus == MarketResolutionStatus.RESOLVED) {
                TextDecoration.LineThrough
            } else {
                TextDecoration.None
            },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        marketResolutionStatus?.let {
            Spacer(modifier = Modifier.width(8.dp))

            val resolutionText = when (marketResolutionStatus) {
                MarketResolutionStatus.DISPUTED -> stringResource(R.string.status_disputed)
                MarketResolutionStatus.RESOLVED -> stringResource(R.string.status_resolved)
            }

            Text(
                text = resolutionText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun OutcomeColorDot(color: Color) {
    Box(
        modifier = Modifier
            .size(9.dp)
            .clip(RoundedCornerShape(99.dp))
            .background(color)
    )
}

@Composable
private fun TwoOutcomeDistributionBar(
    rows: List<MarketDisplayRow>,
    modifier: Modifier = Modifier
) {
    val first = rows.getOrNull(0)?.price ?: return
    val second = rows.getOrNull(1)?.price ?: return

    Spacer(modifier = Modifier.height(4.dp))
    DualDistributionBar(
        firstFraction = first,
        secondFraction = second,
        modifier = modifier.padding(horizontal = 16.dp)
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun DualDistributionBar(
    firstFraction: Double,
    secondFraction: Double,
    modifier: Modifier = Modifier
) {
    val first = firstFraction.coerceIn(0.0, 1.0)
    val second = secondFraction.coerceIn(0.0, 1.0)
    val total = first + second
    val firstWeight = (if (total > 0.0) first / total else 0.5).coerceAtLeast(0.001)
    val secondWeight = (if (total > 0.0) second / total else 0.5).coerceAtLeast(0.001)

    Box(
        modifier = modifier
            .height(12.dp)
            .clip(RoundedCornerShape(99.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
            Box(
                modifier = Modifier
                    .weight(firstWeight.toFloat())
                    .fillMaxHeight()
                    .background(OutcomeOrange)
            )
            Box(
                modifier = Modifier
                    .weight(secondWeight.toFloat())
                    .fillMaxHeight()
                    .background(OutcomeBlue)
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(1.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.42f))
        )
    }
}

private fun distributionColorFor(index: Int): Color {
    return if (index == 0) OutcomeOrange else OutcomeBlue
}

// --- Previews --- //
@Preview(showBackground = true, name = "Binary Event Preview")
@Composable
private fun BinaryEventListItemPreview() {
    PolymarketAppTheme {
        EventListItem(event = PreviewMocks.sampleBinaryEvent, onClick = {})
    }
}

@Preview(showBackground = true, name = "Multi Market Event Preview (Corrected)")
@Composable
private fun MultiMarketEventListItemPreview() {
    PolymarketAppTheme {
        EventListItem(event = PreviewMocks.sampleMultiMarketOptimizedEvent, onClick = {})
    }
}

// NEW Preview for categorical market
@Preview(showBackground = true, name = "Categorical Event Preview")
@Composable
private fun CategoricalEventListItemPreview() {
    PolymarketAppTheme {
        EventListItem(event = PreviewMocks.sampleCategoricalEvent, onClick = {})
    }
}
