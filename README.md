# PNG Image Replacer Plugin

A simple Intellij Plugin that makes it easy to replace the content of existing PNG image files.dd

## Usage 

Use the right-click Refactor -> Replace with Clipboard Image popup menu option, either from the
project tree or by right-clicking an image.

![Image Replace popoup menu action](screenshot.png)

If there is a multi-resolution image in the clipboard the plugin will use the image with the highest resolution.


## Installation

Install either from the Intellij Marketplace or clone this repo and build with

```
./gradlew buildPlugin
```

If all goes well the plugin jar will be in the build/libs folder.

Install this jar using the "Install Plugin from Disk." option in the Intellij Plugin Settings:

![Plugin Installation from file](plugin-install.png)


Have fun!