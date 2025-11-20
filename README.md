<div align="center">
<img width="200" height="200" alt="image" src="https://github.com/user-attachments/assets/0bf28518-9aba-43d8-9b9e-2b491dfbb6d4" />
<h1>토닥</h1>
</div>

## 개요
AI 기반 공감 응답을 통해 사용자의 지속적인 감정 관리를 지원하는 감정 케어 서비스

## 시스템 아키텍처
<img width="832" height="540" alt="image" src="https://github.com/user-attachments/assets/7bd14135-d23e-42fa-871b-7b07f04f07c7" />

## 담당한 기능
* [실시간 알림 기능 구현](#1-실시간-알림-기능-구현)
* [락 획득 로직 개선 및 트랜잭션 최적화](#2-락-획득-로직-개선-및-트랜잭션-최적화)
* [포인트 로그 조회 속도 개선](#3-포인트-로그-조회-속도-개선)
* 인증, 방명록, AI 댓글 생성 및 자동 작성 기능 개발

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
### 문제 및 의사결정 과정
### 성과

## 3. 포인트 로그 조회 속도 개선
### 개요
### 문제 및 의사결정 과정
### 성과
