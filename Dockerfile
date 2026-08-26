# ------------------------------------------------------------------------------
# Stage 1: Build the application
# ------------------------------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy build configuration first to leverage Docker layer caching for dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the application JAR
COPY src ./src
RUN mvn package -DskipTests

# ------------------------------------------------------------------------------
# Stage 2: Runtime environment
# ------------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-jammy AS runner

WORKDIR /app

# Create a non-root user for security best practices
RUN groupadd --system spring \
    && useradd --system --gid spring spring
USER spring:spring

# Copy only the compiled JAR from the builder stage
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
