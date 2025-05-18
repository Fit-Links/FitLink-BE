## 9-1. 고정 예약 로직


### 9-1-1. 매일 스케줄러를 실행

처음 고정 예약을 기능 명세로 봤을 때 들었던 생각은, **‘매일 스케줄러가 자정에 돌아서, 당일에 고정 예약으로 잡혀 있는 예약들을 선별하여, 일주일 뒤에 예약을 잡아주면 되겠다.’** 이였습니다.

그래서 다음과 같이 스케줄러를 설정하고,

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class FixedReservationScheduler { //

    private final ReservationFacade reservationFacade;

    /**
     * 매일 정각마다 고정 예약 확인하고, 예약 실행 (일주일 뒤에 고정 예약 함)
     */
    @Scheduled(cron = "0 0 0 * * *") // 매일 00:00:00에 실행
    public void fixedReserveSession() {
        reservationFacade.checkFixedReserveSession();
    }
}
```

기능을 구현하였습니다.

```java
@Component
@RequiredArgsConstructor
public class ReservationFacade {

...

	@Transactional
	public void checkFixedReserveSession() {
	    // 고정 예약 상태의 예약 조회
	    List<Reservation> fixedReservations = reservationService.getFixedReservations();
	
	    // 일주일 뒤에 시간으로 예약 도메인 생성
	    List<Reservation> newReservations = fixedReservations.stream()
	            .map(Reservation::toFixedDomain)
	            .toList();
	    // 일주일 뒤에 시간에 예약이 있다면(예약 대기 포함) 취소 절차 진행
	    newReservations.forEach((r) -> {
	        List<Reservation> getThatTimeReservations = reservationService.getReservationThatTimes(
	                ReservationCommand.GetReservationThatTimes.builder()
	                        .trainerId(r.getTrainer().getTrainerId())
	                        .date(r.getReservationDates())
	                        .build());
	
	        cancelExistingReservations(getThatTimeReservations, "트레이너의 고정 예약으로 인해 예약이 취소되었습니다.");
	    });
	    // 고정 예약 진행
	    reservationService.fixedReserveSessions(newReservations);
	}
	
	private void cancelExistingReservations(List<Reservation> reservations, String cancelMsg) {
	    if (!reservations.isEmpty()) {
	        reservations.forEach(Reservation::checkPossibleReserveStatus);
	        // 만약 예약이 있다면, 그 예약들 모두 강제로 취소
	        reservationService.cancelReservations(reservations, cancelMsg);
	        // 취소했다면, 취소됐다는 알람 전송
	        reservations.forEach(r -> notificationService.sendCancelReservationNotification(r.getReservationId(),
	                memberService.getMemberDetail(r.getMember().getMemberId()), RESERVATION_REFUSE));
	    }
	}
...
}

