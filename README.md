<div align="center">
<img width="200" height="200" alt="image" src="https://github.com/user-attachments/assets/0bf28518-9aba-43d8-9b9e-2b491dfbb6d4" />
<h1>토닥</h1>
</div>

## 개요
AI 기반 공감 응답을 통해 사용자의 지속적인 감정 관리를 지원하는 감정 케어 서비스

## 개발 정보
* 2025.03 - 2025.06 (4M / 4명) [BE, AI]

## 기술 스택
<p dir="auto">
<img src="https://img.shields.io/badge/-Java-DB6900?logo=Java&amp;logoColor=white&amp;labelColor=DB6900" style="max-width: 100%;">
<br>
<img src="https://img.shields.io/badge/-Spring-6DB33F?logo=Spring&amp;logoColor=white&amp;labelColor=6DB33F" style="max-width: 100%;">
<img src="https://img.shields.io/badge/-SpringBoot-6DB33F?logo=springboot&amp;logoColor=white&amp;labelColor=6DB33F" style="max-width: 100%;">
<img src="https://img.shields.io/badge/-SpringDataJpa-6DB33F?logo=SpringDataJpa&amp;logoColor=white&amp;labelColor=6DB33F" style="max-width: 100%;">
<img src="https://img.shields.io/badge/-Junit5-25A162?logo=Junit5&amp;logoColor=white&amp;labelColor=25A162" style="max-width: 100%;">
<br>
<img src="https://img.shields.io/badge/-MySql-438CB2?logo=MySql&amp;logoColor=white&amp;labelColor=438CB2" style="max-width: 100%;">
<img src="https://img.shields.io/badge/-Redis-FF4438?logo=Redis&amp;logoColor=white&amp;labelColor=FF4438" style="max-width: 100%;">
<br>
<img src="https://img.shields.io/badge/-RabbitMQ-FF6600?logo=RabbitMQ&amp;logoColor=white&amp;labelColor=FF6600" style="max-width: 100%;">
<img src="https://img.shields.io/badge/-LangChain4j-1C3C3C?logo=LangChain&amp;logoColor=white&amp;labelColor=1C3C3C" style="max-width: 100%;">
<br>
<img src="https://img.shields.io/badge/-Prometheus-E6522C?logo=Prometheus&amp;logoColor=white&amp;labelColor=E6522C" style="max-width: 100%;">
<img src="https://img.shields.io/badge/-Grafana-F46800?logo=Grafana&amp;logoColor=white&amp;labelColor=F46800" style="max-width: 100%;">
<img src="https://img.shields.io/badge/-k6-7D64FF?logo=k6&amp;logoColor=white&amp;labelColor=7D64FF" style="max-width: 100%;">
</p>

## 시스템 아키텍처
<img width="832" height="540" alt="image" src="https://github.com/user-attachments/assets/7bd14135-d23e-42fa-871b-7b07f04f07c7" />

