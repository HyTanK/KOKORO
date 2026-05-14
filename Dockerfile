# 1. ビルド環境の構築
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY . .
RUN ./gradlew bootJar -x test

# 2. 実行環境の構築
FROM eclipse-temurin:21-jre-jammy
ENV JAVA_TOOL_OPTIONS="-Xmx300m -Xms300m"
WORKDIR /app

# ⚠️【修正】プロジェクト名「kokoro」に合わせた正確なファイル名に指定し直しました
COPY --from=build /app/build/libs/kokoro-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
