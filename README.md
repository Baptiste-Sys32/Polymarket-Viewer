# Polymarket Viewer

Unofficial Android app for browsing Polymarket prediction markets, tracking watchlisted events, and checking price charts, with home screen widgets.

This repository is a personal fork of [streamatico/PolymarketViewer](https://github.com/streamatico/PolymarketViewer). Notable changes: filters and layout brought closer to the website look, plus a forced dark mode for OLED and AMOLED screens.

## Screenshots

<div align="center">
  <img src="metadata/en-US/images/phoneScreenshots/1.png" width="160" alt="Main Events List"/>
  <img src="metadata/en-US/images/phoneScreenshots/2.png" width="160" alt="Event Details"/>
  <img src="metadata/en-US/images/phoneScreenshots/3.png" width="160" alt="Market Details"/>
  <img src="metadata/en-US/images/phoneScreenshots/4.png" width="160" alt="Price Charts"/>
  <img src="metadata/en-US/images/phoneScreenshots/5.png" width="160" alt="Widgets"/>
</div>

## Key Technologies

* **Language** - Kotlin
* **UI & Theme** - Jetpack Compose with Material 3
* **Widgets** - AndroidX Glance App Widgets
* **Charts** - Vico Charts
* **Networking & DI** - Ktor Client & Koin

## Acknowledgments

This project is built upon the open-source architecture of the upstream repository:
https://github.com/streamatico/PolymarketViewer

## Build

```bash
./gradlew assembleDebug
```

Needs the Android SDK. `local.properties` is git-ignored, point `sdk.dir` at your install.

## License

This project is licensed under the GNU General Public License v3.0 (GPL-3.0). See the [LICENSE.txt](LICENSE.txt) file for details.

## Disclaimer

This is an unofficial, experimental application. It is not affiliated with, endorsed by, or associated with Polymarket. All data is sourced via public API endpoints.
