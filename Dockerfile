# 1. 자바 25 버전의
FROM openjdk:25-ea-jdk-slim

# 2. 내장 톰캣 등을 위한 임시 저장소 볼륨 설정
VOLUME /tmp

# 3. 빌드된 jar 파일을 컨테이너 내부로 복사
# build/libs/*.jar
COPY build/libs/*.jar app.jar

# 4. 실행 명령어 (프로파일은 prod로 설정)
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=prod", "-jar", "/app.jar"]