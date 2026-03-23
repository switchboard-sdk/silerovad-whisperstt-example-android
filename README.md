# Switchboard SDK Whisper STT and Silero VAD Example

This project showcases how to use Switchboard SDK and its Whisper STT and Silero VAD extensions to create a speech to text application.

# Setup

## Prerequisites

### Android Studio

- Android Studio version 2025.2.2 was used to create the project but it is not a requirement to use the same version.
- JDK 17.0.9 was used for development

### Android Device

64-bit Android Device with minimum OS 7.0 (API 24)

## Download Whisper Models

Switchboard Whisper extension requires Whisper models to operate. The application expects the models to be present in the `assets` folder (`app/src/main/assets`). You can download them via following links:

- [ggml-tiny.en.bin](https://switchboard-sdk-public.s3.amazonaws.com/assets/models/whisper/ggml-tiny.en.bin)
- [ggml-base.en.bin](https://switchboard-sdk-public.s3.amazonaws.com/assets/models/whisper/ggml-base.en.bin)

The following scripts handle downloading the files to the correct directory:

### macOS / Linux

```
bash scripts/setup.sh
```

### Windows

```
script\setup.bat
```

# How to build

- Open the project in Android Studio
- Select your target device/simulator
- Run

# How to use application

- Select the Whisper model you want to use (Tiny for speed, Base for accuracy)
- Press Start
- Speak into the microphone of your device
- Transcriptions will appear in real-time
- Adjust voice activity detector parameters as needed (threshold and silence duration)

## Diagrams
- [Whisper STT App Architecture](./images/architecture.png)
- [Sequence Diagram](./images/sequence.png)

# Running Tests
- Checkout `benchmark-tests` branch
- Open the project in Android Studio
- Selected `Release` build variant
- Gradle Sync
- Run `WhisperSTTBenchmarkTest` configuration

## View Test Results

Test results will be logged in the last test `zz_printRawResults` with tag `BENCHMARK_RESULTS`