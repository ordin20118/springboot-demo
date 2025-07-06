#!/bin/bash

echo "🧹 Docker 정리 스크립트 시작..."

# 1. Docker Compose 컨테이너 중지 및 삭제
echo "📦 Docker Compose 컨테이너 정리..."
docker-compose -f docker-compose.dev.yml down --rmi all -v 2>/dev/null || true
docker-compose down --rmi all -v 2>/dev/null || true

# 2. 특정 이미지 삭제
echo "🗑️  tipsy-api 관련 이미지 삭제..."
docker rmi tipsy-api-app:latest 2>/dev/null || true

# 3. 사용하지 않는 Docker 리소스 정리
echo "🧽 사용하지 않는 Docker 리소스 정리..."
docker system prune -f

# 4. 빌드 캐시 정리
echo "📋 빌드 캐시 정리..."
docker builder prune -f

# 5. 로컬 Gradle 캐시 정리 (권한 문제 시 주석 해제)
# echo "📚 Gradle 캐시 정리..."
# rm -rf .gradle 2>/dev/null || true

echo "✅ Docker 정리 완료!"
echo ""
echo "🎯 이제 깨끗한 상태에서 빌드할 수 있습니다:"
echo "   docker-compose -f docker-compose.dev.yml build --no-cache" 