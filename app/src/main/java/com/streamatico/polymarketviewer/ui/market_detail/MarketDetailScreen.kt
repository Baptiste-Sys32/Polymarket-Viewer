package com.streamatico.polymarketviewer.ui.market_detail

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.streamatico.polymarketviewer.R
import com.streamatico.polymarketviewer.data.model.gamma_api.MarketDto
import com.streamatico.polymarketviewer.data.model.gamma_api.getTitleOrDefault
import com.streamatico.polymarketviewer.ui.shared.UiError
import com.streamatico.polymarketviewer.ui.shared.UiFormatter
import com.streamatico.polymarketviewer.ui.shared.components.ErrorBox
import com.streamatico.polymarketviewer.ui.shared.components.LoadingBox
import com.streamatico.polymarketviewer.ui.shared.components.MyScaffold
import com.streamatico.polymarketviewer.ui.tooling.PreviewMocks
import java.time.OffsetDateTime

// Market details screen
@Composable
fun MarketDetailScreen(
    viewModel: MarketDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    MarketDetailContent(
        uiState = uiState,
        isRefreshing = isRefreshing,
        onRefresh = viewModel::refreshMarketDetails,
        onNavigateBack = onNavigateBack,
        onRetry = { viewModel.retryLoad() }
    )
}

