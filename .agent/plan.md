# Project Plan

A Kotlin Multiplatform (KMP) desktop application that converts AAB (Android App Bundle) files to APK. It should be based on an existing Swing-based tool located at /Users/okpos/Downloads/MrSohnAabTool-main/src. The app should allow users to select an AAB file and output an APK file. Use Compose for Desktop.

## Project Brief

# Project Brief:
 AABTools

A Kotlin Multiplatform (KMP) desktop application designed to
 simplify the process of converting Android App Bundle (AAB) files to APK format. This tool modernizes an existing Swing-based
 implementation into a sleek, efficient desktop experience.

### Features
*   **File Selection:** Intuitive file picker to select
 source `.aab` files from the local file system.
*   **AAB to APK Conversion:** Core engine
 that processes the App Bundle and generates a installable APK file.
*   **Conversion Status Tracking:** Real-time
 progress bar and status updates during the extraction and conversion process.
*   **Output Management:** Option to specify the destination
 folder for the generated APK.

### High-Level Technical Stack
*   **Language:** Kotlin
*   **UI
 Framework:** Compose for Desktop (Jetpack Compose)
*   **Build System & Code Gen:** Gradle with **KSP (
Kotlin Symbol Processing)**
*   **Asynchronous Processing:** Kotlin Coroutines
*   **Dependency Injection:** Manual or
 lightweight DI (if needed for MVP)
*   **AAB Tooling:** Bundletool (underlying library for conversion
)

### UI Design Image
![UI Design](/Users/okpos/AndroidStudioProjects/AABTools/input_images/
aab_tools_ui_mockup.jpg)
Image path = /Users/okpos/AndroidStudioProjects
/AABTools/input_images/aab_tools_ui_mockup.jpg

## Implementation Steps
**Total Duration:** 30m 57s

### Task_1_SetupKMPDesktop: Configure the project for Kotlin Multiplatform and Compose for Desktop. Update build.gradle.kts and libs.versions.toml to include Compose Desktop plugins, KSP, and the Bundletool dependency.
- **Status:** COMPLETED
- **Updates:** Project successfully configured for KMP with a desktop (JVM) target.
- **Acceptance Criteria:**
  - Project successfully configured for KMP with a desktop (JVM) target
  - Bundletool and Compose Desktop dependencies are correctly added
  - Initial desktop application builds and runs successfully
- **Duration:** 4m 35s

### Task_2_ImplementConversionLogic: Implement the core AAB to APK conversion logic using the Bundletool library. Port relevant logic from the existing Swing-based tool located at /Users/okpos/Downloads/MrSohnAabTool-main/src to a modern Kotlin implementation.
- **Status:** COMPLETED
- **Updates:** Analyzed legacy Swing source code at /Users/okpos/Downloads/MrSohnAabTool-main/src.
- **Acceptance Criteria:**
  - AabConverter service implemented using Bundletool APIs
  - Logic correctly handles AAB to APK conversion (universal mode)
  - Conversion process provides progress updates via Coroutines or Flow
- **Duration:** 21m 49s

### Task_3_BuildDesktopUI: Create the Compose for Desktop UI. Implement file selection for AAB files, output directory selection, and the progress/status screen based on the provided mockup.
- **Status:** COMPLETED
- **Updates:** Implemented Output Folder Selection UI using AWT FileDialog.
- **Acceptance Criteria:**
  - UI includes functional file pickers and conversion controls
  - Real-time progress bar and status updates are linked to the conversion engine
  - The implemented UI must match the design provided in /Users/okpos/AndroidStudioProjects/AABTools/input_images/aab_tools_ui_mockup.jpg
- **Duration:** 1m 46s

### Task_4_RunAndVerify: Finalize the application by integrating all components, ensuring stability, and verifying the end-to-end conversion process.
- **Status:** COMPLETED
- **Updates:** Final verification by critic agent successful.
All core features implemented: AAB selection, Output folder selection, AAB metadata extraction, Single APK output.
UI aligns with mockup and uses Material 3.
Custom app icon integrated.
App is stable and responsive.
Conversion logic using Bundletool is fully functional.
- **Acceptance Criteria:**
  - Full AAB to APK conversion workflow is successful and stable
  - Application does not crash during processing
  - Build passes and app aligns with user requirements
  - Final verification by critic agent confirms UI fidelity and functional stability
- **Duration:** 2m 47s

