# Point Management Service

## 소개
이 프로젝트는 사용자 포인트 시스템을 관리하는 백엔드 서비스입니다. 서비스는 다수의 클라이언트가 동시에 포인트를 충전하거나 사용할 수 있도록 설계되었습니다. 동시성 제어는 데이터 일관성을 보장하기 위해 필수적입니다.

## 동시성 문제 개요
다수의 클라이언트가 동시에 포인트를 충전하거나 사용할 때, 데이터가 일관되지 않을 수 있는 문제가 발생할 수 있습니다. 예를 들어, 두 개의 요청이 거의 동시에 도착하여 포인트를 업데이트하려고 할 때, 이전 상태를 참조하여 잘못된 결과를 저장할 수 있습니다. 이를 방지하기 위해 동시성 제어가 필요합니다.

## 동시성 제어 방식

### synchronized 블록
`synchronized` 키워드는 메서드나 특정 코드 블록에 대한 동시 접근을 제어합니다. 자바의 전통적인 동시성 제어 방법으로, 간단한 사용 사례에 적합합니다.

### ReentrantLock
`ReentrantLock`은 `synchronized`보다 더 유연한 동시성 제어 메커니즘을 제공합니다. 락을 명시적으로 잠그고 해제할 수 있어 더 정교한 제어가 가능합니다. 또한 타임아웃, 공정성 등의 옵션을 설정할 수 있습니다.

### Kotlin의 코루틴과 Mutex
코틀린에서는 코루틴을 사용하여 비동기 프로그래밍을 쉽게 할 수 있습니다. `Mutex`는 코루틴 내에서 동시성을 제어하는 데 사용되며, 자바의 락과 유사하게 작동합니다.

## 선택한 방법과 그 이유
이 프로젝트에서는 `ReentrantLock`을 사용하여 동시성 문제를 해결했습니다. `ReentrantLock`은 동시성 문제를 정교하게 제어할 수 있으며, 스레드 간의 공정성을 보장할 수 있습니다. 또한, 복잡한 시나리오에서 `synchronized` 블록보다 더 나은 성능과 유연성을 제공합니다.

## 구현 내용
`PointService` 클래스에서 포인트 충전 및 사용 메서드에 `ReentrantLock`을 적용했습니다. 이를 통해 각 메서드가 동시에 여러 스레드에 의해 호출될 때에도 일관된 데이터가 유지될 수 있도록 했습니다.

```kotlin
private val lock = ReentrantLock()

fun charge(id: Long, amount: Long): UserPoint {
    lock.lock()
    try {
        // 포인트 충전 로직
    } finally {
        lock.unlock()
    }
}

fun use(id: Long, amount: Long): UserPoint {
    lock.lock()
    try {
        // 포인트 사용 로직
    } finally {
        lock.unlock()
    }
}
```

## 테스트 전략
동시성 제어가 올바르게 동작하는지 확인하기 위해, 여러 개의 스레드를 사용해 동시에 포인트를 충전하거나 사용하는 통합 테스트를 작성했습니다. 각 테스트는 다수의 요청이 동시에 처리되는 상황에서 데이터의 일관성을 검증합니다.

```kotlin
@Test
fun `동시 충전 테스트`() {
    // 10개의 스레드가 동시에 10포인트씩 충전하도록 테스트
    val executor = Executors.newFixedThreadPool(10)
    val initialPoints = pointService.getUserPoint(userId).point

    repeat(10) {
        executor.submit {
            pointService.charge(userId, 10)
        }
    }

    executor.shutdown()
    executor.awaitTermination(1, TimeUnit.MINUTES)

    val finalPoints = pointService.getUserPoint(userId).point
    assertEquals(initialPoints + 100, finalPoints)  // 10개의 요청, 각각 10포인트 충전
}

@Test
fun `동시 사용 테스트`() {
    // 10개의 스레드가 동시에 10포인트씩 사용하도록 테스트
    val executor = Executors.newFixedThreadPool(10)
    val initialPoints = pointService.getUserPoint(userId).point

    repeat(10) {
        executor.submit {
            pointService.use(userId, 10)
        }
    }

    executor.shutdown()
    executor.awaitTermination(1, TimeUnit.MINUTES)

    val finalPoints = pointService.getUserPoint(userId).point
    assertEquals(initialPoints - 100, finalPoints)  // 10개의 요청, 각각 10포인트 사용
}
```