```

그런데 기능을 구현하고 보니 문제점이 보였습니다.

- 일주일 뒤에 그 시간에 예약이 있다면 그 예약을 취소시켜야할까?
    - 예약의 우선 순위가 고정예약이 가장 높을까?
- 만약 그날에 고정 예약이 2개가 있는데, 스케줄러가 돌다가 에러가 나면 둘 다 실패해버리네?
    - 각각의 고정 예약은 독립적으로 예약이 되어야 하지 않을까?
    - 만약 에러가 발생하면 다시 시도하는 로직이 있어야하지 않을까?

### 9-1-2. SQS로 로직 이관

위와 같은 고민 때문에 다음과 같이 문제점을 해결하기로 하였습니다.

- 일주일 뒤에 그 시간에 예약이 있다면 그 예약을 취소시켜야할까?

→ **확정된 예약이 아닌 대기 중인 예약이 있다면** 거절시키고, 그 나머지 경우에는 고정 예약을 진행시키자!

- 만약 그날에 고정 예약이 2개가 있는데, 스케줄러가 돌다가 에러가 나면 둘 다 실패해버리네?

→ 각각의 고정 예약을 하나의 테스크로 인식하고, **Queue**를 통해서 고정 예약을 진행시키자!

Queue 기반으로 고정예약 작업을 선택한 이유는 다음과 같습니다.

1. **실패 분리**
- 개별 작업으로 분리되기 때문에 **하나 실패해도 전체 작업엔 영향이 없습니다.**
- 실패한 작업만 따로 retry하거나 alert 줄 수 있습니다.
2. **확장성**
- Queue 기반 구조는 **작업이 많아져도 분산 처리가 가능합니다.**
    - 메시지 브로커 (예: RabbitMQ, SQS, Kafka)
3. **비동기 처리**
- 사용자 입장에서 **즉각적인 반응** 필요 없이 백그라운드에서 진행이 가능합니다.

스프링 내부적으로 Queue구조를 메모리에 등록해서 사용할 수도 있지만, 그럼 또 다음과 같은 문제들이 고려되었습니다.

1.  **서버가 재시작되면 큐가 초기화됩니다.**
- 고정 예약 작업이 queue에 들어있다가, 서버가 다운되면 queue정보가 사라지게 됩니다.
    - 즉, **비지속성(non-durable)** 구조를 가지게 됩니다.
2. **분산 환경에서 동기화가 안 됩니다.**
- 만약 서버 2대 이상인 분산 환경이라면 인스턴스 마다 queue가 따로 있게 되기 때문에 **예약이 중복되거나 누락될 수 있습니다.**
3. **모니터링/재시도/DLQ 등 고급 기능이 없습니다.**
- 재시도 로직, delay, backoff 같은 걸 직접 구현해야 합니다.

따라서 외부 미들웨어로 동작하는 Queue를 고려하게 되었습니다.

- Kafka
- RabbitMQ
- AWS SQS

Kafka는 스택 자체가 무겁고, 빠르게 스트리밍한 데이터를 처리하는 용도가 지금 아니기 때문에 제외하기로 했습니다.

마지막으로 남은 RabbitMQ와 AWS SQS를 비교해보면

| **기준** | **RabbitMQ** | **SQS (Amazon Simple Queue Service)** |
| --- | --- | --- |
| 운영 방식 | 직접 설치 / 관리 필요 (EC2나 ECS 위에 올려야 함) | Fully managed, AWS에서 다 해줌 |
| 복잡도 | 더 유연하지만 설정과 운영 부담 있음 | 간단하게 시작 가능, 운영 거의 없음 |
| 기능 | 고급 라우팅 (topic, fanout 등), retry 설정도 큼 | 기본적인 FIFO, DLQ(Dead Letter Queue)만 있음 |
| Retry | Consumer에서 직접 재시도 구현해야 함 | DLQ + Visibility Timeout으로 재시도 유도 가능 |
| Outbox 패턴 연계 | 직접 연동 | AWS DynamoDB Streams, EventBridge랑 연계 쉬움 |

지금 상황에서는 **정말 간단한 Queue기능** 역할만 해주는 스택만 있으면 됩니다.

따라서 우리 서버는 AWS 위에 올라가서 운영이 되기 때문에 좀 더 AWS에 친화적이면서 운영이 쉬운 SQS 최종적으로 사용하기로 결정했습니다.

추가적으로 queue 메세지 발행이 실패하는 것을 방지하기 위해 **Transactional Outbox Pattern**를 적용하였습니다.

### **Transactional Outbox Pattern?**

- 도메인 로직이 성공적으로 수행되었다면, 이에 해당하는 이벤트 메세지를 `Outbox Table` 이라는 별도의 테이블에 저장하여 함께 Commit 합니다.
- 동일한 트랜잭션 내에서 이벤트 발행을 위한 Outbox 데이터 적재까지 진행해 이벤트 발행을 보장합니다.
- 이벤트 발행 상태 또한 Outbox 데이터에 존재하므로, 배치 프로세스 등을 이용해 미발행된 데이터에 대한 Fallback 처리가 용이합니다.

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationScheduler { //

    private final ReservationFacade reservationFacade;

    /**
     * 매일 정각마다 고정 예약 확인하고, 예약 실행 (일주일 뒤에 고정 예약 함)
     */
    @Scheduled(cron = "0 0 0 * * *") // 매일 00:00:00에 실행
    public void createFixedReservations() {
        reservationFacade.checkCreateFixedReservation();
    }
```

