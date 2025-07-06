#!/bin/bash

# JAR 파일 직접 실행 스크립트

echo "🚀 JAR 파일로 애플리케이션을 시작합니다..."

# JAR 파일 경로
JAR_FILE="build/libs/demo-0.0.1-SNAPSHOT.jar"

# JAR 파일 존재 확인
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ JAR 파일을 찾을 수 없습니다. 먼저 빌드를 실행하세요."
    echo "🔨 빌드 명령어: ./gradlew build"
    exit 1
fi

# 프로파일 설정 (기본값: dev)
PROFILE=${1:-dev}

echo "📋 프로파일: $PROFILE"
echo "📦 JAR 파일: $JAR_FILE"

# 애플리케이션 실행
echo "🏃 애플리케이션 실행 중..."
echo "🛑 종료하려면 Ctrl+C를 누르세요."

java -jar "$JAR_FILE" --spring.profiles.active=$PROFILE

echo "🛑 애플리케이션이 종료되었습니다." 