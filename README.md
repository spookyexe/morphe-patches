# 👋🧩 Spooky's Morphe Patches

## ❓ About

This repo contains some patches I made for myself. Feel free to use them and suggest improvements/ file a bug report.

## 🩹 Patches list

<!-- PATCHES_START EXPANDED -->
> **[v1.1.0-dev.2](https://github.com/spookyexe/morphe-patches/releases/tag/v1.1.0-dev.2)**&nbsp;&nbsp;•&nbsp;&nbsp;`dev`&nbsp;&nbsp;•&nbsp;&nbsp;2 patches total
<details open>
<summary>📦 Beeline&nbsp;&nbsp;•&nbsp;&nbsp;2 patches</summary>
<br>

**🎯 Supported versions:**

| 4.2.8014 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Disable Beeline telemetry](#disable-beeline-telemetry) | Stops Beeline from sending usage and crash telemetry to remote servers.<br><br>Disables:<br>- Segment Analytics (track / screen / identify events)<br>- Firebase Analytics (logEvent)<br>- Firebase Crashlytics (log messages, recorded exceptions)<br>- Mapbox map telemetry (events collection switched off at the SDK level)<br><br>Does not affect app functionality — only telemetry collection is<br>suppressed. Navigation, routing, maps and Plus features all keep working. |  |
| [Unlock Beeline Plus features](#unlock-beeline-plus-features) | Unlocks Beeline plus features such as round-trip generation, live traffic and premium map types.<br>    The following patches are also needed for this patch to work:<br>        - Fix Firebase after re-signing (rushiranpise/morphe-patches)<br>        - Provide Original app certificate (rushiranpise/morphe-patches)<br>        - Spoof app signature (rushiranpise/morphe-patches)<br>        - Spoof install source (rushiranpise/morphe-patches) |  |

</details>

<!-- PATCHES_END -->

#### How to use these patches

Click here to add these patches to Morphe: https://morphe.software/add-source?github=spookyexe/morphe-patches

Or manually add this repository url as a patch source in Morphe: https://github.com/spookyexe/morphe-patches

### 🛠️ Building

To build Spooky Patches,
you can follow the [Morphe documentation](https://github.com/MorpheApp/morphe-documentation).

## 📜 License

Spooky Patches are licensed under the [GNU General Public License v3.0](LICENSE)
