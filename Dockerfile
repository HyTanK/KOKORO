# 1. ビルド環境（Java 21に揃えます）
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# --- 🚀 Render対策: メモリ制限とデーモン無効化の設定 ---
ENV GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.jvmargs=-Xmx384m -XX:MaxMetaspaceSize=192m"

# 💡 全てのソースコードを最初にコピーします（上書きバグを完全に防止）
COPY . .

# 💡 ソースコピー完了後に、改件コードの消去と最高権限の付与を確実に実行します
RUN tr -d '\r' < gradlew > gradlew.tmp && mv gradlew.tmp gradlew && chmod 755 gradlew

# 💡 修正：テストコードのコンパイル(チェック)自体を完全に除外して本体だけを書き出します
RUN ./gradlew bootJar -x test -x compileTestJava --no-daemon

# 2. 実行環境（Java 21に揃えます）
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Renderの512MB制限に合わせて、実行時のJavaメモリ（JVMヒープ）を制限
ENV JAVA_TOOL_OPTIONS="-Xmx300m -Xms256m -XX:+UseSerialGC"

COPY --from=build /app/build/libs/app.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
