[![](https://jitpack.io/v/hospes/EasyImage.svg)](https://jitpack.io/#hospes/EasyImage)

# THIS IS FORK
## Reason
So this is the fork as mentioned in the title adn the reason is to upgrade current EasyImage ib to support latest (mostly alpha) dependencies and features that provides `androidx`.
In our case i'm talking about `ActivityResultContract` feature that provides safe interface for sharing data between activitie and deprecate default `onActivityResut` way.

# Original readme can be found here: [readme.md](https://github.com/jkwiecien/EasyImage/blob/master/README.md)

# What is it?
EasyImage allows you to easily capture images and videos from the gallery, camera or documents without creating lots of boilerplate.

# Setup

## Runtime permissions
This library requires specific runtime permissions. Declare it in your `AndroidMnifest.xml`:
```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.CAMERA" />
```

**Please note**: for devices running API 23 (marshmallow) you have to request this permissions in the runtime, before calling `EasyContracts.CameraForImage/EasyContracts.CameraForVideo.launch()`. It's demonstrated in the sample app.

**There is also one issue about runtime permissions**. According to the docs: 

    If your app targets M and above and declares as using the CAMERA permission which is not granted, then attempting to use this action will result in a SecurityException.

For this reason, if your app uses `CAMERA` permission, you should check it along **with** `WRITE_EXTERNAL_STORAGE` before calling `EasyContracts.CameraForImage/EasyContracts.CameraForVideo.launch()`

## Gradle dependency
Get the latest version from jitpack

[![](https://jitpack.io/v/hospes/EasyImage.svg)](https://jitpack.io/#hospes/EasyImage)

# Usage
## Essentials

Register your EasyContract result receiver instance like this:
```kotlin
private val gallery = registerForActivityResult(EasyContracts.Gallery(this)) { result -> 
    when (result) {
        /* Handle success result */
        is EasyContracts.Result.Success -> onPhotosReturned(result.files.toTypedArray())
        
        /* Handle error result */
        is EasyContracts.Result.Error -> result.throwable.printStackTrace() 
        
        /* Handle canceled result */
        is EasyContracts.Result.Canceled -> Unit 
    }
}
```

Launch intent like this:
```kotlin
gallery.launch(true) /* true/false - means allowing multi selecting of images */
```

### EasyContracts class contain number of ready to go contracts
- `EasyContracts.Chooser`
- `EasyContracts.Documents`
- `EasyContracts.Gallery`
- `EasyContracts.CameraForImage`
- `EasyContracts.CameraForVideo`

# License

    Copyright 2020 Andrew Khloponin.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.