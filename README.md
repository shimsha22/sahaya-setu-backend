# Sahaya-Setu Backend

This is the Spring Boot REST API for the Sahaya-Setu SHG ledger application.

## Requirements
* Java Development Kit (JDK)
* PostgreSQL

## Database Configuration
1. Create a PostgreSQL database named `sahaya_db`.
2. Open `src/main/resources/application.properties` and update the database credentials:
   
   spring.datasource.url=jdbc:postgresql://localhost:5432/sahaya_db
   spring.datasource.username=YOUR_POSTGRES_USERNAME
   spring.datasource.password=YOUR_POSTGRES_PASSWORD

## Setup Instructions
1. Clone the repository:
   git clone https://github.com/YOUR_USERNAME/sahaya-setu-backend.git

2. Open the project folder in IntelliJ IDEA or Eclipse.

3. Run the application by starting the `SahayaSetuApplication.java` main class.

The server will start on http://localhost:8080. On the first run, Hibernate will automatically create the necessary database tables.
