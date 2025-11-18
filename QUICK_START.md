# 빠른 배포 가이드

## 🚀 배포 순서

### 1단계: 백엔드 배포 (Render)

1. **Render 가입/로그인**: https://render.com
2. **데이터베이스 생성**:
   - New + → PostgreSQL 또는 MySQL
   - 데이터베이스 정보 기록
3. **Web Service 생성**:
   - New + → Web Service
   - GitHub 저장소 연결
   - 설정:
     - Name: `healthcare-backend`
     - Build Command: `./gradlew build -x test`
     - Start Command: `java -jar build/libs/*.jar`
4. **환경 변수 설정** (Render Dashboard → Environment):
   ```
   SPRING_PROFILES_ACTIVE=production
   SPRING_DATASOURCE_URL=jdbc:mysql://[HOST]:[PORT]/[DB]?useSSL=true&serverTimezone=Asia/Seoul
   SPRING_DATASOURCE_USERNAME=[USERNAME]
   SPRING_DATASOURCE_PASSWORD=[PASSWORD]
   JWT_SECRET=[32자 이상 랜덤 문자열]
   APP_JWT_SECRET=[32자 이상 랜덤 문자열]
   CLOUDINARY_CLOUD_NAME=[YOUR_CLOUD_NAME]
   CLOUDINARY_API_KEY=[YOUR_API_KEY]
   CLOUDINARY_API_SECRET=[YOUR_API_SECRET]
   OPENAI_API_KEY=[YOUR_OPENAI_API_KEY] (선택사항)
   ```
5. **배포 완료 후 백엔드 URL 확인**: `https://healthcare-backend.onrender.com`

### 2단계: 프론트엔드 배포 (Vercel)

1. **Vercel 가입/로그인**: https://vercel.com (GitHub 연동)
2. **프로젝트 추가**:
   - Add New... → Project
   - GitHub 저장소 선택
   - 설정:
     - Framework Preset: **Vite**
     - Root Directory: **frontend**
     - Build Command: `npm run build`
     - Output Directory: `dist`
3. **환경 변수 설정** (Vercel Dashboard → Settings → Environment Variables):
   ```
   VITE_API_BASE_URL=https://healthcare-backend.onrender.com/api
   ```
   ⚠️ **백엔드 URL을 실제 Render URL로 변경하세요!**
4. **배포 완료 후 프론트엔드 URL 확인**: `https://your-project.vercel.app`

### 3단계: CORS 설정 업데이트

프론트엔드 배포 후, Render의 환경 변수에 추가:
```
CORS_ALLOWED_ORIGINS=https://your-project.vercel.app
```

백엔드를 재배포하여 CORS 설정 적용

## ✅ 배포 확인

- 백엔드: `https://healthcare-backend.onrender.com/api/health`
- 프론트엔드: `https://your-project.vercel.app`
- 로그인/회원가입 테스트

## 📝 참고사항

- Render Free 플랜은 15분 비활성 시 sleep 상태
- 환경 변수는 절대 Git에 커밋하지 마세요
- 자세한 내용은 `DEPLOYMENT.md` 참고

