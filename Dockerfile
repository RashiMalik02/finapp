#Stage 1: to build the application
FROM eclipse-temurin:21-jdk-jammy AS builder

#Set the working directory inside the container
WORKDIR /app

#Install apache maven
#update package lists and install maven without the recommended packages to keep the layer small
RUN apt-get update && apt-get install -y --no-install-recommends maven && rm -rf /var/lib/apt/lists/*

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -Dmaven.test.skip=true


#Stage 2: build a production ready image

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

#Copy the final executable JAR file from the 'builder' stage's target directory
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8090

#defining the command to run the application when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]