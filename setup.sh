#!/bin/bash

ASSETS_DIR="app/src/main/assets"

mkdir -p "$ASSETS_DIR"

echo "Downloading ggml-base.en.bin..."
curl -L -o "$ASSETS_DIR/ggml-base.en.bin" https://switchboard-sdk-public.s3.amazonaws.com/assets/models/whisper/ggml-base.en.bin

echo "Downloading ggml-tiny.en.bin..."
curl -L -o "$ASSETS_DIR/ggml-tiny.en.bin" https://switchboard-sdk-public.s3.amazonaws.com/assets/models/whisper/ggml-tiny.en.bin

echo "Whisper Models downloaded"