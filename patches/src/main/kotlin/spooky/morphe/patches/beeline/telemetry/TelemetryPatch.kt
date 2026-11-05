package spooky.morphe.patches.beeline.telemetry

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

import spooky.morphe.patches.shared.Constants.COMPATIBILITY_BEELINE

/**
 * Disable Beeline telemetry.
 *
 * Stops the app from sending usage and crash telemetry to remote servers.
 * No-ops the dispatch points for the app's analytics and crash-reporting SDKs
 * so no usage data is transmitted, while leaving all core functionality
 * (navigation, routing, map rendering, subscriptions, etc.) untouched.
 *
 * Targets:
 *   • Segment Analytics — `wc.c(Event)` — central Segment event dispatch
 *   • Firebase Analytics — `FirebaseAnalytics.logEvent(String, Bundle)`
 *   • Firebase Crashlytics — `FirebaseCrashlytics.log(String)`
 *   • Firebase Crashlytics — `FirebaseCrashlytics.recordException(Throwable)`
 *   • Mapbox Telemetry — `MapTelemetry.setUserTelemetryRequestState(boolean)`
 *
 * This patch is safe to apply with the other Beeline patches (Unlock Plus,
 * Firebase cert fix, signature spoof, install source spoof). It does not
 * interact with any of them.
 */
@Suppress("unused")
val disableBeelineTelemetryPatch = bytecodePatch(
    name = "Disable Beeline telemetry",
    description = """
        Stops Beeline from sending usage and crash telemetry to remote servers.

        Disables:
        - Segment Analytics (track / screen / identify events)
        - Firebase Analytics (logEvent)
        - Firebase Crashlytics (log messages, recorded exceptions)
        - Mapbox map telemetry (user telemetry request state)

        Does not affect app functionality — only telemetry collection is
        suppressed. Navigation, routing, maps and Plus features all keep working.
    """.trimIndent(),
    default = true,
) {
    compatibleWith(COMPATIBILITY_BEELINE)

    execute {
        // ─── Segment Analytics ─────────────────────────────────────────────
        // wc.c(Event) is the central dispatch for all Segment events.
        SegmentAnalyticsDispatchFingerprint.methodOrNull
            ?.addInstructions(0, "return-void")
            ?: println("Telemetry: Segment Analytics dispatch (wc.c) not found — skipping")

        // ─── Firebase Analytics ────────────────────────────────────────────
        FirebaseAnalyticsLogEventFingerprint.methodOrNull
            ?.addInstructions(0, "return-void")
            ?: println("Telemetry: Firebase Analytics logEvent not found — skipping")

        // ─── Firebase Crashlytics ──────────────────────────────────────────
        FirebaseCrashlyticsLogFingerprint.methodOrNull
            ?.addInstructions(0, "return-void")
            ?: println("Telemetry: Firebase Crashlytics log(String) not found — skipping")

        FirebaseCrashlyticsRecordExceptionFingerprint.methodOrNull
            ?.addInstructions(0, "return-void")
            ?: println("Telemetry: Firebase Crashlytics recordException(Throwable) not found — skipping")

        // ─── Mapbox Telemetry ──────────────────────────────────────────────
        MapboxTelemetryRequestStateFingerprint.methodOrNull
            ?.addInstructions(0, "return-void")
            ?: println("Telemetry: Mapbox setUserTelemetryRequestState not found — skipping")

        println("Telemetry: Beeline telemetry patch applied.")
    }
}
