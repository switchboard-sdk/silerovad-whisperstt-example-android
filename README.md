# Switchboard SDK Whisper STT Example

This project showcases how to use Switchboard SDK and its Whisper STT extension to create a speech to text application.

# Setup

## Install Android Studio

Android Studio 2025.2.2 was used to create the project but it is not a requirement for you to use the same version.

## Download Whisper Models

Switchboard Whisper extension requies Whisper models to operate. The application expects the models to be present the in `assets` folder (`app/src/main/assets`). You can download them via following links:

- [ggml-tiny.en.bin](https://switchboard-sdk-public.s3.amazonaws.com/assets/models/whisper/ggml-tiny.en.bin)
- [ggml-base.en.bin](https://switchboard-sdk-public.s3.amazonaws.com/assets/models/whisper/ggml-base.en.bin)

Following scripts handle both downloading and copying of the files to correct directory, you can run in your command line interface.

### macOS / Linux

`bash scripts/setup.sh`

### Windows

`script\setup.bat`

# How to build

- Open the project in Android Studio
- Select your target device/simulator
- Run

# How to use

- Select the Whisper Model you want to use
- Press Start
- Speak into the microphone of your device
- You can configure voice activity detector's parameters as needed
