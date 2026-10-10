# Ace Tuner V1

KernelSU module for the POCO F6 (Snapdragon 8s Gen 3 / peridot).

## Features
- CPU governor and frequency-cap controls
- CPU/GPU tuning options included in the WebUI
- Optional battery-side charging-current target
- Thermal-profile and related tuning controls included in the module
- WebUI-based configuration

## Install
1. Download `Ace_Tuner_V1.zip` from this repository.
2. Install it through KernelSU Manager.
3. Reboot if required, then open the module WebUI.

## Notes
- Device/runtime behavior has not been independently validated by this repository upload.
- Sysfs nodes and governor tunables vary by kernel and ROM.
- The module keeps the existing ID `cpufreq_max_cap` for upgrade compatibility with the previous Apex Tuner package.
