# Образ Android-емулятора (API 34, x86_64) для скриншотів без Android Studio.
FROM ghcr.io/cirruslabs/android-sdk:34
RUN apt-get update && apt-get install -y --no-install-recommends \
      libpulse0 libnss3 libxcomposite1 libxcursor1 libxi6 libxtst6 libgl1 libasound2t64 libxkbfile1 libdbus-1-3 \
    && rm -rf /var/lib/apt/lists/*
RUN yes | sdkmanager --licenses >/dev/null \
    && sdkmanager "emulator" "system-images;android-34;google_apis;x86_64"
RUN echo no | avdmanager create avd -n lab -k "system-images;android-34;google_apis;x86_64" -d pixel_6
