# Multi watch media and sensor logger
This app is in developing phase which will record audio, sensor data (mainly type_vector_rotation, type_accelerometer, type_gyroscope) and will store to database ( in wear, at host mobile, in a remote server or even streaming using socket service). This app uses the concept and base code from [WCL](https://github.com/googlesamples/android-WclDemoSample)
## Dependencies
* [WearCompanionLibrary (WCL)](https://github.com/googlesamples/android-WearCompanionLibrary)
* The phone module also depends on the [design support library](http://android-developers.blogspot.com/2015/05/android-design-support-library.html).
* Note: if you follow the instructions below, the WCL library will bring in other dependencies that
  are needed. If you choose to use WCL in a different way (for example using an archive version of
  that library), then you may need to include the dependencies that are listed for WCL as well.

## Setup
* Checkout this project as MediaLoggerWearApp:
```
$ git clone https://github.com/mahbubcsedu/Multi-watch-sensor-logger.git
```
* To make sure all is working, compile WCL:
```
$ cd WearCompanionLibrary && ./gradlew build
```
* Compile this project:
```
$ cd ../MediaLoggerWearApp && ./gradlew build
```
* To open the project in Android Studio, you may need to first open the WCL project in Android Studio
  to build the required "*.iml" file for Android Studio. To do this, start Android Studio and select "Open An Existing
  Android Studio Project" and navigate to WearCompanionLibrary directory and select the build.gradle in the root of
  that project. This should create the needed files.
* Close Android Studio and open that again and follow the same steps but this time navigate to
  MediaLoggerWearApp project and select build.gradle there.
