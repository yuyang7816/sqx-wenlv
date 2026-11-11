# 生产镜像（需先执行 mvn clean package -Pprod -pl sqx-application -am）
FROM openjdk:8-jdk-slim

LABEL maintainer="sqx"

WORKDIR /sqx

COPY target/rongeyou.jar /app/rongeyou.jar

EXPOSE 9186

ENTRYPOINT ["java", "-jar", "/app/rongeyou.jar", "--spring.profiles.active=prod"]
