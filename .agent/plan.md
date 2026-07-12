# Project Plan

chatBoat: An AI search engine app with Google and Apple login, and guest login using phone number verification. It should include features similar to OpenAI (ChatGPT), Gemini, Grok, and Claude.

## Project Brief

# chatBoat Project Brief

**chatBoat** is a cutting-edge AI search engine and conversational assistant designed for Android. It provides a seamless, intelligence-driven search experience similar to industry leaders like ChatGPT and Gemini, optimized for a wide range of device form factors through a modern, adaptive interface.

## Features
* **AI-Powered Conversational Search:** A central chat interface that allows users to perform complex queries, get summarized answers, and engage in multi-turn dialogues with a high-performance LLM.
* **Unified Authentication:** Secure and flexible login options including Google Sign-In, Apple ID integration, and a Guest mode verified via phone number (SMS).
* **Contextual Chat History:** A streamlined session management system that allows users to revisit, rename, or continue previous AI search threads.
* **Adaptive Multi-Pane UI:** A sophisticated layout that automatically adjusts between single-pane mobile views and multi-pane tablet/foldable views (e.g., list-detail for history and active chat).

## High-Level Technical Stack
* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Material 3)
* **Navigation:** Jetpack Navigation 3 (State-driven)
* **Layout Strategy:** Compose Material Adaptive Library (for edge-to-edge and foldable support)
* **Asynchronous Logic:** Kotlin Coroutines & Flow
* **Networking:** Retrofit & OkHttp (for AI API integration and authentication)
* **Image Loading:** Coil (for rendering AI-generated or shared images)

## Implementation Steps

### Task_1_AuthAndNavigation: Implement authentication (Google, Apple, Phone) and the main navigation structure using Navigation 3.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - Authentication UI for Google, Apple, and Guest (Phone) is implemented.
  - Navigation 3 state-driven flow between Auth and Main screens is functional.
  - Project builds successfully.
- **StartTime:** 2026-07-12 12:07:47 IST

### Task_2_AICoreAndDataLayer: Integrate AI API (e.g., Gemini) and implement local persistence using Room for chat history.
- **Status:** PENDING
- **Acceptance Criteria:**
  - API_KEY integrated and AI responses are fetched via Retrofit.
  - Room database stores and retrieves chat sessions and messages.
  - App does not crash during data operations.

### Task_3_AdaptiveUIAndM3Theme: Develop the adaptive multi-pane UI for chat and history using Material 3 and Edge-to-Edge display.
- **Status:** PENDING
- **Acceptance Criteria:**
  - List-detail layout adapts to mobile and tablet/foldable screens.
  - Full Edge-to-Edge display implemented.
  - Vibrant energetic Material 3 color scheme applied for light and dark modes.

### Task_4_AppAssetsAndIcons: Create an adaptive app icon and finalize branding assets.
- **Status:** PENDING
- **Acceptance Criteria:**
  - Adaptive app icon matching the chatBoat theme is generated.
  - App icon is correctly displayed on the launcher.

### Task_5_RunAndVerify: Perform a final run to verify application stability and requirement alignment.
- **Status:** PENDING
- **Acceptance Criteria:**
  - Project builds successfully.
  - All existing tests pass.
  - App does not crash during end-to-end usage.
  - Critic agent verifies alignment with project brief and UI requirements.

