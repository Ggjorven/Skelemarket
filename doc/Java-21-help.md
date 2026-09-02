# Java 21 Help

This file provides **Java 21** install instructions for several platforms, including: [Windows](#Windows), [Linux](#Linux) & [MacOS](#MacOS)

## Windows

To install **Java 21** on **Windows** follow these instructions:

1. On **Windows** we utilize the installer, go to: [https://www.oracle.com/java/technologies/downloads/#jdk21-windows](https://www.oracle.com/java/technologies/downloads/#jdk21-windows)

2. Scroll down to the **x64 Installer** and click on the download link.

3. After it has finished downloading go to your downloads location and double click the **.exe** you just downloaded.

4. Now follow the installer and you should have **Java 21** installed on your system.

## Linux

To install **Java 21** with a way to easily switch back and forth between different versions we'll use **SDKMAN!** on **Linux**:

1. Install **SDKMAN!**:
    ```sh
    curl -s "https://get.sdkman.io" | bash
    source "$HOME/.sdkman/bin/sdkman-init.sh"
    ```

2. Install Temurin/OpenJDK 21:
    ```sh
    sdk install java 21.0.2-tem
    ```

3. Set **Java 21** as the default:
    ```sh
    sdk default java 21.0.2-tem
    ```

## MacOS

To install **Java 21** on **MacOS** follow these instructions:

1. // TODO: ...
