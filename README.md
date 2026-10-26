# ApexTelemetry 🏍️💨
### High-Performance Motorcycle Lean & G-Force Telemetry HUD for Android (Optimized for Samsung Galaxy S20 FE)

ApexTelemetry turns your Android smartphone into an onboard MotoGP-style telemetry display. Designed specifically for handlebar-mounted riding displays with full **Auto-Rotation** support, sensor noise dampening, and direct sunlight visibility.

---

## 🌟 Key Features

1. **Live Lean Angle Arc Gauge:**
   - Real-time roll angle display (-60° to +60°) with dynamic color shifting (Cyan ➔ Orange ➔ Red).
   - Dynamic left and right peak-hold markers that persist through corners.
   - MotoGP-style digital degree readout with directional indicator (`◄ L` / `R ►`).

2. **2D G-Force Friction Circle (Radar):**
   - Live crosshair plotting lateral cornering Gs and longitudinal acceleration/braking Gs.
   - Dual-ring radar scale (0.5G & 1.0G references).
   - Peak session brake and lateral G metrics.

3. **GPS Speedometer:**
   - High-contrast digital speed readout in km/h.
   - Real-time top-speed capture.

4. **Hardware Sensor Coordinate Remapping & Auto-Rotation:**
   - Seamlessly switches between **Portrait** and **Landscape** modes without resetting peak session metrics.
   - Dynamically remaps Android sensor axes via `SensorManager.remapCoordinateSystem` so the gauge operates accurately in both vertical and horizontal handlebar clamps.

5. **One-Tap "Tare / Zero" Calibration:**
   - Mount the phone on your motorcycle at any angle, hold the bike upright, and tap **"CALIBRATE / TARE ZERO"** to lock in your neutral 0° baseline.

6. **Chassis & Engine Buzz Filtering:**
   - Exponential Moving Average (EMA) low-pass filtering eliminates single/twin-cylinder handlebar vibrations, ensuring needle stability.

7. **Samsung S20 FE Display & Battery Optimizations:**
   - **True Pure Black (`#000000`) AMOLED Theme:** Minimizes power consumption and prevents thermal buildup on long rides under the sun.
   - **120Hz Animation Smoothness:** Built with Compose Canvas drawing for high FPS and zero input lag.
   - **Screen Keep-Awake:** Keeps the HUD active throughout your ride.

---

## 📱 How to Use on Your Motorcycle

1. Clamp your Samsung S20 FE into your handlebar phone mount (in either **Portrait** or **Landscape**).
2. Launch **ApexTelemetry**.
3. Sit on the bike with the motorcycle held perfectly upright (off the kickstand).
4. Tap the **"CALIBRATE / TARE ZERO"** button. The status will change to **`CALIBRATED (0° SET)`**.
5. Ride! The app will track your lean angle, peak angles into each corner, braking G-force, and top speed.
6. Tap **RESET** at any time to clear the session peak markers.

---

## 🛠️ Project Structure

```
ApexTelemetry/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/mototags/apextelemetry/
│   │   │   ├── MainActivity.kt                      // Lifecycle, keep-awake, auto-rotation handling
│   │   │   ├── model/
│   │   │   │   └── TelemetryData.kt                 // Telemetry state model
│   │   │   ├── data/
│   │   │   │   ├── sensor/TelemetrySensorManager.kt // Fusion, axis remapping & EMA filtering
│   │   │   │   └── location/LocationService.kt      // Fused Location provider for GPS speed
│   │   │   └── ui/
│   │   │       ├── TelemetryViewModel.kt            // StateFlow telemetry manager
│   │   │       ├── HUDDashboardScreen.kt            // Responsive Portrait & Landscape HUD
│   │   │       ├── components/
│   │   │       │   ├── LeanGauge.kt                 // Canvas arc & peak indicators
│   │   │       │   ├── GForceMeter.kt               // 2D G friction radar
│   │   │       │   └── Speedometer.kt               // Digital GPS speed
│   │   │       └── theme/                           // High-contrast AMOLED palette
│   │   └── res/                                     // Vectors, mipmaps, strings
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml                           // Version catalog
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🚀 Building & Installing the APK

### In Android Studio:
1. Open Android Studio and select **Open** ➔ choose this folder (`PowerPlatform Functional resources`).
2. Connect your Samsung S20 FE via USB with **USB Debugging** enabled (in Developer Options).
3. Click **Run ▶** to install and launch directly on the device.

### Via Command Line (Gradle):
```bash
./gradlew assembleDebug
```
The output APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`
