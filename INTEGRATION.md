# Integrating the Android SDK

## Adding the Switchboard SDK to Your Project

Add Synervoz maven repository URL to your repositories in Gradle

```
repositories {
    google()
    mavenCentral()
    maven { url "https://s3.amazonaws.com/synervoz-android-maven-repository" }
}
```

Then you can add the following dependencies:

```
dependencies {
    implementation 'com.synervoz:switchboardsdk:3.2.0'
    implementation 'com.synervoz:switchboard.extensions.whisper:3.2.0'
    implementation 'com.synervoz:switchboard.extensions.onnx:3.2.0'
    implementation 'com.synervoz:switchboard.extensions.silerovad:3.2.0'
}
```
## Dependencies Configuration

You will need the following additions in your Android manifest and app's gradle files.

### Android Manifest (`AndroidManifest.xml`)

Add the following manifest attribute to `application` tag of your `AndroidManifest.xml`

```
    android:extractNativeLibs="true"
```

## Application Gradle (`build.gradle`)

Add the following to your app's gradle file's `android` block

```
    packagingOptions {
        jniLibs {
            useLegacyPackaging true
        }
    }
```


## Download Whisper Models

Download Whisper models from the following links and place them in `assets` folder of your Android application i.e `app/src/main/assets`.

- [ggml-tiny.en.bin](https://switchboard-sdk-public.s3.amazonaws.com/assets/models/whisper/ggml-tiny.en.bin)
- [ggml-base.en.bin](https://switchboard-sdk-public.s3.amazonaws.com/assets/models/whisper/ggml-base.en.bin)

## Permissions

Add the following permissions to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.INTERNET" />
```

Request runtime permission to process audio input:

```kotlin
if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
    != PackageManager.PERMISSION_GRANTED) {
    ActivityCompat.requestPermissions(this,
        arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_PERMISSION_CODE)
}
```

## Load Extensions

Setup the Switchboard extensions.`loadExtensionLibrary` load the library of the extension, `load` registers it with the Switchboard SDK.

```
WhisperExtension.load()
SileroVADExtension.load()
```

## Initialize Switchboard SDK

```
Switchboard.initialize(
    appId = appId,
    appSecret = appSecret,
    extensions = mapOf(
        "Whisper" to emptyMap<String, Any>(),
        "SileroVAD" to emptyMap<String, Any>()
    )
)
```

## Copy Whisper STT Model Files

Switchboard Whisper extension needs a path of the STT model to load it. Since Android does not expose paths of assets, we will need to copy them to application's internal directory first

```
fun copyAssetFileToInternal(context: Context, assetPath: String, targetFileName: String): File {
    val outFile = File(context.filesDir, targetFileName)
    try {
        context.assets.open(assetPath).use { input ->
            outFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    } catch (e: IOException) {
        Log.e("AssetUtils", "Failed to copy asset file: $assetPath", e)
    }
    return outFile
}

AssetUtils.copyAssetFileToInternal(context, "ggml-tiny.en.bin", "ggml-tiny.en.bin")
```

## Create Engine

```
val configJson = context.assets.open("STTPlayerExample.json").readBytes().decodeToString()
val result = Switchboard.createEngine(configJson)
val engineId = result.value
```

`STTPlayerExample.json` contains the audio graph defining the audio pipeline.

## Add Transcription listener

Whisper STT Node emits `transcribed` event on transcribing speech to text, we need to add a listener to receive the event and associated data.

```
val transcriptionListenerResult = Switchboard.addEventListener(
            objectId = "sttNode",
            eventName = "transcribed"
        ) { _, eventData ->
            val data = eventData as? Map<String, Any> ?: return
            val transcription = data["text"] as? String ?: ""
            Log.d("TRANSCRIPTION", "$transcription")
        }
```

## Load Whisper Model

We need to call `loadModel` action on Whisper STT Node with path of the model file.

```
val modelPath = "${context.filesDir}/ggml-tiny.en.bin"
Switchboard.callAction(
     objectId = "sttNode",
     actionName = "loadModel",
     params = mapOf(
         "modelPath" to modelPath,
         "useGPU" to true
    )
)
```

## Start the Audio Engine

Now that everything is setup, we can start the audio processing and transcription by calling `start` action with the `engineId` we received from `createEngine` call's result.

```
Switchboard.callAction(engineId, "start")
```

Now you should see transcriptions of any speech detected by the device's microphone.

## Stop and Clean Up

When done, stop the engine and remove listeners:

```kotlin
Switchboard.callAction(engineId, "stop")
Switchboard.removeEventListener("sttNode", transcriptionListenerId)
```
