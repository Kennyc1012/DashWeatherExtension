# DashWeatherExtension
Dashclock Weather extension

![screenshot](https://github.com/Kennyc1012/DashWeatherExtension/raw/master/art/screen_shot.png)


## Features 
- Shows current temperature and weather conditions
- Automatic location detection
- Imperial and metric units
- Displays high/low temperature (can also be inverted), UV index, humidity percentage, and current location, all of which can be turned on or off
- Can set a limit to how often it can update
- API 21+

## Building
In order to build DashWeatherExtension, you must use [Android Studio](https://developer.android.com/studio/index.html) and have [Kotlin](https://kotlinlang.org/docs/tutorials/kotlin-android.html) plugin configured for it. 

Once imported into AndroidStudio, you will need to acquire an API key from [Dark Sky](https://darksky.net/dev). These keys are free, but your requests are only free for the first 1,000 a day. If you make more than 1,000 API request in a day, you will be charged.

When you have an API key, either create or edit the `gradle.properties` file in the projects root directory and add the config `API_KEY = "YOUR API KEY"` where "YOUR API KEY" is the key you obtained by [Dark Sky](https://darksky.net/dev).


![screenshot](https://github.com/Kennyc1012/DashWeatherExtension/raw/master/art/poweredby-oneline.png)
