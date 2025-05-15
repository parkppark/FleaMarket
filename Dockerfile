# 1. Base Image 선택 (JDK 17 사용)
FROM openjdk:17-jdk-slim

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. 환경변수 설정
ENV SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/market?serverTimezone=UTC
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=12345

# 4. Gradle 빌드 결과물(JAR 파일) 복사
COPY build/libs/market-0.0.1-SNAPSHOT.jar /app/app.jar

# 5. 애플리케이션 포트 지정
EXPOSE 8080

# 6. 컨테이너 시작 시 JAR 파일 실행
CMD ["java", "-jar", "app.jar"]

# 파일 업로드 디렉토리 생성
RUN mkdir -p /app/uploads