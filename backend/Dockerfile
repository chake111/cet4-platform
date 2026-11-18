# 多阶段构建：构建阶段带 Maven + JDK，运行阶段只留 JRE

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# 镜像源同时供容器构建与 CI 使用；依赖目录用 BuildKit 缓存，源码改动不会重新下载
COPY maven-settings.xml ./maven-settings.xml
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn -B -q -s maven-settings.xml -DskipTests package

FROM eclipse-temurin:17-jre-jammy

# 非 root 运行
RUN groupadd --system app && useradd --system --gid app --home-dir /app --shell /usr/sbin/nologin app

WORKDIR /app
COPY --from=build /build/target/cet4-backend-*.jar /app/app.jar
RUN chown -R app:app /app

USER app
EXPOSE 8080

ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -Dfile.encoding=UTF-8"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]