#!/bin/bash

echo "🚀 Spring Boot Demo 테스트 실행"

echo ""
echo "📋 테스트 종류:"
echo "1. 전체 테스트 실행"
echo "2. 단위 테스트만 실행"
echo "3. 통합 테스트만 실행"
echo "4. API 테스트만 실행"
echo "5. 특정 테스트 클래스 실행"
echo "6. 테스트 커버리지 확인"

read -p "실행할 테스트 번호를 선택하세요 (1-6): " choice

case $choice in
    1)
        echo "🔍 전체 테스트 실행 중..."
        ./gradlew test
        ;;
    2)
        echo "🔍 단위 테스트 실행 중..."
        ./gradlew test --tests "*Test" --exclude-tests "*IntegrationTest" --exclude-tests "*ApiTest"
        ;;
    3)
        echo "🔍 통합 테스트 실행 중..."
        ./gradlew test --tests "*IntegrationTest"
        ;;
    4)
        echo "🔍 API 테스트 실행 중..."
        ./gradlew test --tests "*ApiTest"
        ;;
    5)
        read -p "테스트 클래스 이름을 입력하세요 (예: UserServiceTest): " testClass
        echo "🔍 $testClass 실행 중..."
        ./gradlew test --tests "*$testClass"
        ;;
    6)
        echo "🔍 테스트 커버리지 확인 중..."
        ./gradlew test jacocoTestReport
        echo "📊 커버리지 리포트: build/reports/jacoco/test/html/index.html"
        ;;
    *)
        echo "❌ 잘못된 선택입니다."
        exit 1
        ;;
esac

echo ""
echo "✅ 테스트 실행 완료!" 