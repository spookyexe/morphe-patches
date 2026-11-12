package spooky.morphe.patches.beeline.unlockplus

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.returnEarly

import spooky.morphe.patches.shared.Constants.COMPATIBILITY_BEELINE

/**
    Unlock Beeline Plus features: round-trip generation, live traffic and premium map types.

    The following patches are also needed for Firebase to fully work after
    re-signing (all from rushiranpise/morphe-patches, Universal set):
        - Fix Firebase after re-signing
        - Provide Original app certificate
        - Spoof app signature
        - Spoof install source

    Beeline 4.2.8704 note: `isPlusStyle` is returned FALSE on purpose (matches
    the upstream reference) — the premium map *types* become available once the
    map-style check stops treating them as Plus-exclusive styling.
 */
@Suppress("unused")
val unlockBeelinePlusPatch = bytecodePatch(
    name = "Unlock Beeline Plus features",
    description = """Unlocks Beeline plus features such as round-trip generation, live traffic and premium map types.
    The following patches are also needed for this patch to work:
        - Fix Firebase after re-signing (rushiranpise/morphe-patches)
        - Provide Original app certificate (rushiranpise/morphe-patches)
        - Spoof app signature (rushiranpise/morphe-patches)
        - Spoof install source (rushiranpise/morphe-patches)""",
    default = true
) {
    compatibleWith(COMPATIBILITY_BEELINE)

    execute {
        PremiumMapTypesFingerprint.method.returnEarly(false)
        MainViewModelisPlusActiveFingerprint.method.returnEarly(true)
        PlanRouteAutoRouteUiStateisBeelinePlusActiveFingerprint.method.returnEarly(true)
        OptionsDialogUiStateisPlusActiveFingerprint.method.returnEarly(true)
        OptionsDialogUiStateisPlusAvailableOrActiveFingerprint.method.returnEarly(true)
        SettingsViewModelgetShowPlusSignUpScreenFingerprint.method.returnEarly(false)
        isPlusAvailableInLocaleFingerprint.method.returnEarly(true)

        // 4.2.8704 superset: ViewModels gate Plus features straight off the
        // subscription state holder in addition to the wrapper getters above.
        PlusSubscriptionStateFingerprint.methodOrNull?.returnEarly(true)
    }
}
