# Ace Tuner V1 — Android companion app

Initial Android companion app for the existing KernelSU module on POCO F6 / Snapdragon 8s Gen 3.

- Embeds a WebView UI and bridges its commands to `/system/bin/su`.
- Includes a basic floating Game Bar overlay for CPU cluster frequency, battery temperature and available RAM.
- The existing KernelSU module with ID `cpufreq_max_cap` must remain installed; the app does not replace it.
- FPS/GPU telemetry is not implemented reliably yet. This is an initial scaffold and must be compiled and device-tested before use.

Build: run `gradle assembleDebug` from this directory with JDK 17, Android SDK platform 35 and build-tools 35.0.0.