## 담당한 기능
* [실시간 알림 기능 구현](#1-실시간-알림-기능-구현)
* [락 획득 로직 개선 및 트랜잭션 최적화](#2-락-획득-로직-개선-및-트랜잭션-최적화)
* [포인트 로그 조회 속도 개선](#3-포인트-로그-조회-속도-개선)
* 인증, 방명록, AI 댓글 생성 및 자동 작성 기능 개발
* 포인트 로그 관리 Admin Dashboard 개발

## 1. 실시간 알림 기능 구현
### 개요
* 일기 작성, 댓글 작성 시 각각 친구, 일기의 주인에게 실시간 알림 발송
* 실시간 알림 전송 방식과 분산 환경을 고려한 구현 필요

### 문제 및 의사결정 과정
#### 1. Polling vs WebSocket vs SSE
* 서버 &rarr; 클라이언트 단방향 알림 푸시만 필요
* 연결 유지 비용 최소화
* 실시간성이 어느정도는 필요하지만 엄격한 실시간성을 필요로 하진 않음

**Polling**
* 주기적 요청으로 서버 부하 및 실시간성 저하
**WebSocket**
* 양방향 통신 불필요, TCP 연결 유지 오버헤드 발생
**SSE**
* Server-Sent Events
* HTTP 기반 단방향 실시간 푸시 + 자동 재연결 + 간단한 구현

#### 2. Kafka vs Redis Pub/Sub vs RabbitMQ
* 분산 환경에서 각 인스턴스는 자신에게 연결된 클라이언트의 SseEmitter만 메모리에 보관
* 클라이언트 A가 인스턴스 1에 SSE 연결된 상태에서, 인스턴스 2에서 A에게 보낼 알림이 발생하면 전달 실패
* 이를 위해 메시지 큐를 도입하고 모든 분산 서버에 메시지를 전달해야 함

**Kafka**
* 고성능, 대용량 처리에 강하나 알림 시스템에는 과도한 복잡도
**Redis Pub/Sub**
* 구현 간단하나 메시지 영속성 미보장, 구독자 미연결 시 알림 유실
**RabbitMQ**
* 메시지 영속성 보장 + DLQ 기본 제공 + 적절한 운영 복잡도

> **RabbitMQ를 통한 실시간 알림 전달 기능 구현**

#### 3. Direct Exchange vs Fanout Exchange
* 알림 저장은 한 인스턴스에서만 이루어져야 함 (DB 중복 저장 방지)
* 알림 전달 메시지는 모든 인스턴스로 전달되어야 함 (각 인스턴스의 SSE 연결 확인 필요)

**Direct Exchange**
* 알림 객체 DB 저장 (단일 큐, 경쟁 소비자)
**Fanout Exchange**
* 알림 전달 메시지 브로드캐스트 (SpEL 기반 인스턴스별 동적 큐 생성 및 바인딩)
```java
@Configuration
public class RabbitMQConfig {

		...
    
    // Bean으로 큐 이름 저장 (한 번만 생성)
    @Bean
    public String dynamicPublishNotificationQueueName() {
        return PUBLISH_NOTIFICATION_QUEUE + ":" + UUID.randomUUID().toString();
    }
    
    @Bean
    public Queue publishNotificationQueue(
            @Qualifier("dynamicPublishNotificationQueueName") String queueName) {
        //  큐 이름주입받아서 사용
        return new Queue(queueName, false, true, true);
        //               durable=false, exclusive=true, autoDelete=true
    }
    
    @Bean
    public FanoutExchange publishNotificationExchange() {
        return new FanoutExchange(PUBLISH_NOTIFICATION_EXCHANGE);
    }
    
    @Bean
    public Binding publishNotificationBinding(
            Queue publishNotificationQueue,
            FanoutExchange publishNotificationExchange) {
        return BindingBuilder
            .bind(publishNotificationQueue)
            .to(publishNotificationExchange);
    }
}

@Component
public class NotificationSubscriber {
    
    @RabbitListener(queues = "#{@dynamicPublishNotificationQueueName}")
    public void consumePublishNotificationMessage(Notification notification) {
        notificationService.publishNotification(notification);
    }
}
```

> **단일 큐 Direct Exchange로 알림 저장, 동적 큐 Fanout Exchange로 전체 인스턴스에 알림 이벤트 전달**

### 성과
* 다중 인스턴스 환경(3대)에서 알림 누락률 약 66% > 0%로 개선하여 100% 전달 보장
* DLQ + Slack 연동으로 실패 메시지 즉각 모니터링 체계 구축
* 인스턴스별 전용 큐 설계로 수평 확장 시 알림 전송 안정성 확보

## 2. 락 획득 로직 개선 및 트랜잭션 최적화
### 개요
* 서비스 레이어에서 일기, 댓글 작성 로직 실행 시 분산 락 획득, 포인트 획득, 알림 전송 등 여러 로직이 실행됨

### 문제 및 의사결정 과정
* 서비스 레이어에서 트랜잭션 관리, 분산 락, 알림 전송 등 기술적 처리와 비즈니스 로직이 혼재되어 코드가 복잡해지고 역할이 불분명해짐
* 부가 기능(포인트 획득, 알림 전송) 실패가 핵심 기능(일기 작성, 댓글 작성)의 트랜잭션까지 롤백시키는 등 기능 간 결합도가 높아 시스템 안정성과 확장성에 한계가 있음을 발견
* 분산 락 획득 시 락 대기 구간에서 불필요하게 DB 커넥션이 점유되는 현상 발생

#### 1. Facade 계층 도입
* SRP를 실현하기 위해 Facade 계층을 도입해 기술적 처리와 비즈니스 로직을 분리
```java
@RequiredArgsConstructor
@Component
public class LockExecutor {

    public static final int DELAY = 100;
    public static final int MULTIPLIER = 2;

    private final LockWithMemberFactory lockWithMemberFactory;

    @Retryable(
            value = {
                    TodakException.class,
                    DeadlockLoserDataAccessException.class
            },
            backoff = @Backoff(delay = DELAY, multiplier = MULTIPLIER, random = true)
    )
    public void executeWithLock(String lockPrefix, Member member, Runnable runnable) {
        String lockKey = lockPrefix + member.getId();
        Lock lock = null;
        try {
            lock = lockWithMemberFactory.tryLock(member, lockKey, 10, 2);
            runnable.run();
        } finally {
            if (lock != null) {
                lockWithMemberFactory.unlock(member, lock);
            }
        }
    }
}
```
* 분산 락 획득과 트랜잭션 시작 시점을 분리하여 락 대기 중에는 DB 커넥션이 점유되지 않도록 설계
* Spring ApplicationEvent를 활용해 부가 기능을 비동기 이벤트 기반으로 전환, 핵심 트랜잭션과 느슨하게 연결하여 결합도와 오류 전파 위험을 낮춤
```java
@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean(name = "steadyExecutor")
    public Executor steadyExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(50);      // 높은 기본 스레드 수로 일정한 처리량 유지
        executor.setMaxPoolSize(60);       // 약간의 여유만 두기
        executor.setQueueCapacity(500);    // 큰 대기열로 일시적 부하 흡수
        executor.setThreadNamePrefix("Steady-");
        executor.setKeepAliveSeconds(60);  // 추가 스레드의 유휴 시간 제한
        executor.initialize();
        return executor;
    }

}
```
* 일정하게 발생하는 트래픽에 대응하기 위한 커스텀 스레드 설정 구현

### 성과
* 평균 응답시간을 9.7s에서 333ms로 29배 단축하고 TPS를 6배 증가시켜 시스템 성능과 사용자 경험을 획기적으로 개선
* 응답 속도 개선으로 일기, 댓글 작성 시 사용자 대기 시간이 크게 감소하여 서비스 이용 만족도가 향상

## 3. 포인트 로그 조회 속도 개선
### 개요
* 포인트 로그 조회 페이지에서 user_id, point_type, point_status를 기준으로 조회가 가능함

### 문제 및 의사결정 과정
* MySQL 쿼리플랜 분석 결과, Admin Dashboard에서 포인트 로그를 복합 조건으로 대량 조회할 때 대용량 데이터에 적합한 인덱스가 없어 Full Table Scan이 발생함
* 이로 인해 쿼리 타임아웃과 화면 응답 지연이 빈번하게 나타남
* 복합 인덱스 도입으로 해결
<div display="flex">
	<img width="404" height="502" alt="image" src="https://github.com/user-attachments/assets/5029cb9a-fc9e-4925-8ce4-fa4b79d5231d" />
	<img width="404" height="502" alt="image" src="https://github.com/user-attachments/assets/57642347-1cf2-4922-8d4e-93abb1683583" />
</div>
* 대표 패턴이 'userId + 포인트 유형/상태'로 카디널리티를 고려하여 (user_id, point_type, point_status : 카디널리티 내림차순) 복합 인덱스를 설계

### 성과
* 포인트 로그 1,000,000건에 대해 조회 시간을 220ms &rarr; 24ms로 9배 개선
