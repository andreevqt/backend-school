FROM openjdk:18-alpine
WORKDIR /app
COPY . .
RUN chmod +x ./mvnw
RUN ./mvnw clean package
EXPOSE 80
ENTRYPOINT ["java","-jar","./target/app.jar"]