package com.mototags.apextelemetry.data.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.mototags.apextelemetry.model.RideSession
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RideExporter(private val context: Context) {

    private val exportDir: File
        get() {
            val dir = File(context.cacheDir, "exports")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    fun exportToGpx(ride: RideSession): File {
        val sanitizedTitle = ride.title.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        val file = File(exportDir, "Apex_${sanitizedTitle}_${ride.startTime}.gpx")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)

        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<gpx version=\"1.1\" creator=\"ApexTelemetry Pro\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n")
        sb.append("  <metadata>\n")
        sb.append("    <name>").append(ride.title).append("</name>\n")
        sb.append("    <time>").append(dateFormat.format(Date(ride.startTime))).append("</time>\n")
        sb.append("  </metadata>\n")
        sb.append("  <trk>\n")
        sb.append("    <name>").append(ride.title).append("</name>\n")
        sb.append("    <desc>Max Left: ").append(ride.maxLeftLean.toInt()).append(" deg | Max Right: ")
            .append(ride.maxRightLean.toInt()).append(" deg | Top Speed: ").append(ride.maxSpeedKmh.toInt()).append(" km/h</desc>\n")
        sb.append("    <trkseg>\n")

        for (pt in ride.points) {
            if (pt.latitude != 0.0 && pt.longitude != 0.0) {
                sb.append("      <trkpt lat=\"").append(pt.latitude).append("\" lon=\"").append(pt.longitude).append("\">\n")
                sb.append("        <ele>").append(pt.altitude).append("</ele>\n")
                sb.append("        <time>").append(dateFormat.format(Date(pt.timestamp))).append("</time>\n")
                sb.append("        <speed>").append(String.format(Locale.US, "%.2f", pt.speedKmh / 3.6f)).append("</speed>\n")
                sb.append("      </trkpt>\n")
            }
        }

        sb.append("    </trkseg>\n")
        sb.append("  </trk>\n")
        sb.append("</gpx>\n")

        file.writeText(sb.toString())
        return file
    }

    fun exportToCsv(ride: RideSession): File {
        val sanitizedTitle = ride.title.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        val file = File(exportDir, "Apex_${sanitizedTitle}_${ride.startTime}.csv")

        val sb = StringBuilder()
        sb.append("Timestamp,Time_UTC,Latitude,Longitude,Altitude_m,Speed_kmh,Lean_Angle_deg,Lateral_G,Longitudinal_G\n")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

        for (pt in ride.points) {
            sb.append(pt.timestamp).append(",")
            sb.append(dateFormat.format(Date(pt.timestamp))).append(",")
            sb.append(String.format(Locale.US, "%.6f", pt.latitude)).append(",")
            sb.append(String.format(Locale.US, "%.6f", pt.longitude)).append(",")
            sb.append(String.format(Locale.US, "%.1f", pt.altitude)).append(",")
            sb.append(String.format(Locale.US, "%.1f", pt.speedKmh)).append(",")
            sb.append(String.format(Locale.US, "%.1f", pt.leanAngle)).append(",")
            sb.append(String.format(Locale.US, "%.3f", pt.lateralG)).append(",")
            sb.append(String.format(Locale.US, "%.3f", pt.longitudinalG)).append("\n")
        }

        file.writeText(sb.toString())
        return file
    }

    fun shareFile(file: File, mimeType: String = "application/octet-stream") {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, "Export Telemetry: ${file.name}").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (_: Exception) {
            // Handled gracefully
        }
    }
}
