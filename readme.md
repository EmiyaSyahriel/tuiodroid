## TUIOdroid 
is an open source TUIO tracker for Android devices, which allows multi-touch remote 
control based on the TUIO protocol. 

It is capable of sending standard TUIO/UDP messages through any active 3G or WIFI connection. This application is available free of charge on the Android Market and can be used in conjunction with any TUIO enabled client application. Its source code is also available under the terms of the GPL and therefore can be freely used for the creation of open source TUIO enabled mobile applications. Apart from that TUIOdroid is also a useful tool for the development and testing of TUIO 1.1 client implementations. You can find more information about the TUIO protocol and framework at http://www.tuio.org/

This is a fork of the original project [here](https://github.com/TobiasSchwirten/tuiodroid).

### What this fork does?
- Migrate the project to Gradle to make it works on Android Studio
- Modernizes the UI to fit on newer Android version
- Adds way to select how should users opens the settings:
    - Shake the phone (Original)
    > It annoys me a lot since my phone rocks a lot when using the app and it keeps opening the settings
    - Volume Up / Down (or Both)
    - Back Button, You always can swipe the app from recent to close the app or use the exit option on settings
- Make the app translatable

### Links
- [Original Project](https://github.com/TobiasSchwirten/tuiodroid)
- [Official Market Link (now dead)](https://market.android.com/details?id=tuioDroid.impl)

### Original Creator
This application has been created by [Tobias Schwirten](https://github.com/TobiasSchwirten) and [Martin Kaltenbrunner](https://github.com/mkalten). The included Java TUIO implementation is based on the JavaOSC library by Chandrasekhar Ramakrishnan. Please note that the GPL demands the publication of the full source code of any derived work. If you are planning to develop a proprietary application based on this code, we may be able to provide an alternative commercial license option.

### License
GNU GPL-3.0 , see [License](license.txt)