<div align="center">

# 🌿 Plant Verdure

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
[![Java](https://img.shields.io/badge/Language-Java-blue.svg)](https://www.java.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg)](https://android-arsenal.com/api?level=21)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](http://makeapullrequest.com)

**An intelligent Android application for plant species identification and disease detection using AI-powered image recognition**

[Features](#-features) • [Tech Stack](#-tech-stack) • [Installation](#-installation) • [Usage](#-usage) • [Screenshots](#-screenshots) • [Contributing](#-contributing)

</div>

---

## 📋 Overview

Plant Verdure is an advanced Android application that leverages the power of machine learning and computer vision to help plant enthusiasts, gardeners, and farmers identify plant species and detect early signs of plant diseases. With a user-friendly interface and powerful AI capabilities, the app provides instant predictions and personalized care recommendations to keep your plants healthy and thriving.

## ✨ Features

### 🔍 Core Features
- **Plant Species Identification**: Upload or capture plant images to instantly identify species with high accuracy
- **Disease Detection**: Early detection of plant diseases through AI-powered image analysis
- **Real-time Predictions**: Get instant results with confidence scores for each prediction
- **Image Management**: Capture photos directly from camera or upload from gallery

### 📊 Data & Storage
- **Plant Database**: Store and manage detailed information about your plants
- **History Tracking**: Keep track of all your plant identifications and diagnoses
- **Offline Access**: View previously saved plant data without internet connection
- **Cloud Sync**: Backup and sync your plant collection across devices

### 💡 Smart Recommendations
- **Personalized Care Tips**: Receive customized care recommendations based on plant species
- **Disease Treatment**: Get actionable advice for treating identified plant diseases
- **Growth Tracking**: Monitor and track your plant's health over time
- **Notification Reminders**: Set watering and care reminders for your plants

### 🎨 User Experience
- **Intuitive Interface**: Clean, modern, and easy-to-navigate design
- **Dark Mode Support**: Comfortable viewing in any lighting condition
- **Multi-language Support**: Available in multiple languages
- **Accessibility Features**: Designed to be accessible to all users

## 🛠️ Tech Stack

### **Frontend**
- **Language**: Java / XML
- **Architecture**: MVVM (Model-View-ViewModel)
- **UI Framework**: Android XML Layouts
- **Material Design**: Material Design 3 Components

### **Machine Learning**
- **ML Framework**: TensorFlow Lite / ML Kit
- **Model Type**: Convolutional Neural Networks (CNN)
- **Image Processing**: OpenCV / Android ImageAnalysis
- **Pre-trained Models**: Custom trained models for plant classification

### **Backend & Storage**
- **Local Database**: Room Persistence Library
- **Preferences**: SharedPreferences / DataStore
- **Cloud Storage**: Firebase Storage (optional)
- **Backend Services**: Firebase / Custom REST API

### **Android Jetpack Components**
- **Navigation**: Navigation Component
- **Lifecycle**: LiveData & ViewModel (Java)
- **Camera**: CameraX API
- **Async Operations**: Java Threads / AsyncTask (or RxJava, if required)
- **Dependency Injection**: Dagger / Koin (Java support)

### **Image Processing**
- **Image Capture**: CameraX
- **Image Compression**: Glide / Picasso
- **Image Cropping**: UCrop / Android Image Cropper

### **Additional Libraries**
- **Networking**: Retrofit / OkHttp
- **JSON Parsing**: Gson / Moshi
- **Image Loading**: Glide / Picasso
- **Analytics**: Firebase Analytics
- **Crash Reporting**: Firebase Crashlytics

## 📦 Installation

### Prerequisites
Before you begin, ensure you have the following installed:
- **Android Studio**: Arctic Fox (2020.3.1) or later
- **JDK**: Java Development Kit 8 or higher
- **Android SDK**: API Level 21 (Android 5.0) or higher
- **Gradle**: 7.0 or higher (usually bundled with Android Studio)
- **Git**: For version control

### Step 1: Clone the Repository
```bash
git clone https://github.com/Venkatesh123-start/Verdure.git
cd Verdure
```

### Step 2: Open in Android Studio
1. Launch Android Studio
2. Select **File > Open**
3. Navigate to the cloned repository folder
4. Click **OK** to open the project

### Step 3: Sync Project with Gradle Files
Android Studio will automatically prompt you to sync the project. If not:
1. Click **File > Sync Project with Gradle Files**
2. Wait for the sync to complete

### Step 4: Configure API Keys (if applicable)
If the app uses external APIs or Firebase:
1. Create a `local.properties` file in the root directory (if not exists)
2. Add your API keys:
```properties
FIREBASE_API_KEY=your_firebase_api_key
ML_API_KEY=your_ml_api_key
```

### Step 5: Build the Project
1. Click **Build > Make Project** or press `Ctrl+F9` (Windows/Linux) or `Cmd+F9` (Mac)
2. Resolve any dependency issues if they arise

### Step 6: Run the Application

#### On a Physical Device:
1. Enable **Developer Options** on your Android device
2. Enable **USB Debugging**
3. Connect your device via USB
4. Click the **Run** button or press `Shift+F10`
5. Select your device from the list

#### On an Emulator:
1. Click **Tools > AVD Manager**
2. Create a new Virtual Device (API 21+)
3. Click the **Run** button or press `Shift+F10`
4. Select your emulator from the list

### Step 7: Grant Permissions
When the app launches for the first time, grant the following permissions:
- **Camera**: For capturing plant images
- **Storage**: For accessing and saving images
- **Internet**: For ML model updates and cloud features (if applicable)

## 🚀 Usage

### Getting Started

1. **Launch the App**: Open Plant Verdure from your app drawer

2. **Identify a Plant**:
   - Tap the camera icon on the home screen
   - Choose to capture a new photo or select from gallery
   - Ensure the plant is well-lit and in focus
   - Tap the capture/select button
   - Wait for the AI to analyze the image
   - View the identification results with confidence score

3. **Detect Plant Disease**:
   - Select the disease detection mode
   - Upload or capture an image of the affected plant part
   - Review the diagnosis results
   - Access treatment recommendations

4. **Save Plant Information**:
   - After identification, tap "Save Plant"
   - Add custom notes and care schedules
   - View saved plants in your collection

5. **Access Care Recommendations**:
   - Navigate to the plant details page
   - Review personalized care tips
   - Set reminders for watering and fertilizing

### Tips for Best Results
- **Good Lighting**: Ensure adequate natural or artificial lighting
- **Clear Focus**: Keep the plant in focus and avoid blurry images
- **Close-up Shots**: Capture leaves, flowers, or affected areas closely
- **Multiple Angles**: Try different angles for better identification
- **Clean Background**: Remove cluttered backgrounds when possible

## 📸 Screenshots

> Add screenshots of your app here to showcase the UI/UX

<div align="center">

| Home Screen | Plant Identification | Disease Detection | Care Recommendations |
|------------|---------------------|-------------------|---------------------|
| ![Home](screenshots/home.png) | ![Identify](screenshots/identify.png) | ![Disease](screenshots/disease.png) | ![Care](screenshots/care.png) |

</div>

## 🏗️ Project Structure

```
Verdure/
├── src/
│   ├── androidTest/                       # Instrumented UI tests (Java)
│   ├── main/
│   │   ├── AndroidManifest.xml
│   │   ├── ic_launcher-playstore.png
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           ├── ui/            # UI components (Activities/Fragments - Java)
│   │   │           ├── viewmodel/     # ViewModels (Java)
│   │   │           ├── repository/    # Data repositories (Java)
│   │   │           ├── model/         # Data models (Java)
│   │   │           ├── database/      # Room database (Java)
│   │   │           ├── ml/            # ML model integration (Java)
│   │   │           ├── utils/         # Utility classes (Java)
│   │   │           └── di/            # Dependency injection (Java)
│   │   └── res/                      # Resources (layouts, drawables, etc. - XML)
│   └── test/                         # Unit tests (Java)
├── gradle/
├── build.gradle
├── settings.gradle
└── README.md
```
*Note: All main app code and structure are in Java/XML, inside `com/example/` and XML resources under `res/`.*

## 🧪 Testing

### Run Unit Tests
```bash
./gradlew test
```

### Run Instrumentation Tests
```bash
./gradlew connectedAndroidTest
```

### Run All Tests
```bash
./gradlew testDebugUnitTest connectedAndroidTest
```

## 🤝 Contributing

We welcome contributions from the community! Here's how you can help:

### How to Contribute

1. **Fork the Repository**
   ```bash
   # Click the 'Fork' button at the top right of this page
   ```

2. **Clone Your Fork**
   ```bash
   git clone https://github.com/YOUR_USERNAME/Verdure.git
   cd Verdure
   ```

3. **Create a Feature Branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

4. **Make Your Changes**
   - Write clean, maintainable code
   - Follow the existing code style
   - Add comments where necessary
   - Update documentation if needed

5. **Test Your Changes**
   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

6. **Commit Your Changes**
   ```bash
   git add .
   git commit -m "Add: your feature description"
   ```

7. **Push to Your Fork**
   ```bash
   git push origin feature/your-feature-name
   ```

8. **Create a Pull Request**
   - Go to the original repository
   - Click "New Pull Request"
   - Select your fork and branch
   - Describe your changes clearly
   - Submit the pull request

### Contribution Guidelines
- Follow Android development best practices
- Maintain consistent code formatting
- Write meaningful commit messages
- Add unit tests for new features
- Update documentation for significant changes
- Be respectful and constructive in discussions

### Code of Conduct
- Be respectful and inclusive
- Welcome newcomers and help them learn
- Focus on constructive feedback
- Respect differing viewpoints and experiences

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2024 Verdure

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## 📞 Contact & Support

### Get in Touch
- **GitHub**: [@Venkatesh123-start](https://github.com/Venkatesh123-start)
- **Repository**: [Verdure](https://github.com/Venkatesh123-start/Verdure)
- **Issues**: [Report a Bug](https://github.com/Venkatesh123-start/Verdure/issues)
- **Discussions**: [Join the Discussion](https://github.com/Venkatesh123-start/Verdure/discussions)

### Support
If you encounter any issues or have questions:
1. Check the [Issues](https://github.com/Venkatesh123-start/Verdure/issues) page
2. Search existing issues before creating a new one
3. Provide detailed information when reporting bugs
4. Include screenshots or logs when applicable

### Community
- ⭐ Star this repository if you find it helpful
- 🐛 Report bugs and request features
- 💡 Share your ideas and suggestions
- 📖 Improve documentation
- 🔀 Submit pull requests

## 🙏 Acknowledgments

- Thanks to the open-source community for amazing libraries and tools
- Plant disease datasets from [PlantVillage](https://plantvillage.psu.edu/)
- ML models inspired by research in computer vision and plant pathology
- Icons and images from [Material Design Icons](https://materialdesignicons.com/)
- All contributors who have helped improve this project

## 🗺️ Roadmap

### Upcoming Features
- [ ] AR (Augmented Reality) plant identification
- [ ] Community forum for plant care tips
- [ ] Integration with smart garden devices
- [ ] Plant growth time-lapse feature
- [ ] Social sharing capabilities
- [ ] Multi-plant detection in a single image
- [ ] Plant care chatbot powered by AI
- [ ] Weather-based care recommendations

### Version History
- **v1.0.0** - Initial release with basic plant identification
- **v1.1.0** - Added disease detection feature
- **v1.2.0** - Improved ML model accuracy
- **Future** - See roadmap above

---

<div align="center">

**Made with ❤️ by the Plant Verdure Team**

*Helping plants thrive, one identification at a time* 🌱

[⬆ Back to Top](#-plant-verdure)

</div>
