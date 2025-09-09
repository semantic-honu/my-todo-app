# --- ビルドステージ ---
# GradleとJDK 21を含むイメージをベースにする
FROM gradle:jdk21 AS build

# 作業ディレクトリを設定
WORKDIR /app

# ビルドに必要なファイルを先にコピーする
# (依存関係のダウンロードをキャッシュするため)
COPY build.gradle settings.gradle gradlew gradlew.bat ./
COPY gradle ./gradle

# 依存関係をダウンロードする
# RUN ./gradlew dependencies --no-daemon
# ↑ 依存関係の解決で問題が起きる場合があるため、一度ビルド全体を実行する方が確実です。

# ソースコードをコピーする
COPY src ./src

# Gradleを使ってアプリケーションをビルドする
# テストは実行しない(--x test)ことでビルド時間を短縮できる
RUN ./gradlew build --no-daemon -x test


# --- 実行ステージ ---
# JREのみを含む軽量なイメージをベースにする
FROM openjdk:21-slim

# 作業ディレクトリを設定
WORKDIR /app

# ビルドステージから生成されたJARファイルのみをコピーする
COPY --from=build /app/build/libs/*.jar app.jar

# アプリケーションがリッスンするポートを8080に設定
EXPOSE 8080

# コンテナ起動時にアプリケーションを実行するコマンド
CMD ["java", "-jar", "app.jar"]
