FROM openjdk:17

# 빌드 시 dev 프로필을 적용하도록 환경 변수 설정
ARG PROFILE
ENV SPRING_PROFILES_ACTIVE=${PROFILE}

WORKDIR /app

# JAR 파일 복사
COPY build/libs/*.jar app.jar

# 실행 명령
CMD ["java", "-jar", "app.jar"]
