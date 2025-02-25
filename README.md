<img src="MoeimEasy_Cover.jpg" alt="배너" width="100%"/>
<br/>
<br/>


# :computer: 프로젝트 개요
- 프로젝트 이름: MoeimEasy
- 프로젝트 설명: 회비 정산 및 납부, 일정 관리, 실시간 채팅이 가능한 모임 관리 서비스

<br/>

# :sunglasses: 팀원 및 역할 분담
| 이름   | 포지션 | 담당 역할 |
|--------|---|-----------|
| 장현수 |  BE | <ul><li>프로젝트 전체 일정 관리 및 진행 상황 점검</li><li>로그인</li><li>회원가입</li><li>아이디찾기</li><li>비밀번호 재설정</li><li>회원정보수정</li></ul> |
| 김지훈 |  BE |<ul><li>회의록 작성 및 프로젝트 주요 결정 사항 정리</li><li>일정 관리 페이지</li><li>게시판</li><li>모임 갤러리</li><li>헤더 및 사이드 바</li></ul> |
| 이유진 |  BE |<ul><li>UX 개선 및 인터페이스 설계</li><li>회원 초대 및 조회</li><li>모임 입장 및 생성</li><li>채팅</li><li>정산</li></ul>  |
| 안성준 |  BE |<ul><li>DB 설계 및 배포 환경 설정 및 관리</li><li>회비 조회</li><li>거래내역 조회</li><li>카테고리 별 소비내역</li></ul>    |

<br/>

# :mag: 주요 기능

- **회비 납부내역 조회**:
   - 회원이 속한 모임의 회비 납부내역 조회가 가능하다.

- **회비 입금 & 계좌 송금**:
  - "회비 납부" 버튼 클릭 후 모임 회비 납부 모달 창에서 회비 납부가 가능하다.  

- **거래내역 조회**:
  - 모임에 저장된 거래내역과 당월 잔액, 수입, 지출 내역 조회가 가능하다.

- **카테고리별 지출내역 조회**:
  - 카테고리별 지출내역을 도넛 차트로 조회가 가능하다.

- **모임 계좌 지출**:
  - 모임 계좌의 돈 지출이 가능하다.

<br/>

# :star: 기술 스택

| 기술        | 버전 / 이미지   |
|-------------|----------------|
| Java        | 17             |
| Vue.js      | 3.5.13         |
| SpringBoot  | 4.2.x          |
| Git         | <img src="https://github.com/user-attachments/assets/483abc38-ed4d-487c-b43a-3963b33430e6" alt="git" width="100"> |
| Notion      | <img src="https://github.com/user-attachments/assets/34141eb9-deca-416a-a83f-ff9543cc2f9a" alt="Notion" width="100"> |
<br/>

# :raised_hands: 브랜치 전략 (Branch Strategy)
우리는 다음과 같은 브랜치를 사용했습니다.

- main Branch
  - 배포 가능한 상태의 코드를 유지합니다.
  - 모든 배포는 이 브랜치에서 이루어집니다.
  
- {dev_name} Branch
  - 팀원 각자의 개발 브랜치입니다.
  - 모든 기능 개발은 이 브랜치에서 이루어집니다.

<br/>

# :muscle: 컨벤션
```
feat : 새로운 기능 추가
fix : 버그 수정
docs : 문서 수정
style : 코드 포맷팅, 세미콜론 누락, 코드 변경이 없는 경우
refactor : 코드 리펙토링
test : 테스트 코드, 리펙토링 테스트 코드 추가
chore : 빌드 업무 수정, 패키지 매니저 수정
```
<br/>
