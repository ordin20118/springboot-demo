#!/bin/bash

# 운영 환경 실행 스크립트

echo "🚀 운영 환경으로 애플리케이션을 시작합니다..."

# 환경 변수 설정
export ENVIRONMENT=prod

# 운영용 환경 변수 설정
export SPRING_DATASOURCE_URL=${PROD_SPRING_DATASOURCE_URL}
export SPRING_DATASOURCE_USERNAME=${PROD_SPRING_DATASOURCE_USERNAME}
export SPRING_DATASOURCE_PASSWORD=${PROD_SPRING_DATASOURCE_PASSWORD}
export JWT_SECRET=${PROD_JWT_SECRET}
export JWT_EXPIRATION=${PROD_JWT_EXPIRATION:-3600000}

# 보안 확인
if [ -z "$PROD_SPRING_DATASOURCE_PASSWORD" ]; then
    echo "❌ 운영 환경 데이터베이스 비밀번호가 설정되지 않았습니다."
    echo "   .env 파일에서 PROD_SPRING_DATASOURCE_PASSWORD를 설정해주세요."
    exit 1
fi

if [ -z "$PROD_JWT_SECRET" ]; then
    echo "❌ 운영 환경 JWT 시크릿이 설정되지 않았습니다."
    echo "   .env 파일에서 PROD_JWT_SECRET을 설정해주세요."
    exit 1
fi

# Docker Compose 실행
docker-compose up --build -d

echo "✅ 운영 환경이 백그라운드에서 시작되었습니다."
echo "📱 애플리케이션: http://localhost:8080"
echo "📊 로그 확인: docker-compose logs -f app" 