```java
@Component
@RequiredArgsConstructor
public class ReservationFacade {

...

	@Transactional
	public void checkCreateFixedReservation() {
	    // 고정 예약 상태의 예약 조회
	    reservationService.publishFixedReservations();
	}
    
...    
}
```

```java

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReservationService {

private final ApplicationEventPublisher publisher;

...

   public void publishFixedReservations() {
        // 고정 예약 상태의 예약 조회
        List<Reservation> fixedReservations = reservationRepository.getFixedReservations();
        // 오늘 날짜 고정 예약들 필터하기
        List<Reservation> todayFixedReservations = fixedReservations.stream()
                .filter(Reservation::isTodayReservation)
                .toList();
        // 고정 예약건 개별 이벤트 발행
        todayFixedReservations.forEach(reservation ->
                publisher.publishEvent(GenerateFixedReservationEvent.builder()
                        .reservationId(reservation.getReservationId())
                        .trainerId(reservation.getTrainer().getTrainerId())
                        .memberId(reservation.getMember().getMemberId())
                        .sessionInfoId(reservation.getSessionInfo().getSessionInfoId())
                        .name(reservation.getName())
                        .confirmDate(reservation.getConfirmDate())
                        .topic(eventTopic.getReservationQueue())
                        .messageId(UUID.randomUUID().toString())
                        .build()));
    }

...    
}
```

```java
@Component
@RequiredArgsConstructor
public class OutboxEventListener {

    private final OutboxService outboxService;
    private final EventProducer eventProducer;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void saveOutbox(OutboxEvent event) {
        // Outbox data 생성
        outboxService.createOutbox(event.toOutboxCommand());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishOutbox(OutboxEvent event) {
        // outbox 메시지 발행 완료 채크
        outboxService.publishOutbox(event.getMessageId());
        // 이벤트 메시지 발행
        eventProducer.publish(event.getTopic(), event.getKey(), event.toPayload());
    }
}
```

```java
@Component
@RequiredArgsConstructor
public class EventProducerImpl implements EventProducer {

    private final SqsProducer sqsProducer;

    @Override
    public void publish(String topic, String key, String payload) {
        sqsProducer.publish(topic, payload);
    }
}
```

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class SqsProducer {

    private final SqsTemplate sqsTemplate;

    public void publish(String topic, String payload) {
        log.info("[SQS] :: PUBLISH :: sending to queue={}, payload={}", topic, payload);

        try {
            sqsTemplate.send(to -> to.queue(topic).payload(payload));
            log.info("[SQS] :: SUCCESS :: queue={}, payload={}", topic, payload);
        } catch (Exception ex) {
            log.error("[SQS] :: FAILED :: queue={}, payload={}, error={}", topic, payload, ex.getMessage(), ex);
        }
    }
}
```

이렇게 구성하였고, 스케줄러로 만약 실패한 메세지가 있다면 주기적으로 확인하여 retry 해주게 설정해주었습니다.

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxScheduler {

    private final OutboxService outboxService;

    @Scheduled(fixedDelay = 5 * 60 * 1000) // 5분 마다 실행
    public void retryFailEvent() {
        outboxService.retryFailMessage();
    }
}
```

테스트까지 끝내고 정말 마지막이라고 생각이 들었습니다.

### 9-1-3. 고정 예약 로직 변경

그런데 위와 같이 로직을 구성하였을 때 다음과 같은 문제점이 발견되었습니다.

- 지금 로직은 그 날에 고정 예약들을 모아서 일주일 뒤에 같은 시간대 예약을 진행하는데, 만약 일주일 뒤에 다른 확정된 예약이 있다면?

→ 그 다음 고정 예약을 하지 못 했기 때문에 **그 고정 예약은 다음 스케줄을 계속 예약하지 못하고 버려진다.**