// Market details presentation (without ViewModel)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarketDetailContent(
    uiState: MarketDetailUiState,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit
) {
    MyScaffold(
        topBar = {
            TopAppBar(
                title = {
                    val titleText = when (uiState) {
                        is MarketDetailUiState.Success -> uiState.market.getTitleOrDefault(stringResource(R.string.market_details_title))
                        is MarketDetailUiState.Loading -> stringResource(R.string.market_details_loading_title)
                        is MarketDetailUiState.Error -> stringResource(R.string.market_details_title)
                    }
                    Text(titleText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is MarketDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingBox()
                }
            }
            is MarketDetailUiState.Success -> {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    MarketDetailsContent(
                        market = uiState.market,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            is MarketDetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorBox(
                        error = uiState.error,
                        onRetry = onRetry,
                    )
                }
            }
        }
    }
}

// Main content on successful load
@Composable
fun MarketDetailsContent(market: MarketDto, modifier: Modifier = Modifier) {
    var isDescriptionExpanded by remember { mutableStateOf(false) }

    val outcomes = market.outcomes
    val prices = market.outcomePrices
    val outcomePricePairs = remember(outcomes, prices) {
        outcomes.zip(prices) { outcome, price -> outcome to price }
            //.sortedByDescending { it.second }
    }

    LazyColumn(modifier = modifier, contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
        // --- Display Market Image --- //
        if(!market.imageUrl.isNullOrBlank()) {
            item {
                AsyncImage(
                    model = market.imageUrl,
                    contentDescription = "Market image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(16.dp)) // Space between image and title
            }
        }

        // --- Display Title --- //
        item {
            Text(text = market.question, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- Market description (expandable) --- //
        item {
            market.description?.takeIf { it.isNotBlank() }?.let {
                Column {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 5,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.animateContentSize()
                    )
                    if (it.lines().size > 5) {
                        val showMoreText = if (isDescriptionExpanded) "Show less" else "Show more"
                        Text(
                            text = showMoreText,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable { isDescriptionExpanded = !isDescriptionExpanded }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // --- Additional market information (in card) --- //
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.34f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    market.volume?.let {
                        InfoRow(label = "Volume", value = UiFormatter.formatLargeValueUsd(it))
                        Spacer(Modifier.height(8.dp))
                    }
                    market.liquidity?.let {
                        InfoRow(label = "Liquidity", value = UiFormatter.formatLargeValueUsd(it))
                        Spacer(Modifier.height(8.dp))
                    }
                    market.startDate?.let {
                        InfoRow(label = "Starts", value = UiFormatter.formatDateTimeLong(it))
                        Spacer(Modifier.height(8.dp))
                    }
                    market.endDate?.let {
                        InfoRow(label = "Ends (estimated)", value = UiFormatter.formatDateTimeLong(it))
                        Spacer(Modifier.height(8.dp))
                    }
                    market.umaResolutionStatus?.let {
                        InfoRow(label = "Resolution Status", value = it)
                    }
                    market.resolutionSource?.takeIf { it.isNotBlank() }?.let {
                        // Remove Spacer after the last element
                        InfoRow(label = "Resolution Source", value = it)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // Spacing after card
            // HorizontalDivider() // Remove divider here
        }

        // --- Outcomes list --- //
        if (outcomePricePairs.isNotEmpty()) {
            val leadingOutcomeIndex = if (outcomePricePairs.size == 2) {
                outcomePricePairs.indices.maxByOrNull { outcomePricePairs[it].second }
            } else {
                null
            }

            item { // Header for outcomes
                Text("Outcomes:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.34f))
            }
            itemsIndexed(outcomePricePairs) { index, (outcome, price) ->
                MarketOutcomeRow(
                    outcomeText = outcome,
                    price = price,
                    isLeadingOutcome = index == leadingOutcomeIndex,
                    //marketVolume = market.volume
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f))
            }
        }
    }
}

// -- Helper Composables and functions -- //

// Row for displaying information (Label: Value)
@Composable
private fun InfoRow(label: String, value: String) {
    Row {
        Text("$label: ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

// Row for displaying market outcome
@Composable
private fun MarketOutcomeRow(
    outcomeText: String,
    price: Double?,
    isLeadingOutcome: Boolean,
    //marketVolume: Double?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = outcomeText,
                    style = if (isLeadingOutcome) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isLeadingOutcome) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = UiFormatter.formatPriceAsPercentage(price),
                style = if (isLeadingOutcome) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                fontWeight = if (isLeadingOutcome) FontWeight.ExtraBold else FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        price?.let {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { it.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isLeadingOutcome) 10.dp else 8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

// --- Previews --- //

@Preview(showBackground = true, name = "Success - Short Description")
@Composable
private fun MarketDetailViewPreviewSuccessShortDesc() {
    val shortDescMarket = PreviewMocks.sampleMarket1.copy(
        id = "2",
        question = "Market with short description?",
        slug = "market-with-short-description",
        description = "Short and sweet.",
        startDate = OffsetDateTime.now(),
        endDate = null,
        resolutionSource = "Community",
        volume = 5000.0,
        liquidity = 1000.0,
        outcomePricesJson = """["0.60", "0.40"]"""
    )
    MaterialTheme { // Wrap preview in MaterialTheme
        MarketDetailContent(
            uiState = MarketDetailUiState.Success(shortDescMarket),
            isRefreshing = false,
            onRefresh = {},
            onNavigateBack = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "Success - No Volume/Liquidity")
@Composable
private fun MarketDetailViewPreviewSuccessNoMetrics() {
    val noMetricsMarket = PreviewMocks.sampleMarket1.copy(
        question = "Market without volume/liquidity?",
        description = "Description here.",
        volume = null, // No volume
        liquidity = null, // No liquidity
        outcomesJson = """["Outcome A", "Outcome B"]""", // For scalar, might be different
        outcomePricesJson = """["0.5", "0.5"]""" // Placeholder prices
    )
    MaterialTheme { // Wrap preview in MaterialTheme
        MarketDetailContent(
            uiState = MarketDetailUiState.Success(noMetricsMarket),
            isRefreshing = false,
            onRefresh = {},
            onNavigateBack = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
private fun MarketDetailViewPreviewLoading() {
    MaterialTheme { // Wrap preview in MaterialTheme
        MarketDetailContent(
            uiState = MarketDetailUiState.Loading,
            isRefreshing = false,
            onRefresh = {},
            onNavigateBack = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "Success State")
@Composable
private fun MarketDetailViewPreviewSuccess() { // Removed PreviewParameter
    MaterialTheme { // Wrap preview in MaterialTheme
        MarketDetailContent(
            uiState = MarketDetailUiState.Success(PreviewMocks.sampleMarket1), // Use the sample market directly
            isRefreshing = false,
            onRefresh = {},
            onNavigateBack = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
private fun MarketDetailViewPreviewError() {
    MaterialTheme { // Wrap preview in MaterialTheme
        MarketDetailContent(
            uiState = MarketDetailUiState.Error(
                UiError(title = stringResource(R.string.market_details_failed_to_load_preview))
            ),
            isRefreshing = false,
            onRefresh = {},
            onNavigateBack = {},
            onRetry = {}
        )
    }
}
