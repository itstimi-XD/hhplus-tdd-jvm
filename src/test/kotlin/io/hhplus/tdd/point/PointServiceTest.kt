package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension

// 테스트 케이스의 작성 및 작성 이유를 주석으로 작성하도록 합니다.
@ExtendWith(MockitoExtension::class)
class PointServiceTest{

    @Mock
    private lateinit var userPointTable: UserPointTable

    @Mock
    private lateinit var pointHistoryTable: PointHistoryTable

    @InjectMocks
    private lateinit var pointService: PointService

    private val userId = 1L
    private val initialPoint = UserPoint(id = userId, point = 100L, updateMillis = System.currentTimeMillis())

    @Test
    fun `포인트 조회 테스트`(){
        `when`(userPointTable.selectById(userId)).thenReturn(initialPoint)
        // pointService.getUserPoint(userId) 호출 시, userPointTable의 selectById 메소드가 호출되며, 해당 유저의 포인트 정보가 반환
        val userPoint = pointService.getUserPoint(userId)
        // assertEquals를 사용해 반환된 포인트 정보가 예상되는 initialPoint와 일치하는지 확인
        assertEquals(initialPoint.point, userPoint.point)
        // 실제로 selectById 메소드가 호출되었는지 검증
        verify(userPointTable).selectById(userId)
    }

    @Test
    fun `포인트 충전 테스트`(){
        val chargeAmount = 50L
        val updatedPoint = UserPoint(id = userId, point = initialPoint.point + chargeAmount, updateMillis = System.currentTimeMillis())

        `when`(userPointTable.selectById(userId)).thenReturn(initialPoint)
        // insertOrUpdate가 호출될 때 updatedPoint를 반환하도록 설정
        `when`(userPointTable.insertOrUpdate(userId, updatedPoint.point)).thenReturn(updatedPoint)

        val result = pointService.charge(userId, chargeAmount)
        assertEquals(updatedPoint.point, result.point)

        verify(userPointTable).insertOrUpdate(userId, updatedPoint.point)
//        // 포인트 충전 이력이 기록되었는지도 확인
//        verify(pointHistoryTable).insert(userId, chargeAmount, TransactionType.CHARGE, updatedPoint.updateMillis)
    }

//    @Test
//    fun `포인트 적립 시도 시 최대 잔고 초과 시 예외 발생`() {
//        // 최대 잔고 설정 (기존 잔고 + 충전할 금액이 최대 잔고를 초과하도록 설정)
//        val maxBalance = 100_000L
//        val initialPoint = UserPoint(id = userId, point = maxBalance - 10L, updateMillis = System.currentTimeMillis())
//        val chargeAmount = 20L  // 충전 시도가 최대 잔고를 초과하게 만드는 금액
//
//        `when`(userPointTable.selectById(userId)).thenReturn(initialPoint)
//
//        // assertThrows를 사용해 예외가 발생하는지 확인
//        val exception = assertThrows(IllegalArgumentException::class.java) {
//            pointService.charge(userId, chargeAmount)
//        }
//
//        // 예외 메시지가 예상되는 내용과 일치하는지 확인
//        assertEquals("최대 잔고를 초과할 수 없습니다. 초과하고싶으면 은행을 사시던가요.", exception.message)
//
//        // 최대 잔고를 초과했으므로 insertOrUpdate가 호출되지 않아야 함을 검증
//        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong())
//        // 포인트 이력이 기록되지 않아야 함을 검증
//        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(TransactionType::class.java), anyLong())
//    }

    @Test
    fun `포인트 사용 테스트`(){
        val useAmount = 30L
        val updatedPoint = UserPoint(id = userId, point = initialPoint.point - useAmount, updateMillis = System.currentTimeMillis())

        `when`(userPointTable.selectById(userId)).thenReturn(initialPoint)
        // insertOrUpdate가 호출될 때 updatedPoint를 반환하도록 설정
        `when`(userPointTable.insertOrUpdate(userId, updatedPoint.point)).thenReturn(updatedPoint)

        val result = pointService.use(userId, useAmount)
        assertEquals(updatedPoint.point, result.point)

        verify(userPointTable).insertOrUpdate(userId, updatedPoint.point)
//        // 포인트 사용 이력이 기록되었는지도 확인
//        verify(pointHistoryTable).insert(userId, useAmount, TransactionType.USE, updatedPoint.updateMillis)
    }


    @Test
    fun `포인트 사용 시 잔액이 부족한 경우 예외 발생`(){

        // `useAmount`가 현재 포인트보다 크므로 잔액이 부족해 예외가 발생
        val useAmount = 200L

        `when`(userPointTable.selectById(userId)).thenReturn(initialPoint)

        // assertThrows를 사용해 예외가 발생하는지 확인하고, 발생한 예외의 메시지가 "잔액이 부족합니다."와 일치하는지 확인
        val exception = assertThrows(IllegalArgumentException::class.java){
            pointService.use(userId, useAmount)
        }
        assertEquals("잔액이 부족합니다.", exception.message)

        // 잔액이 부족할 경우 insertOrUpdate와 insert 메소드가 호출되지 않아야 하므로, verify(..., never())로 이를 검증합니다.
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong())
//        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(TransactionType::class.java), anyLong())
    }

    @Test
    fun `포인트 이력 조회 테스트`() {
        // 유저의 포인트 이력(histories)을 미리 정의, selectAllByUserId가 호출될 때 이를 반환하도록 설정
        val histories = listOf(
            PointHistory(id = 1L, userId = userId, type = TransactionType.CHARGE, amount = 50L, timeMillis = System.currentTimeMillis()),
            PointHistory(id = 2L, userId = userId, type = TransactionType.USE, amount = 30L, timeMillis = System.currentTimeMillis())
        )
        `when`(pointHistoryTable.selectAllByUserId(userId)).thenReturn(histories)

        val result = pointService.getUserPointHistory(userId)

        // 결과의 사이즈가 예상되는 이력의 사이즈와 일치하는지 확인
        assertEquals(histories.size, result.size)

        // 각 이력 항목의 주요 필드만 비교 (id, userId, type, amount)
        for (i in histories.indices) {
            assertEquals(histories[i].id, result[i].id)
            assertEquals(histories[i].userId, result[i].userId)
            assertEquals(histories[i].type, result[i].type)
            assertEquals(histories[i].amount, result[i].amount)
            // timeMillis는 특정 범위 내에 있는지 확인 (대략적인 시간 차이 허용)
            assertTrue(Math.abs(histories[i].timeMillis - result[i].timeMillis) < 1000)
        }

        // selectAllByUserId 메소드가 호출되었는지 검증
        verify(pointHistoryTable).selectAllByUserId(userId)
    }

}