# 1. ビルド環境
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# すべてのファイルをコピー
COPY . .

# ⚠️ 【超重要】126エラー（権限不足）を強制突破する魔法
RUN chmod +x gradlew

# ビルド実行（build.gradleの設定により app.jar が生成されます）
RUN ./gradlew bootJar -x test

# 2. 実行環境
FROM eclipse-temurin:21-jre-jammy
ENV JAVA_TOOL_OPTIONS="-Xmx300m -Xms300m"
WORKDIR /app

# ⚠️ 【修正】build.gradleで指定した「app.jar」をピンポイントでコピー
COPY --from=build /app/build/libs/app.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]