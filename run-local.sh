#!/bin/bash

# 로컬 환경 실행 스크립트 (Docker 없이)

echo "🚀 로컬 환경으로 애플리케이션을 시작합니다..."

# 환경 변수 설정
export ENVIRONMENT=local

# 현재 디렉터리 확인
if [ ! -f "gradlew" ]; then
    echo "❌ gradlew 파일을 찾을 수 없습니다. 프로젝트 루트 디렉터리에서 실행하세요."
    exit 1
fi

# Gradle 빌드 (선택사항)
echo "🔨 애플리케이션 빌드 중..."
./gradlew build -x test

if [ $? -eq 0 ]; then
    echo "✅ 빌드 완료"
else
    echo "❌ 빌드 실패"
    exit 1
fi

# 애플리케이션 실행
echo "🏃 애플리케이션 실행 중..."
echo "📝 로그는 application.log 파일에 저장됩니다."

# 개발 프로파일로 실행
./gradlew bootRun --args='--spring.profiles.active=dev'

echo "🛑 애플리케이션이 종료되었습니다." 