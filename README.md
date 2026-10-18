# MosaicCast TV Local

Browser video mosaic for Chromebook with a companion Android TV / Mi Box receiver project.

## Web studio

The web studio is published from [docs/index.html](docs/index.html). Once Pages is enabled, open https://tbenitz.github.io/mosaiccast-tv-local/ .

## What works

- Browser video mosaic with Grid / PiP / Focus layouts.
- Tab/window/screen capture using the browser's screen sharing dialog.
- Per-source mute and volume controls within the sender.
- Remote-friendly keyboard shortcuts when the keys reach the webpage.

## Important limitations

Casting this page to Mi Box through Chrome Cast **does not** transmit the Mi Box remote's arrow or OK keys to the Chromebook. Genuine remote control needs the separate Chromebook extension plus the native Android TV receiver. A website on GitHub Pages cannot itself remotely operate Chrome tabs on a Chromebook or act as an Android TV APK.

## Installation

See [docs/setup.html](docs/setup.html). The packaged Chromebook extension and Android TV source are separate deliverables and must be installed/built independently. They are not yet included in this repository.

## Security

Your media capture stays in your browser in the web studio. Chrome asks permission separately for each capture, and protected video may be unavailable. Never expose unauthenticated local receiver signaling to the public internet.
