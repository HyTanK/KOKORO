# 1. ビルド環境の構築
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY . .
# ⚠️【超重要】 以下の4行目の末尾が「-x test」になっているか確認、または書き換えてください
RUN ./gradlew bootJar -x test

# 2. 実行環境の構築
FROM eclipse-temurin:21-jre-jammy
ENV JAVA_TOOL_OPTIONS="-Xmx300m -Xms300m"
WORKDIR /app
COPY --from=build /app/build/libs/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
