# MosaicCast TV — Mi Box Receiver

## ✅ Android TV APK built successfully

**[Download the APK artifact from the successful GitHub Actions run](https://github.com/tbenitz/mosaiccast-tv-local/actions/runs/39202533669)**

On that page scroll to **Artifacts** and download **MosaicCast-TV-Receiver-APK**. Extract the ZIP to get `app-debug.apk`. It is a debug-signed installable APK, not a Play Store release.

### Install on the Mi Box

1. Download the ZIP from GitHub on your Chromebook, unzip it, and copy `app-debug.apk` to a USB drive, or transfer it to the Mi Box using a trusted local file transfer method.
2. On Android TV, open the APK in a file manager and authorize that file manager under **Install unknown apps** when prompted.
3. Select **Install**, then launch **MosaicCast Receiver** from the TV's applications.
4. The screen displays your Mi Box local IP and a six-digit pairing PIN; arrow keys/OK should display a remote-test message even before pairing.
5. On the Chromebook, install and open the separately supplied **MosaicCast Local TV Sender Chrome extension**. Add demo feeds, enter TV IP and PIN, and choose **Connect Mi Box**. Both devices must be on the same local network. **Do not use Chrome's Cast button.**

### Limitations

The GitHub Pages video studio at https://tbenitz.github.io/mosaiccast-tv-local/ is separate from the Chromebook sender extension. The hosted web studio alone cannot drive the receiver. The Chromebook extension is required for WebRTC video and remote commands, and its code/binary has not yet been uploaded to this repository.

The APK build succeeded in GitHub Actions; Mi Box hardware, WebView WebRTC capability, pairing, and end-to-end video playback have **not** been hardware-tested. If the receiver reports WebRTC unavailable, try updating Android System WebView on the TV.

### Rebuild APK after changes

Open [Build MosaicCast Android TV APK](https://github.com/tbenitz/mosaiccast-tv-local/actions/workflows/build-apk.yml), select **Run workflow**, then download the newly produced artifact from the successful run.

### Project structure

- `app/`: Native Android TV receiver, local signaling endpoint, remote input handler and WebRTC WebView player.
- `.github/workflows/build-apk.yml`: Automated APK build.
- `docs/index.html`: Standalone browser video studio published via GitHub Pages.
- `docs/setup.html`: Browser studio and setup guidance.

Do not forward local signaling port 8765 to the public internet.
