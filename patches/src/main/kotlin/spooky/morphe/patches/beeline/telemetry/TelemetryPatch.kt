package spooky.morphe.patches.beeline.telemetry

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

import spooky.morphe.patches.shared.Constants.COMPATIBILITY_BEELINE

private const val TELEMETRY_UTILS_CLASS = "Lcom/mapbox/common/TelemetryUtils;"
private const val SET_COLLECTION_STATE =
    "$TELEMETRY_UTILS_CLASS->setEventsCollectionState(ZLcom/mapbox/common/TelemetryUtilsResponseCallback;)V"

/**
 * No-op a void method found by [this] fingerprint, safely.
 *
 * Guards against two failure modes:
 *  - fingerprint not found  → report and skip (SDK absent in another build)
 *  - fingerprint matched a BODYLESS method (abstract declaration on an
 *    interface) → report and skip. Calling `addInstructions` on such a method
 *    NPEs inside the patcher (InstructionExtensions dereferences the
 *    non-existent implementation), which is exactly what happened when the
 *    Mapbox fingerprint targeted the `MapTelemetry` interface.
 */
private fun Fingerprint.noopVoid(label: String) {
    val method = methodOrNull ?: run {
        println("Telemetry: $label not found — skipping")
        return
    }
    if (method.instructionsOrNull == null) {
        println("Telemetry: $label matched an abstract/bodyless method — skipping")
        return
    }
    method.addInstructions(0, "return-void")
}

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
 *   • Mapbox Telemetry — `MapTelemetryImpl.enableTelemetryCollection(Z)` forced
 *     to pass `false` to `TelemetryUtils.setEventsCollectionState`, the single
 *     Java funnel that switches Mapbox events collection on/off
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
        - Mapbox map telemetry (events collection switched off at the SDK level)

        Does not affect app functionality — only telemetry collection is
        suppressed. Navigation, routing, maps and Plus features all keep working.
    """.trimIndent(),
    default = true,
) {
    compatibleWith(COMPATIBILITY_BEELINE)

    execute {
        // ─── Segment Analytics ─────────────────────────────────────────────
        // wc.c(Event) is the central dispatch for all Segment events.
        SegmentAnalyticsDispatchFingerprint.noopVoid("Segment Analytics dispatch (wc.c)")

        // ─── Firebase Analytics ────────────────────────────────────────────
        FirebaseAnalyticsLogEventFingerprint.noopVoid("Firebase Analytics logEvent")

        // ─── Firebase Crashlytics ──────────────────────────────────────────
        FirebaseCrashlyticsLogFingerprint.noopVoid("Firebase Crashlytics log(String)")
        FirebaseCrashlyticsRecordExceptionFingerprint.noopVoid(
            "Firebase Crashlytics recordException(Throwable)"
        )

        // ─── Mapbox Telemetry ──────────────────────────────────────────────
        // A plain no-op of enableTelemetryCollection would leave Mapbox's SDK
        // default state in place, so instead force the boolean passed to the
        // native switch to 0 (false) — the same state Beeline's own opt-out
        // dialog produces — making collection off deterministically.
        //
        // Body of enableTelemetryCollection(Z):
        //   new-instance p0, Ltz4;              # response callback (reuses p0)
        //   const/16  v0, 0x14
        //   invoke-direct {p0, v0}, Ltz4;-><init>(I)V
        //   invoke-static {p1, p0}, TelemetryUtils;->setEventsCollectionState(ZLcom/mapbox/common/TelemetryUtilsResponseCallback;)V
        //   return-void
        // p0 = callback (registerB), p1 = state flag (registerA). Replace the
        // invoke with const/4 v<p1>, 0x0 + the same invoke re-emitted.
        val funnel = MapboxTelemetryCollectionStateFingerprint.methodOrNull
        if (funnel == null) {
            println("Telemetry: Mapbox funnel (MapTelemetryImpl.enableTelemetryCollection) not found — skipping")
        } else {
            val instructionList = funnel.instructionsOrNull?.toList()
            if (instructionList == null) {
                println("Telemetry: Mapbox funnel matched a bodyless method — skipping")
            } else {
                val invokeIndex = instructionList.indexOfFirst { instr ->
                    ((instr as? ReferenceInstruction)?.reference as? MethodReference)
                        ?.let { it.definingClass == TELEMETRY_UTILS_CLASS && it.name == "setEventsCollectionState" }
                        ?: false
                }
                val invoke = instructionList.getOrNull(invokeIndex) as? FiveRegisterInstruction
                if (invokeIndex < 0 || invoke == null) {
                    println("Telemetry: setEventsCollectionState call not found inside Mapbox funnel — skipping")
                } else {
                    val stateReg = invoke.registerA       // p1 — the boolean flag
                    val callbackReg = invoke.registerB    // p0 — response callback
                    funnel.replaceInstruction(invokeIndex, "const/4 v$stateReg, 0x0")
                    funnel.addInstruction(invokeIndex + 1, "invoke-static {v$stateReg, v$callbackReg}, $SET_COLLECTION_STATE")
                    println("Telemetry: Mapbox events collection forced OFF (setEventsCollectionState(false)).")
                }
            }
        }

        println("Telemetry: Beeline telemetry patch applied.")
    }
}
