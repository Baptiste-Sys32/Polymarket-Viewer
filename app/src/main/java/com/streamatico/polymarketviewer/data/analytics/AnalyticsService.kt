package com.streamatico.polymarketviewer.data.analytics

import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope

// Analytics is intentionally disabled in this private fork.
class AnalyticsService(
    @Suppress("UNUSED_PARAMETER") private val httpClient: HttpClient,
    @Suppress("UNUSED_PARAMETER") private val userPreferencesRepository: com.streamatico.polymarketviewer.data.preferences.UserPreferencesRepository,
    @Suppress("UNUSED_PARAMETER") private val appScope: CoroutineScope
) {
    fun track(event: AnalyticsEvent) {
        // No-op.
    }
}