이렇게 생각하니 지금의 고정 예약 로직이 무의미하다는 느낌을 받았습니다.

그래서 심플하게 정책을 다시 정하기로 결정했습니다.

**‘고정 예약 시간을 정하면, 그 회원의 세션이 모두 소진 될 때까지 미리 예약을 다 해버리자!’**

이렇게 하면 가장 큰 문제가 해결이 되었습니다.

만약 중간에 회원이 스케줄을 변경해야 하는 상황이 생기면 해당 날짜의 고정 예약을 다른 날짜로 변경 요청을 하면 되기 때문입니다.

팀원들과 다시 상의를 하였고, 이 정책을 최종적으로 고정 예약이 따르기로 하였습니다.

```java
@Component
@RequiredArgsConstructor
public class ReservationFacade {

...

	@Transactional
	public List<Reservation> createFixedReservation(ReservationCriteria.CreateFixed criteria, SecurityUser user) {
    // 세션이 충분한지 확인
    memberService.isSessionCountEnough(user.getTrainerId(), criteria.memberId());
    // 기존에 확정된 예약이 있는지 확인
    reservationService.checkConfirmedReservationsExistOrThrow(user.getTrainerId(), criteria.reservationDates());
    // 대기중인 예약이 있으면 거절
    List<Reservation> refusedReservations = reservationService.refuseWaitingReservations(criteria.reservationDates());
    // 거절을 했다면 -> 멤버에게 예약이 거절되었다는 알림 전송
    refuseReservations(refusedReservations);
    // 고정 예약 진행
    List<Reservation> reservationDomains = criteria.toDomain(memberService.getSessionInfo(user.getTrainerId(),
            criteria.memberId()), user);
    SessionInfo sessionInfo = memberService.getSessionInfo(user.getTrainerId(), criteria.memberId());
    // 세션 차감
    memberService.deductSession(user.getTrainerId(), criteria.memberId(), sessionInfo.getRemainingCount());
    List<Reservation> fixedReservations = reservationService.createFixedReservations(reservationDomains,
            sessionInfo.getRemainingCount());
    // 트레이너 -> 멤버에게 예약 됐다는 알림 전송
    reservationDomains.forEach(r -> {
        PersonalDetail memberDetail = memberService.getMemberDetail(r.getMember().getMemberId());
        Token token = authService.getTokenByPersonalDetailId(memberDetail.getPersonalDetailId());
        notificationService.sendNotification(NotificationCommand.ApproveReservation.of(memberDetail,
                r.getReservationId(), r.getConfirmDate(), r.getTrainer().getTrainerId(), true,
                token.getPushToken()));
    });
    return fixedReservations;
	}
	...
}
```

이 과정을 지나면서, **처음 설계가 얼마나 중요한지를 다시 한번 깨닫는 계기가 되었습니다.**
<br />
<br />

## 9.2. 알림 발송 로직
<br />


### 9-2-1. 기존 알림 발송 로직

알림 발송은 서비스를 하는데 있어서 중요한 역할을 합니다. 실시간으로 예약에 대한 정보나, 어떤 중요한 정보를 전달하기 위해서 필요합니다.

그래서 예약을 하거나 어떤 중요한 정보를 수정할 때마다 마지막에 알림을 전송하는 로직을 작성하였습니다.

