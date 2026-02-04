# Switchboard SDK Whisper STT Example

This project showcases how to use Switchboard SDK and its Whisper STT extension to create a speech to text application.

# Setup

## Prerequisites

### Android Studio
Android Studio version 2025.2.2 was used to create the project but it is not a requirement to use the same version.

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

# How to use

- Select the Whisper model you want to use (Tiny for speed, Base for accuracy)
- Press Start
- Speak into the microphone of your device
- Transcriptions will appear in real-time
- Adjust voice activity detector parameters as needed (threshold and silence duration)
