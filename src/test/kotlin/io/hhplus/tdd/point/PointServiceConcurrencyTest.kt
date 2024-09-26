package io.hhplus.tdd.point

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.beans.factory.annotation.Autowired
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SpringBootTest
class PointServiceConcurrencyTest {

    @Autowired
    private lateinit var pointService: PointService

    private val userId = 1L

    // 각 테스트 전에 초기 포인트를 설정하는 메소드
    @BeforeEach
    fun setUp() {
        // 초기 포인트를 100으로 설정하여 테스트가 일관된 초기 상태에서 시작되도록 함
        pointService.charge(userId, 100L)  // 예: 100포인트로 설정
    }

    @Test
    fun `동시 충전 테스트`() {
        // 10개의 스레드를 생성하여 동시에 충전 요청을 보내기 위해 ThreadPool을 생성
        val executor = Executors.newFixedThreadPool(10)

        // 초기 포인트를 가져옴
        val initialPoints = pointService.getUserPoint(1L).point

        // 10개의 스레드가 동시에 10포인트씩 충전
        repeat(10) {
            executor.submit {
                pointService.charge(1L, 10)
            }
        }

        // 모든 스레드의 작업이 완료될 때까지 기다림
        executor.shutdown()
        executor.awaitTermination(1, TimeUnit.MINUTES)

        // 최종 포인트를 가져와서 10번의 충전 후 예상되는 포인트 값과 일치하는지 확인
        val finalPoints = pointService.getUserPoint(1L).point
        // 10개의 요청이 모두 완료된 후의 최종 포인트 값이 올바른지 확인
        assertEquals(initialPoints + 100, finalPoints)  // 10개의 요청, 각각 10포인트 충전
    }

    @Test
    fun `동시 사용 테스트`() {
        // 10개의 스레드를 생성하여 동시에 사용 요청을 보내기 위해 ThreadPool을 생성
        val executor = Executors.newFixedThreadPool(10)

        // 초기 포인트를 가져옴
        val initialPoints = pointService.getUserPoint(1L).point

        // 10개의 스레드가 동시에 10포인트씩 사용
        repeat(10) {
            executor.submit {
                pointService.use(1L, 10)
            }
        }

        // 모든 스레드의 작업이 완료될 때까지 기다림
        executor.shutdown()
        executor.awaitTermination(1, TimeUnit.MINUTES)

        // 최종 포인트를 가져와서 10번의 사용 후 예상되는 포인트 값과 일치하는지 확인
        val finalPoints = pointService.getUserPoint(1L).point
        // 10개의 요청이 모두 완료된 후의 최종 포인트 값이 올바른지 확인
        assertEquals(initialPoints - 100, finalPoints)  // 10개의 요청, 각각 10포인트 사용
    }
}
