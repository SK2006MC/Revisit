# OfflineBrowser

A browser that stores everything locally.

## Description

OfflineBrowser is a unique browser designed to store all web content locally. This allows users to access and browse their favorite websites without needing an internet connection. This project is primarily for offline use and should not be used for sensitive information like passwords, email, OTP, etc.

## Features

- Store web pages locally
- Access stored web pages offline
- Simple and intuitive user interface
- Option to update stored webpages and contents
- Option to download webpages and contents
- JavaScript console

## Installation

To install and run OfflineBrowser, follow these steps:

### Method 1
Click [here](/release) to download the APK and then install it. Now it's ready to use.

### Method 2
1. Clone the repository:
   ```sh
   git clone https://github.com/SK2006MC/OfflineBrowser.git
   ```
2. Navigate to the project directory:
   ```sh
   cd OfflineBrowser
   ```
3. Build the APK:
   ```sh
   ./gradlew assembleRelease
   ```
4. Or for debugging:
   ```sh
   ./gradlew assembleDebug
   ```
5. Install it to your phone by sending the APK. The APK will be located in `build/output/release` or `build/output/debug`.
6. Alternatively, enable USB debugging on your phone and allow access for the computer to install the APK. To install, run:
   ```sh
   ./gradlew installRelease
   ```
   or
   ```sh
   ./gradlew installDebug
   ```

## Usage

1. Open the application.
   1.1. If it's the first time, select the location (folder) where you want to store the webpages.
   1.2. Then click Start.
2. Swipe from the left side of the screen to the right side to access the URL bar.
3. Enter the URL and click the Load URL button.
4. The webpage will be automatically stored in the location you specified.

## Contributing

Contributions are welcome! Please create a pull request or open an issue for any enhancements or bug fixes.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Contact

For any questions or inquiries, please contact [SK2006MC](https://github.com/SK2006MC).
