\# ParkPin (Wayback)



Campus parking spot tracker for CSE226 (Advanced Android Development), problem statement P07.

On Google Play the app is called \*\*Wayback\*\*.



\## Overview

Save where you parked with one tap and navigate back to it later with Google Maps.

An optional photo of the spot is labeled on the device with Google ML Kit, so the surroundings help you recognise the place.

Spots are stored on the phone, and saving works offline.



\## Features

\- One-tap save of the current GPS location, with a "Low accuracy" warning when the fix is poor

\- Navigate back through Google Maps (walking directions)

\- History of saved spots with delete

\- Rename a spot

\- Photo at the spot with on-device AI image labeling (Google ML Kit)



\## Tech stack

\- Kotlin, Jetpack Compose (Material 3)

\- MVVM: ViewModel, StateFlow, coroutines

\- Room (local database)

\- Fused Location Provider (Google Play services)

\- Google ML Kit Image Labeling (on-device)

\- Google Maps through an intent (no Maps SDK)



\## How it works

1\. Tap \*\*Save spot\*\*. The app asks for a GPS fix (waits up to 10 seconds, then falls back to the last known location and marks it low accuracy).

2\. The spot is written to Room and the screen updates from the database.

3\. \*\*Add photo\*\* opens the camera app. ML Kit labels the picture and the tags are stored with the spot.

4\. \*\*Navigate\*\* opens Google Maps with walking directions.



\## Privacy

Spots, photos and labels stay on the device. See the \[privacy policy](PRIVACY.md).



\## Build

Open in Android Studio, sync Gradle, and run on a device with location services.



\## Status

Closed testing on Google Play.



\## License

MIT

