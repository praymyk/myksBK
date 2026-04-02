# GEMINI.md

이 파일은 Gemini CLI의 동작 원칙과 프로젝트 컨텍스트를 공유하기 위한 문서입니다.

## 🛠 협업 원칙 (Mandates)

1. **코드 수정 전 승인 필수 (Pre-edit Approval)**: 
   - 모든 코드 수정(Directive) 수행 전, 변경될 코드의 **미리보기(Diff 또는 주요 변경 사항)**를 사용자에게 먼저 제시해야 합니다.
   - 사용자의 *g*"Y" 또는 명시적인 승인**이 있기 전까지는 실제 파일에 반영하지 않습니다.
   - 단, 단순 조회(Inquiry)나 분석 작업은 승인 없이 수행 가능합니다.

2. **기술 스택 준수**: 프로젝트 내 기존 설정(Java 25/21, Spring Boot 4.0.1 등)과 컨벤션을 엄격히 따릅니다.
3. **보안 주의**: API Key (`application.yml` 등)가 노출되지 않도록 각별히 유의합니다.

---

## 🔍 프로젝트 분석 정보 (Project Context)
### myksBK (백엔드 서비스)
- **기술 스택**:
  - **Framework**: Spring Boot 4.0.1
  - **Language**: Java 25 (빌드 환경에 따라 Java 21 사용 가능)
  - **DB**: MariaDB/MySQL (JPA/Hibernate)
  - **AI**: Gemini API (`gemini-3-flash-preview`), OpenAI API 연동
- **주요 도메인**:
  - `ai`: 템플릿 생성 및 품위 유지비(Dignity) 분석 로직 포함.
  - `customer`: 고객 관리 및 검색 (이름, 이메일 기반 Keyword 검색).
  - `auth/jwt`: JWT 기반 인증 시스템.
- **환경 설정**:
  - 로컬 포트: `8081`
  - 프로필: `dev`, `prod` 운영 중.

### 개발 환경 특이사항
- `gradlew` 실행 시 일부 환경에서 Segmentation Fault(139) 발생 가능성 있음.
- Java 25가 기본이나, Gradle 환경에 따라 Java 21(`corretto-21.0.9`)로 우회하여 빌드/테스트 수행 중.
