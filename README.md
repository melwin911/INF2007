# Smart Appointment Booking Application

## Overview
This project is a mobile healthcare application designed to simplify appointment booking, streamline patient check-ins, and integrate a chatbot for general health inquiries. The application is built with Kotlin for Android and incorporates various modern technologies to enhance the user experience.

## Link to video
Youtube link: https://youtu.be/00x-Terkt94

## Features
1. **Seamless Appointment Booking**: Allows users to book medical appointments easily by selecting hospitals, doctors, and available time slots.
2. **Efficient QR-based Check-in**: Reduces wait times by enabling quick check-ins through QR code scanning.
3. **AI-powered Chatbot**: Provides users with instant assistance for medical queries, self-assessments, and healthcare advice.
4. **Real-time Data Management**: Uses Firebase for user authentication, data storage, and real-time synchronization.
5. **Secure Data Handling**: Implements robust security features to protect sensitive user data and comply with healthcare data privacy standards.

## Technologies Used
- **Kotlin**: Primary programming language for Android development.
- **Jetpack Compose**: Modern UI toolkit for building native Android interfaces.
- **Firebase**: Used for authentication, real-time data storage, and cloud functionalities.
- **Google Gemini**: AI model integration for chatbot functionality.
- **MVVM Architecture**: Clean architecture pattern for maintaining app logic and UI separation.

## Setup and Installation
1. **Clone the Repository**: Clone the project repository to your local machine.
2. **Install Dependencies**: Make sure you have Android Studio installed with the required Kotlin and Firebase plugins. Open the project in Android Studio, and it should automatically detect the necessary dependencies.
3. **Firebase Setup (if you wish to use your own databse)**: 
   - Create a Firebase project at the Firebase Console.
   - Add google-services.json to the project under '/app/google-services.json'
   - Under ‎build.gradle.kts, add 'id("com.google.gms.google-services") version "4.4.2" apply false'
   - In your app-level build.gradle.kts file: Add
      - id("com.google.gms.google-services")
   - Add Firestore Dependency: In the app-level build.gradle.kts:
      -  implementation(platform("com.google.firebase:firebase-bom:33.8.0"))
      -  implementation("com.google.firebase:firebase-firestore")
   -  Access Firestore in Code
      - val db = FirebaseFirestore.getInstance() 
   - Access to Firebase:
      - Email: teambraces.inf2007@gmail.com 
      - q1.w2e3r4
4. **Run the App**: Once everything is set up, build and run the app on an Android device or emulator.

## Usage
1. **Account Creation**: 
   - Users should start by creating an account within the app. This is necessary before they can log in and access any features.
   
2. **Booking Appointments**: 
   - Once logged in, users can navigate to the appointment booking section.
   - Select a healthcare facility, service type, date, time, and doctor to book an appointment.

3. **Check-in via QR Code**: 
   - When you arrive at the healthcare facility, use the app to scan the QR code for quick check-in.
   - **Location-based Check-in**: The check-in feature only works if the user's location is near the healthcare facility. The app will verify your proximity before allowing the check-in process to proceed.

4. **GPS Permission**: 
   - For the location-based check-in feature to work, users must enable location permissions on their device. The app will request access to the device's GPS in order to verify the user's proximity to the healthcare facility.

5. **Using the Chatbot**: 
   - Access the chatbot from the main menu to ask general medical questions and get instant responses.

## Future Work and Enhancements
- **Teleconsultation**: Adding support for virtual doctor consultations.
- **Augmented Reality (AR)**: Implementing AR-based indoor navigation for hospitals.
- **Improved AI**: Enhancing the AI chatbot with more advanced medical capabilities.

## Contributors
- **Chee Xuan Yang**: 2301938@sit.singaporetech.edu.sg
- **Dinie Zikry Bin Rudi**: 2301900@sit.singaporetech.edu.sg
- **Ng Min Yuan Jocelyn**: 2301930@sit.singaporetech.edu.sg
- **Chai Jun Yu**: 2301847@sit.singaporetech.edu.sg
- **Chia Qi Jun**: 2301848@sit.singaporetech.edu.sg
