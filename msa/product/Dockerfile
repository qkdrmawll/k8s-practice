# 멀티 스테이지 빌드 방법 사용
# 첫번째 스테이지
FROM openjdk:11 as stage1

WORKDIR /app

# /app/gradlew 파일이 생성
COPY gradlew .
# /app/gradle 폴더로 생성
COPY gradle gradle
COPY src src
COPY build.gradle .
COPY settings.gradle .

RUN ./gradlew bootJar

# 두번째 스테이지
# 첫번째 스테이지에서 jar 파일만 받아와서 컨테이너의 크기를 줄인다

FROM openjdk:11
WORKDIR /app
# stage1에 있는 jar를 app.jar라는 이름으로 copy하는 명령어
COPY --from=stage1 /app/build/libs/*.jar app.jar
# cmd 또는 entrypoint를 통해 컨테이너를 실행
ENTRYPOINT ["java","-jar","app.jar"]

# docker 컨테이너 내에서 밖의 전체 host를 지징하는 도메인 :host.docker.internal
# docker를 실행하면서 주입하는 방식
# docker run -d -p 8080:8080 -e SPRING_DATASOURCE_URL=jdbc:mariadb://host.docker.internal:3308/ordersystem qkdrmawll/ordersystem:latest

# 컨테이너 실행시에 볼륨을 설정할때는 -v 옵션 사용
# docker run -d -p 8080:8080 -e SPRING_DATASOURCE_URL=jdbc:mariadb://host.docker.internal:3308/board -v /Users/qkdrmawll/Desktop/tmp_logs:/app/logs spring_test:latest

