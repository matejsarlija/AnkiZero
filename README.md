# AnkiZero

AnkiZero is an Android application built with Jetpack Compose that helps users with flashcard learning. It leverages Firebase for analytics & crash reporting. Built entirely using agents.

## Architecture

The app follows modern Android architecture guidelines, utilizing Jetpack Compose for the UI layer. For more details on UI patterns, refer to the [Architecture Guide](architecture_guide.md).

## Building from Source

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/your-username/ankizero.git
    cd ankizero
    ```
2.  **Set up Firebase:**
    * Follow the instructions in [Firebase Setup](docs/FirebaseVerification.md) to configure your Firebase project and add the `google-services.json` file to the `app/` directory.
3.  **Build the project:**
    * Open the project in Android Studio.
    * Let Gradle sync and download dependencies.
    * Click on "Build" > "Make Project" or run `./gradlew assembleDebug` from the terminal.

## Running Tests

*   **Unit Tests:**
    ```bash
    ./gradlew testDebugUnitTest
    ```
*   **Android Instrumented Tests:**
    * Ensure an emulator is running or a device is connected.
    ```bash
    ./gradlew connectedDebugAndroidTest
    ```

## Firebase Setup

Detailed instructions for setting up Firebase for this project can be found in [docs/FirebaseVerification.md](docs/FirebaseVerification.md).

## Contributing

We welcome contributions! (Further guidelines will be added here).

## License

This project is currently unlicensed. (License information will be added here).
