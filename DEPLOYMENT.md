# 배포 가이드

이 문서는 Healthcare Platform을 Render(백엔드)와 Vercel(프론트엔드)에 배포하는 방법을 설명합니다.

## 목차
1. [백엔드 배포 (Render)](#백엔드-배포-render)
2. [프론트엔드 배포 (Vercel)](#프론트엔드-배포-vercel)
3. [환경 변수 설정](#환경-변수-설정)

---

## 백엔드 배포 (Render)

### 1. Render 계정 생성 및 로그인
- [Render](https://render.com)에 가입/로그인

### 2. 데이터베이스 생성
1. Render Dashboard → **New +** → **PostgreSQL** (또는 MySQL)
2. 데이터베이스 이름: `healthcare-db`
3. 데이터베이스 정보 기록:
   - Internal Database URL
   - External Database URL
   - Host, Port, Database, Username, Password

### 3. Web Service 생성
1. Render Dashboard → **New +** → **Web Service**
2. GitHub 저장소 연결
3. 설정:
   - **Name**: `healthcare-backend`
   - **Environment**: `Java`
   - **Build Command**: `./gradlew build -x test`
   - **Start Command**: `java -jar build/libs/*.jar`
   - **Plan**: Free 또는 Paid

### 4. 환경 변수 설정
Render Dashboard → **Environment** 탭에서 다음 환경 변수 추가:

#### 필수 환경 변수
```
SPRING_PROFILES_ACTIVE=production
PORT=8080
```

#### 데이터베이스
```
SPRING_DATASOURCE_URL=jdbc:mysql://[HOST]:[PORT]/[DATABASE]?useSSL=true&serverTimezone=Asia/Seoul&characterEncoding=utf8
SPRING_DATASOURCE_USERNAME=[USERNAME]
SPRING_DATASOURCE_PASSWORD=[PASSWORD]
```

#### JWT
```
JWT_SECRET=[32자 이상의 랜덤 문자열]
APP_JWT_SECRET=[32자 이상의 랜덤 문자열]
```

#### Cloudinary
```
CLOUDINARY_CLOUD_NAME=[YOUR_CLOUD_NAME]
CLOUDINARY_API_KEY=[YOUR_API_KEY]
CLOUDINARY_API_SECRET=[YOUR_API_SECRET]
```

#### OpenAI (선택사항)
```
OPENAI_API_KEY=[YOUR_OPENAI_API_KEY]
```

#### CORS (프론트엔드 URL)
```
CORS_ALLOWED_ORIGINS=https://your-frontend.vercel.app,https://your-frontend.vercel.app
```
**중요**: Vercel 배포 후 실제 프론트엔드 URL로 변경해야 합니다!

### 5. 배포 확인
- 배포 완료 후: `https://healthcare-backend.onrender.com/api/health` 접속하여 확인
- 백엔드 URL 기록: `https://healthcare-backend.onrender.com`

---

## 프론트엔드 배포 (Vercel)

### 1. Vercel 계정 생성 및 로그인
- [Vercel](https://vercel.com)에 가입/로그인
- GitHub 계정 연동

### 2. 프로젝트 배포
1. Vercel Dashboard → **Add New...** → **Project**
2. GitHub 저장소 선택
3. 프로젝트 설정:
   - **Framework Preset**: Vite
   - **Root Directory**: `frontend`
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`
   - **Install Command**: `npm install`

### 3. 환경 변수 설정
Vercel Dashboard → **Settings** → **Environment Variables**에서 추가:

```
VITE_API_BASE_URL=https://healthcare-backend.onrender.com/api
```

### 4. 배포 확인
- 배포 완료 후 프론트엔드 URL 확인
- 예: `https://healthcare-platform.vercel.app`

---

## 환경 변수 설정

### 로컬 개발 환경
프로젝트 루트에 `.env` 파일 생성 (Git에 커밋하지 않음):

```env
# Backend
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/healthcare?useSSL=false&serverTimezone=Asia/Seoul
SPRING_DATASOURCE_USERNAME=hc_user
SPRING_DATASOURCE_PASSWORD=hc_password
JWT_SECRET=your-local-jwt-secret-key-32-chars-min
APP_JWT_SECRET=your-local-app-jwt-secret-key-32-chars-min
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret
OPENAI_API_KEY=your-openai-api-key

# Frontend
VITE_API_BASE_URL=http://localhost:8080/api
```

### 프로덕션 환경 (Render)
Render Dashboard의 Environment Variables에서 설정

### 프로덕션 환경 (Vercel)
Vercel Dashboard의 Environment Variables에서 설정

---

## 배포 후 확인 사항

### 백엔드
- [ ] Health Check: `https://[backend-url]/api/health`
- [ ] Swagger UI: `https://[backend-url]/swagger-ui.html`
- [ ] API 엔드포인트 테스트

### 프론트엔드
- [ ] 메인 페이지 로드 확인
- [ ] API 연결 확인 (브라우저 개발자 도구 Network 탭)
- [ ] 로그인/회원가입 기능 테스트

---

## 문제 해결

### 백엔드 배포 실패
- Build Command 확인
- Java 버전 확인 (Java 21 필요)
- 환경 변수 누락 확인

### 프론트엔드 API 연결 실패
- `VITE_API_BASE_URL` 환경 변수 확인
- CORS 설정 확인 (백엔드 WebConfig)
- 브라우저 콘솔 에러 확인

### 데이터베이스 연결 실패
- 데이터베이스 URL 형식 확인
- 방화벽 설정 확인 (Render의 경우 Internal URL 사용 권장)
- 데이터베이스 마이그레이션 확인

---

## 추가 참고사항

- Render의 Free 플랜은 15분간 비활성 시 서비스가 sleep 상태가 됩니다
- Vercel은 무료 플랜에서도 좋은 성능을 제공합니다
- 프로덕션 환경에서는 HTTPS를 사용하세요
- 환경 변수는 절대 Git에 커밋하지 마세요

