# Warhammer Army Strategic Analyzer

## Overview
The Warhammer Army Strategic Analyzer is a fullstack statistical engine designed for Warhammer 40k 10th Edition list building. While many tools focus on individual unit math, this application aggregates army-wide activations to provide a holistic overview of damage output, utilizing probability distributions to highlight the strengths and weaknesses of a **2000p** list.

## 🎲 The Probability Engine: How It Works
This application utilizes a custom-built **Discrete Convolution Engine** to calculate hit distributions for Warhammer 40,000 armies. Unlike simple calculators that only provide a single average (Mean), this machine calculates the exact probability of every possible outcome.

### 1. The Core Methodology: Discrete Convolution
At the heart of the backend is the `ProbabilityMath` utility. To determine how multiple independent attacks interact, the machine uses Mathematical Convolution:
* **Single Die Profile**: Every attack is first converted into a small array. For example, a hit on a 4+ becomes `[0.5, 0.5]`, representing the 50% chance of 0 hits and 50% chance of 1 hit.
* **Iterative Merging**: The machine "convolves" these arrays together. If Unit A has a specific hit distribution, the engine calculates the combined probability of achieving a total sum of hits across all models and units.
* **Army-Wide Aggregation**: This process scales from a single model to an entire army list, resulting in a final probability curve that accounts for every variable simultaneously.


### 2. Warhammer 40k Rule Integration
The `HitProcessor` translates 10th Edition tabletop rules into raw data:
* **Natural Results**: It automatically enforces "Natural 1s always fail" and "Natural 6s always hit," regardless of the Ballistic Skill (BS) value.
* **Sustained Hits**: The engine handles exploding 6s (Sustained 1, 2, or D3).

### 3. Statistical Insights
Once the math engine produces the final distribution, the `DistributionAnalyzer` enriches the data with readable metrics:
* **The 80% Range**: Represents the "realistic" outcome window, specifically the range between the 10th and 90th percentiles.
* **Standard Deviation**: Measures how "swingy" or reliable a specific unit's damage output is relative to the mean.
* **Top 5% Luck**: Calculates the "ceiling" of what the army can achieve (the 95th percentile and above) if the dice go hot.

---

## Architecture and Design Patterns
The project follows a modern three-tier, decoupled architecture with a focus on reactive state management and type-safe data transfer.

### Frontend
- **Angular 19**: Utilizes the latest Signal-based reactivity for high-performance change detection and state management.
- **Reactive Persistence**: Implements a synchronization pattern between the application state and LocalStorage via side-effects.
- **Component-Driven Design**: Features a modular UI built with reusable presentation components and smart container components.

### Backend
- **Spring Boot 3.4**: Configured as a high-performance REST API providing a stateless calculation engine.
- **Functional Business Logic**: The service layer leverages the Java Streams API to process complex army-wide calculations in a declarative style.
- **Immutability**: Utilizes DTOs to ensure thread-safety and data integrity across the pipeline.
- **Automated Testing**: Employs the Spock Framework and Groovy for expressive, data-driven specification testing.

## Technical Specification
The application implements a multi-stage pipeline to transform raw unit data into statistical insights:
1. **Serialization**: Complex unit configurations (including toggles for Sustained Hits and Critical triggers) are serialized into a hierarchical JSON structure.
2. **Ingestion**: The Spring Boot controller deserializes payloads into DTOs.
3. **Processing**: A specialized service layer sanitizes wargaming stats and performs probability aggregation.

## Tech Stack
- **Languages**: Java 25, TypeScript, Groovy
- **Frameworks**: Spring Boot 3.4+, Angular 19+
- **Testing**: Spock Framework, JUnit 5
- **Build Tools**: Gradle, Angular CLI
- **Styling**: TailwindCSS

## Getting Started

### Prerequisites
- Java Development Kit (JDK) 25
- Node.js (Latest LTS)
- Angular CLI

### Backend Installation
1. Navigate to the `/backend` directory.
2. Build the project: `./gradlew clean build`
3. Start the server: `./gradlew bootRun`
The API will be available at `http://localhost:8080`.

### Frontend Installation
1. Navigate to the `/frontend` directory.
2. Install dependencies: `npm install`
3. Launch the development server: `ng serve`
The application will be accessible at `http://localhost:4200`.

## Testing
Run the backend test suite to verify the calculation pipeline:
```bash
cd backend
./gradlew test