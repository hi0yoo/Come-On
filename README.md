# Come-On Back End API Project

## 프로젝트 개요
일정 기반 모임 관리 서비스 구축

**핵심 기능1. 모임 관리 기능**
- 회원은 모임을 생성하여 모임에 참여할 회원을 초대
- 모임에 가입된 회원들은 자신이 참여 가능한 날짜 선택
- 모임 호스트가 회원들의 참여 가능한 날짜를 수치로 확인하여 모임 날짜 확정
- 모임 호스트와 권한이 있는 회원은 모임에 방문할 장소를 선택하여 모임 코스를 작성
- 모임이 끝난 후 모임 호스트는 모임 코스를 다른 사용자들과 공유 가능

**핵심 기능2. 코스 추천 기능**
- 사용자의 위치를 기반으로 하여 주변에 모임을 가지기 좋은 코스를 추천
- 사용자는 마음에 드는 코스를 통해 모임 생성 가능

## 백엔드 팀의 공통 목표
- 단순 기능 구현이 아닌 성능, 재사용성, 유지보수성을 고려하여 구현
- 객체지향 설계 원칙을 준수하고 핵심 로직과 공통 관심사의 분리를 통해 유연하고 확장성 있는 설계를 지향
- 모든 로직은 테스트 케이스 작성
- 평일 오전 10시 스크럼 및 1주 단위의 스프린트를 통해 일정 관리, 모든 이슈 함께 논의하여 해결
- Pull Request를 통해 코드 리뷰

## 사용 기술
- Java11
- Spring Framework 5.3.2, Spring Boot 2.7.2
- JPA, Spring Data JPA, Querydsl
- MySQL
- Redis
- Spring Security, JWT
- Spring Cloud
- Micrometer, Prometheus, Grafana
- AWS EC2, RDS, S3, Route53, ACM, ELB
- Docker
- Spring REST Docs

## 서버 아키텍처
애플리케이션을 MicroService Architecture로 구성하였고, AWS인프라를 사용하여 클라우드 환경에서 서비스를 제공하도록 설계했습니다.

![애플리케이션 구조 drawio (8)](https://user-images.githubusercontent.com/97069541/192691421-cfe92249-ba67-4762-8a01-f5dd6f787b31.png)

- ALB: 서버 도메인과 API 서버를 라우팅, 부하 분산을 수행합니다. SSL/TLS 보안을 적용시켜 HTTPS 프로토콜로 요청을 보낼 수 있도록 설정했습니다.

- API Gateway: 인증 및 권한 부여, 서비스 라우팅, 부하 분산, 로깅의 역할을 담당합니다.

- Discovery Service: 분산되어 있는 서비스들의 정보를 등록합니다.

- Config Server: 각 서버의 설정 정보를 Remote Git Repository에서 관리하며, Cloud Bus를 이용해 메시지 큐로 RabbitMQ를 연동하여 MS로 갱신된 설정정보를 전달합니다.

- Microservices: Come On 서비스의 비즈니스 로직을 수행하는 애플리케이션입니다.
    - Auth Service: 회원 로그인, 로그아웃 등과 같은 인증 기능 전반을 담당합니다.
    - User Service: 회원 도메인의 전반적인 기능을 담당합니다.
    - Meeting Service: 모임 도메인의 전반적인 기능을 담당합니다.
    - Course Service: 코스 도메인의 전반적인 기능을 담당합니다.

- Redis: Access Token의 Black List를 관리를 담당하는 캐시 데이터베이스입니다.

- AWS S3: Come On 서비스의 파일(이미지)을 저장하는 스토리지입니다.

- AWS RDS(MySQL): Come On 서비스의 데이터를 저장하는 관계형 데이터베이스입니다.

- Prometheus: API Gateway의 Metrics 정보를 저장하는 시계열 DB 서버입니다.

- Grafana: Prometheus 서버로부터 Metics 정보를 가져와 시각화하여 대시보드로 표시해주는 Grafana 서버입니다.

## 애플리케이션 구조
```
+---common: 애플리케이션 전반에서 사용되는 경우   
+---domain: 핵심 비즈니스 로직에 사용되는 경우
|   +---config
|   +---common
|   +---domain name
|   |   +---dto
|   |   +---entity
|   |   +---repository
|   |   +---service
+---web: 웹 로직과 관련하여 사용되는 경우
    +---common
    +---config
    +---domain name
		|   +---controller
    |   +---query
    |   +---request
    |   +---response
```

![image](https://user-images.githubusercontent.com/97069541/192133106-b8e07404-4978-4cec-b521-3c6c6436d90f.png)

- Domain 계층은 Web 계층을 의존하지 않도록 설계 (단방향 연관, 의존관계)
    - 웹 로직의 변경이 비즈니스 로직에 영향을 미치지 않음
    - 패키지 사이의 의존성 사이클을 제거하여 변경 시 파급 효과가 연쇄적으로 일어나는 것을 방지함

- 커맨드와 쿼리 분리
    - 메서드는 되도록 상태를 변경하는 Command, 데이터를 반환하는 Query 중 한가지 기능을 실행하도록 설계
    - 클래스 단위로 Command를 수행하는 서비스를 Service로, Query를 수행하는 서비스를 QueryService로 분리
    - 해당 클래스의 메서드를 호출했을 때 Side Effect가 일어나는 메서드인지 아닌지 명확하게 분리가 가능하기 때문
    - Query는 상태를 바꾸지 않아 안전하게 원하는 대로 사용 가능하도록 설계
    - OSIV옵션을 끈 다음 모든 지연로딩은 QueryService에서 진행되도록 최적화
    - 실제 예상하지 못한 데이터 변경이 일어난 경우에는 Command 로직만 확인하여 수정 가능

## ERD

![Comeon ERD (3)](https://user-images.githubusercontent.com/97069541/192691362-abdd469f-7142-4d7c-9cbb-b03ecfde8aac.png)


