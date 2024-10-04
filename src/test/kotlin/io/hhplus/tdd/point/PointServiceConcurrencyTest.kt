package io.hhplus.tdd.point

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SpringBootTest
class PointServiceConcurrencyTest {

    @Autowired
    private lateinit var pointService: PointService

    private val userId = 1L
    private val threadCount = 10 // 스레드 개수를 변수로 선언
    private val amountPerThread = 10L // 각 스레드마다 충전/사용할 금액을 변수로 선언

    // 각 테스트 전에 초기 포인트를 설정하는 메소드
    @BeforeEach
    fun setUp() {
        // 초기 포인트를 100으로 설정하여 테스트가 일관된 초기 상태에서 시작되도록 함
        pointService.charge(userId, 100L)  // 예: 100포인트로 설정
    }

    @Test
    fun `동시 충전 테스트`() {
        // ThreadPool을 생성
        val executor = Executors.newFixedThreadPool(threadCount)

        // 초기 포인트를 가져옴
        val initialPoints = pointService.getUserPoint(userId).point

        // 스레드마다 설정된 금액을 동시에 충전
        repeat(threadCount) {
            executor.submit {
                pointService.charge(userId, amountPerThread)
            }
        }

        // 모든 스레드의 작업이 완료될 때까지 기다림
        executor.shutdown()
        executor.awaitTermination(1, TimeUnit.MINUTES)

        // 최종 포인트를 가져와서 10번의 충전 후 예상되는 포인트 값과 일치하는지 확인
        val finalPoints = pointService.getUserPoint(userId).point
        // 예상 포인트 값이 정확한지 검증
        assertEquals(initialPoints + (threadCount * amountPerThread), finalPoints)
    }

    @Test
    fun `동시 사용 테스트`() {
        // ThreadPool을 생성
        val executor = Executors.newFixedThreadPool(threadCount)

        // 초기 포인트를 가져옴
        val initialPoints = pointService.getUserPoint(userId).point

        // 스레드마다 설정된 금액을 동시에 사용
        repeat(threadCount) {
            executor.submit {
                pointService.use(userId, amountPerThread)
            }
        }

        // 모든 스레드의 작업이 완료될 때까지 기다림
        executor.shutdown()
        executor.awaitTermination(1, TimeUnit.MINUTES)

        // 최종 포인트를 가져와서 10번의 사용 후 예상되는 포인트 값과 일치하는지 확인
        val finalPoints = pointService.getUserPoint(userId).point
        // 예상 포인트 값이 정확한지 검증
        assertEquals(initialPoints - (threadCount * amountPerThread), finalPoints)
    }
}
