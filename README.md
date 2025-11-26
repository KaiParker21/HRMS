# Human Resource Management System (Employee App) 📱💼

## 📖 Overview

The **Human Resource Management System (HRMS)** is a comprehensive, mobile-first Android application designed to fundamentally modernize workforce management. By digitizing traditional manual processes and eliminating the inefficiencies of disparate data silos and paper trails, this system offers a unified, secure, and highly accessible platform for essential HR functions.

This repository hosts the **Employee-side** mobile application, engineered with **Jetpack Compose** and **Kotlin**, and powered by **Firebase**. It provides a seamless user experience for attendance tracking, leave management, and payroll document access, backed by robust real-time data synchronization and secure serverless operations.

## ✨ Key Features

* **🔐 Enterprise-Grade Authentication:** A secure signup and login workflow featuring mandatory email verification via Firebase Authentication. This ensures that only verified identities can access the system.

* **⏱️ Real-time Attendance Tracking:** A precise, one-tap "Clock In" and "Clock Out" interface. The system utilizes real-time listeners to provide instantaneous status updates across all user devices, preventing duplicate entries and ensuring accurate time-logging.

* **📅 Comprehensive Leave Management:** A streamlined workflow for applying for various leave types (Paid, Sick, Unpaid). The app provides a persistent history view with real-time status tracking (Pending, Approved, Rejected), offering employees transparency regarding their requests.

* **📄 Secure Document Portal:** An encrypted repository for employees to access essential HR documents. Users can securely download and view monthly payslips directly on their device via Firebase Storage.

* **🗓️ Interactive Holiday Calendar:** A read-only, interactive view of the organizational holiday schedule, helping employees plan their leave and work schedules effectively.

* **🛡️ Data Privacy & Security:** A strict, zero-trust security model enforced via granular Firestore Security Rules. The system ensures complete data isolation where employees are technically restricted to accessing only their own records.

* **⚡ Robust Offline Capabilities:** A fully functional offline mode utilizing local caching. Employees can perform read/write operations (like clocking in) even without an internet connection; the system automatically synchronizes data with the backend once network connectivity is restored.

## 🛠️ Technology Stack

### Frontend (Android)

* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Declarative UI)
* **Architecture:** MVVM (Model-View-ViewModel)
* **Dependency Injection:** Hilt
* **Async Operations:** Kotlin Coroutines & Flow

### Backend (Serverless)

* **Authentication:** Firebase Authentication (User management & Identity)
* **Database:** Cloud Firestore (NoSQL, Real-time)
* **Storage:** Firebase Storage (Secure file hosting for PDF assets)
* **Infrastructure:** Serverless architecture requiring no manual backend maintenance.

## 🏗️ System Architecture

The application utilizes a robust client-server model leveraging Firebase as a Backend-as-a-Service (BaaS).

* **Client Layer:** The Android application encapsulates all business logic within strictly typed ViewModels. This layer handles user input, validates data locally, and manages UI state (Loading, Success, Error).
* **Data Flow:** User actions trigger calls to the Repository layer, which interfaces with Firestore. The UI layer observes data streams via real-time listeners, guaranteeing that the interface is always reactive.
* **Security:** Business rules and data validation are enforced server-side via Firestore Security Rules, ensuring data integrity and privacy.

**Project Structure:**

```text
com.company.hrms
├── ui/           # Jetpack Compose screens, reusable components & theme definitions
├── viewmodel/    # State holders & business logic implementation
├── data/         # Repository pattern implementation & Data source abstraction
├── model/        # Kotlin data classes & Firestore document schemas
├── auth/         # Authentication wrapper classes & session management
└── utils/        # Extension functions, date formatters & utilities
```
## 📸 Screenshots

### **Login & Authentication**
![Login](https://github.com/KaiParker21/HRMS/blob/master/assets/screenshots/Screenshot_20251115_195618_HRMS.jpg)
![Login 2](https://github.com/KaiParker21/HRMS/blob/master/assets/screenshots/Screenshot_20251115_195642_HRMS.jpg)

### **Dashboard**
![Dashboard](https://github.com/KaiParker21/HRMS/blob/master/assets/screenshots/Screenshot_20251115_195706_HRMS.jpg)
![Dashboard 2](https://github.com/KaiParker21/HRMS/blob/master/assets/screenshots/Screenshot_20251115_195713_HRMS.jpg)
![Dashboard 3](https://github.com/KaiParker21/HRMS/blob/master/assets/screenshots/Screenshot_20251115_195723_HRMS.jpg)
![Dashboard 4](https://github.com/KaiParker21/HRMS/blob/master/assets/screenshots/Screenshot_20251115_195728_HRMS.jpg)

### **Profile / Employee**
![Profile](https://github.com/KaiParker21/HRMS/blob/master/assets/screenshots/Screenshot_20251115_195818_HRMS.jpg)
![Profile 2](https://github.com/KaiParker21/HRMS/blob/master/assets/screenshots/Screenshot_20251115_195829_HRMS.jpg)

### **Attendance**
![Attendance](https://github.com/KaiParker21/HRMS/blob/master/assets/screenshots/Screenshot_20251115_195836_HRMS.jpg)
![Attendance 2](https://github.com/KaiParker21/HRMS/blob/master/assets/screenshots/Screenshot_20251115_195843_HRMS.jpg)
![Attendance 3](https://github.com/KaiParker21/HRMS/blob/master/assets/screenshots/Screenshot_20251115_195850_HRMS.jpg)
![Attendance 4](https://github.com/KaiParker21/HRMS/blob/master/assets/screenshots/Screenshot_20251115_195856_HRMS.jpg)

### **Leave Management**
![Leave](https://github.com/KaiParker21/HRMS/blob/master/assets/screenshots/Screenshot_20251115_195904_HRMS.jpg)
![Leave 2](https://github.com/KaiParker21/HRMS/blob/master/assets/screenshots/Screenshot_20251115_195926_HRMS.jpg)
![Leave 3](https://github.com/KaiParker21/HRMS/blob/master/assets/screenshots/Screenshot_20251115_195949_HRMS.jpg)

### **Admin / Other Screens**
![Admin](https://github.com/KaiParker21/HRMS/blob/master/assets/screenshots/Screenshot_20251115_200014_HRMS.jpg)
![Admin 2](https://github.com/KaiParker21/HRMS/blob/master/assets/screenshots/Screenshot_20251115_200023_HRMS.jpg)
![Admin 3](https://github.com/KaiParker21/HRMS/blob/master/assets/screenshots/Screenshot_20251115_200027_HRMS.jpg)
![Admin 4](https://github.com/KaiParker21/HRMS/blob/master/assets/screenshots/Screenshot_20251115_200112_HRMS.jpg)


