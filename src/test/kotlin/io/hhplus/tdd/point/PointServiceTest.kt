package io.hhplus.tdd.point

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class PointServiceTest(
    private val pointService: PointService,
    private val pointHistory: PointHistory,
    private val userPoint: UserPoint,
    private val transactionType: TransactionType,
) {

    @Test
    fun testGetUserPoint() {
        assertEquals(0, pointService.getUserPoint(1))
    }

    @Test
    fun testChargeUserPoint() {
    }

    @Test
    fun testUseUserPoint() {
    }

    @Test
    fun testGetUserPointHistory() {
    }
}