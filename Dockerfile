FROM openjdk:17

WORKDIR /app

# JAR 파일 복사
COPY build/libs/*.jar app.jar

# 실행 명령
CMD ["java", "-jar", "app.jar"]