```java

@Component
@RequiredArgsConstructor
public class MemberFacade {

...

	@Transactional
	public void connectTrainer(Long memberId, String trainerCode) {
	    memberService.checkMemberAlreadyConnected(memberId);
	
	    Trainer trainer = trainerService.getTrainerByCode(trainerCode);
	    Member member = memberService.getMember(memberId);
	
	    ConnectingInfo connectingInfo = memberService.requestConnectTrainer(trainer, member);
	    PersonalDetail trainerDetail = trainerService.getTrainerDetail(trainer.getTrainerId());
	    Token token = authService.getTokenByPersonalDetailId(trainerDetail.getPersonalDetailId());
	
	    notificationService.sendConnectRequestNotification(NotificationCommand.Connect.of(trainerDetail, member.getMemberId(),
	            member.getName(), connectingInfo.getConnectingInfoId(), token.getPushToken()));
	}
...

}
    
...

@Component
@RequiredArgsConstructor
public class ReservationFacade {
  ...
  
	@Transactional
	public Reservation changeRequestReservation(ReservationCriteria.ChangeReqeust criteria) {
	
	    // 알림 전송 멤버 -> 트레이너에게 예약 변경 요청했다는 알림 발송
	    Reservation reservation = reservationService.changeRequestReservation(criteria.toCommand());
	    PersonalDetail trainerDetail = trainerService.getTrainerDetail(reservation.getTrainer().getTrainerId());
	    Token token = authService.getTokenByPersonalDetailId(trainerDetail.getPersonalDetailId());
	
	    notificationService.sendChangeRequestReservationNotification(NotificationCommand.ChangeRequestReservation.of(trainerDetail,
	            reservation.getReservationId(), reservation.getMember().getMemberId(),
	            reservation.getName(), criteria.reservationDate(), criteria.changeRequestDate(),
	            token.getPushToken()));
	
	
	    return reservation;
	}
}

...
    
```

그런데 알림을 보내는 메서드를 작성하면서 알림 종류 마다 서비스 로직이 하나씩 늘어나기 시작했습니다.

```java
@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final PushNotificationClient pushNotificationClient;

    public void sendConnectRequestNotification(PersonalDetail trainerDetail, String memberName, Long connectingInfoId) {
        Notification notification = connectRequestNotification(trainerDetail, memberName, connectingInfoId);
        notificationRepository.save(notification);
        
        pushNotificationClient.pushNotification(token, title, content);
    }

    public void sendDisconnectNotification(String name, PersonalDetail trainerDetail) {
        Notification notification = disconnectNotification(name, trainerDetail);
        notificationRepository.save(notification);
        
        pushNotificationClient.pushNotification(token, title, content);
    }

    public void sendCancelReservationNotification(Long reservationId, PersonalDetail memberDetail, Reason reason) {
        Notification notification = cancelReservationNotification(reservationId, memberDetail, reason);
        notificationRepository.save(notification);
        
        pushNotificationClient.pushNotification(token, title, content);
    }

    public void sendCancelRequestReservationNotification(Long reservationId, String name,
                                                         LocalDateTime cancelDate, String cancelReason,
                                                         PersonalDetail trainerDetail,
                                                         Reason reason) {
        Notification notification = cancelRequestReservationNotification(reservationId, name,
                cancelDate, cancelReason, trainerDetail, reason);
        notificationRepository.save(notification);
        
        pushNotificationClient.pushNotification(token, title, content);
    }

    public void sendApproveReservationNotification(Long reservationId, PersonalDetail memberDetail) {
        Notification notification = approveReservationNotification(reservationId, memberDetail);
        notificationRepository.save(notification);
    }

    public void sendApproveRequestReservationNotification(Long reservationId, PersonalDetail memberDetail,
                                                          boolean isApprove) {
        Notification notification = approveRequestReservationNotification(reservationId, memberDetail, isApprove);
        notificationRepository.save(notification);
        
        pushNotificationClient.pushNotification(token, title, content);
    }

    public void sendChangeRequestReservationNotification(Long reservationId, String name, LocalDateTime reservationDate,
                                                         LocalDateTime changeDate,
                                                         PersonalDetail memberDetail) {
        Notification notification = changeRequestReservationNotification(reservationId, name,
                reservationDate, changeDate, memberDetail);
        notificationRepository.save(notification);
        
        pushNotificationClient.pushNotification(token, title, content);
    }
    
    ...
    
}
```

이런 형태라면, 알림 종류가 늘어날수록, 알림을 보내는 메서드도 똑같이 늘어나야 하는 상황입니다.

즉, 가장 보호를 받아야 할 비즈니스 계층에서 **OCP가 지켜지지 않고 있었던 것입니다.**
<br />
<br />

