package spooky.morphe.patches.beeline.unlockplus

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ─────────────────────────────────────────────────────────────────────────────
// Fingerprints for Beeline. Names found unobfuscated (main package) are matched
// by definingClass; the obfuscated utility classes are matched by content
// filters (strings / unique method signatures) instead, so they survive
// re-obfuscation between builds.
//
// Validated against 4.2.8704 (xapk). Mapping table of obfuscated classes:
//   "/MainViewModel;"            co.beeline.ui.main.MainViewModel
//   "/PlanRouteAutoRouteUiState;" co.beeline.ui.route.viewmodels.PlanRouteAutoRouteUiState
//   "/OptionsDialogUiState;"     co.beeline.ui.common.dialogs.options.OptionsDialogUiState
//   "/SettingsViewModel;"        co.beeline.ui.settings.SettingsViewModel
//   isPlusStyle                  e96.smali   (MapType enum extensions)
//   isPlusAvailableInLocale      kn7.smali$b (RemoteConfig "androidPlusCountries" check)
//   PlusSubscriptionState        jw9.smali$a (RevenueCat/subscription state holder)
// ─────────────────────────────────────────────────────────────────────────────

object PremiumMapTypesFingerprint : Fingerprint(
    name = "isPlusStyle",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z"
)

object MainViewModelisPlusActiveFingerprint : Fingerprint(
    definingClass = "/MainViewModel;",
    name = "isPlusActive",
    returnType = "Z"
)

object PlanRouteAutoRouteUiStateisBeelinePlusActiveFingerprint : Fingerprint(
    definingClass = "/PlanRouteAutoRouteUiState;",
    name = "isBeelinePlusActive",
    returnType = "Z"
)

object OptionsDialogUiStateisUserAnonymousFingerprint : Fingerprint(
    definingClass = "/OptionsDialogUiState;",
    name = "isUserAnonymous",
    returnType = "Z"
)

object OptionsDialogUiStateisPlusActiveFingerprint : Fingerprint(
    definingClass = "/OptionsDialogUiState;",
    name = "isPlusActive",
    returnType = "Z"
)

object OptionsDialogUiStateisPlusAvailableOrActiveFingerprint : Fingerprint(
    definingClass = "/OptionsDialogUiState;",
    name = "isPlusAvailableOrActive",
    returnType = "Z"
)

object SettingsViewModelgetShowPlusSignUpScreenFingerprint : Fingerprint(
    definingClass = "/SettingsViewModel;",
    name = "getShowPlusSignUpScreen",
    returnType = "Z"
)

object isPlusAvailableInLocaleFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
    returnType = "Z",
    filters = listOf(string("androidPlusCountries"))
)

/**
 * The app's subscription state holder (single source of truth for "is Plus
 * active"). In 4.2.8704 several ViewModels read `a()Z` on this class DIRECTLY
 * (Home / PlanRoute / RouteMap / Destination / ride coordinator), bypassing the
 * wrapper getters above, so we patch the source of truth itself.
 *
 * Matcher: any parameterless `a()Z` in the class that also owns the
 * RevenueCat `CustomerInfo` handler (`b(Lcom/revenuecat/purchases/CustomerInfo;)V`)
 * — unique to the subscription manager.
 */
object PlusSubscriptionStateFingerprint : Fingerprint(
    name = "a",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
    returnType = "Z",
    custom = { _, classDef ->
        classDef.methods.any { m ->
            m.parameterTypes.singleOrNull() == "Lcom/revenuecat/purchases/CustomerInfo;" &&
                m.returnType == "V"
        }
    }
)
