@echo off

set ASSETS_DIR=app\src\main\assets\models

if not exist "%ASSETS_DIR%" mkdir "%ASSETS_DIR%"

echo Downloading ggml-base.en.bin...
curl -L -o "%ASSETS_DIR%\ggml-base.en.bin" https://switchboard-sdk-public.s3.amazonaws.com/assets/models/whisper/ggml-base.en.bin

echo Downloading ggml-tiny.en.bin...
curl -L -o "%ASSETS_DIR%\ggml-tiny.en.bin" https://switchboard-sdk-public.s3.amazonaws.com/assets/models/whisper/ggml-tiny.en.bin

echo Models downloaded successfully!
pause