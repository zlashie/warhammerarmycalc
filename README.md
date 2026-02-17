# Warhammer Army Strategic Analyzer

## Overview
The Warhammer Army Strategic Analyzer is a fullstack statistical engine designed for Warhammer 40k 10th Edition list building. While many tools focus on individual unit math, this application aggregates army-wide activations to provide a holistic overview of damage output, utilizing probability distributions to highlight the strengths and weaknesses of a **2000p** list.

## Architecture and Design Patterns
The project follows a modern three-tier, decoupled architecture with a focus on reactive state management and type-safe data transfer.

### Frontend
- **Angular 19**: Utilizes the latest Signal-based reactivity for high-performance change detection and state management.
- **Reactive Persistence**: Implements a synchronization pattern between the application state and LocalStorage via side-effects.
- **Component-Driven Design**: Features a modular UI built with reusable presentation components and smart container components.

### Backend
- **Spring Boot 3.4**: Configured as a high-performance REST API providing a stateless calculation engine.
- **Functional Business Logic**: The service layer leverages the Java Streams API to process complex army-wide calculations in a declarative style.
- **Immutability**: Utilizes Java Records for Data Transfer Objects (DTOs) to ensure thread-safety and data integrity across the pipeline.
- **Automated Testing**: Employs the Spock Framework and Groovy for expressive, data-driven specification testing.



## Technical Specification
The application implements a multi-stage pipeline to transform raw unit data into statistical insights:
1. **Serialization**: Complex unit configurations (including toggles for Lethal Hits, Sustained Hits, and Critical triggers) are serialized into a hierarchical JSON structure.
2. **Ingestion**: The Spring Boot controller deserializes payloads into immutable Record-based DTOs.
3. **Processing**: A specialized service layer sanitizes string-based wargaming stats (e.g., BS 3+, D6 Attacks) using regular expressions and performs probability aggregation.

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