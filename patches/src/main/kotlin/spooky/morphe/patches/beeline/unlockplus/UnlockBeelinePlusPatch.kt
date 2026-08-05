package spooky.morphe.patches.beeline.unlockplus

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.returnEarly

import spooky.morphe.patches.shared.Constants.COMPATIBILITY_BEELINE

@Suppress("unused")
val unlockBeelinePlusPatch = bytecodePatch(
    name = "Unlock Beeline Plus features",
    description = "Unlocks Beeline plus features such as round-trip generation, live traffic and premium map types.",
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
    }
}
