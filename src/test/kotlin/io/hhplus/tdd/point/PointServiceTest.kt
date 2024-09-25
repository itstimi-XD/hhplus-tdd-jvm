package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.mockito.Mock

class PointServiceTest(
    private val pointService: PointService,
    private val pointHistory: PointHistory,
    private val userPoint: UserPoint,
    private val transactionType: TransactionType,
) {

    @Mock
    private lateinit var userPointTable: UserPointTable

    @Mock
    private lateinit var pointHistoryTable: PointHistoryTable

    @Test
    fun testGetUserPoint() {
        assertEquals(0, pointService.getUserPoint(1))
    }

    @Test
    fun testCharge() {
    }

    @Test
    fun testUse() {
    }

    @Test
    fun testGetUserPointHistory() {
    }
}ㄴ