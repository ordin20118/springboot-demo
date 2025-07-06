#!/bin/bash

# 개발 환경 실행 스크립트

echo "🚀 개발 환경으로 애플리케이션을 시작합니다..."

# 환경 변수 설정
export ENVIRONMENT=dev

# Docker Compose 실행
docker-compose -f docker-compose.dev.yml up --build

echo "✅ 개발 환경이 시작되었습니다."
echo "📱 애플리케이션: http://localhost:8080"
echo "📚 Swagger UI: http://localhost:8080/swagger-ui.html" 