### 9-2-2. 전략 패턴 도입

### 설계 목적

- 다양한 종류의 알림을 **유지보수하기 쉬운 구조로 통합.**
- 각 알림의 **입력 파라미터와 생성 로직을 분리**하여 **유연성과 타입 안정성 확보**.

### 전략 패턴 적용

- NotificationType에 따른 전략을 `Map<NotificationType, Function<DTO, Notification>>` 으로 등록
- 전략 클래스 10개, 20개씩 만드는 오버엔지니어링 방지
- 각 알림은 전용 DTO를 가짐 → `Map<String, Object>` 또는 다형성 없이도 자동완성/컴파일 체크 가능
- 실수로 잘못된 타입 전달 시 컴파일 단계에서 오류 발생 → 유지보수성 ↑

여기에 더해 전략 패턴을 적용하면

- 새로운 알림 타입 추가 시 → DTO 추가 + 전략 등록만 하면 됨
- 기존 로직은 수정할 필요 없음

이기 때문에 OCP를 충족시킬 수 있습니다.

적용 흐름을 보면

1. 처음에 알림 요청 DTO(**전략**)를 만들어 줍니다.

```java
public interface NotificationRequest {
    Notification.NotificationType getType();

    String getPushToken();
}

@Builder
public record Connect(
        PersonalDetail trainerDetail, Long memberId, String memberName,
        Long connectingInfoId, String pushToken
) implements NotificationRequest {
    @Override
    public Notification.NotificationType getType() {
        return Notification.NotificationType.CONNECT;
    }

    @Override
    public String getPushToken() {
        return this.pushToken;
    }

    public static Connect of(PersonalDetail trainerDetail, Long memberId, String memberName,
                             Long connectingInfoId, String pushToken) {
        return Connect.builder()
                .trainerDetail(trainerDetail)
                .memberId(memberId)
                .memberName(memberName)
                .connectingInfoId(connectingInfoId)
                .pushToken(pushToken)
                .build();
    }
}
```

2. 전략을 등록 및 처리를 합니다.

```java
@Component
public class NotificationStrategyHandler {

  private final Map<Notification.NotificationType, Function<NotificationRequest, Notification>> strategyMap =
          new EnumMap<>(Notification.NotificationType.class);

  @PostConstruct
  public void init() {
      strategyMap.put(Notification.NotificationType.CONNECT, this::handleConnectRequest);
      ...

  }

  @SuppressWarnings("unchecked")
  public <T extends NotificationRequest> Notification handle(T request) {
      Function<T, Notification> strategy = (Function<T, Notification>) strategyMap.get(request.getType());

      if (strategy == null) {
          throw new CustomException(ErrorCode.NOTIFICATION_STRANGE_TYPE);
      }

      return strategy.apply(request);
  }

  private Notification handleConnectRequest(NotificationRequest request) {
      NotificationCommand.Connect dto = (NotificationCommand.Connect) request;
      return Notification.connectRequest(dto.trainerDetail(), dto.memberId(), dto.memberName(),
              dto.connectingInfoId());
  }

  ...
}
```

3. 등록한 전략을 이용하여 서비스를 이용하기만 하면 됩니다.

```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationService {
  private final NotificationStrategyHandler strategyHandler;
  ...

  @Transactional
  public <T extends NotificationRequest> void sendNotification(T request) {
      // 1. DB 저장
      Notification notification = strategyHandler.handle(request);
      notificationRepository.save(notification);
      // 2. push 알림 전송 이벤트로 전달
      pushNotificationClient.pushNotification(token, title, content);
  }
}
```

정석으로는 전략마다 컴포넌트를 만들어야 하지만, DTO로 그 부분을 대체했습니다.

이렇게 로직을 수정하고 나니 알림이 추가되더라도, DTO를 만들고 전략을 추가만 하면 됩니다.

즉, NotificationService에 대해서는 **OCP가 지켜지는 것을 볼 수 있습니다.**