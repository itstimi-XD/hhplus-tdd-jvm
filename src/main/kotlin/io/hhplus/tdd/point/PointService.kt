package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import org.springframework.stereotype.Service

//TODO : bean test 방법
//TODO : test api

@Service
class PointService(
    private val userPointTable: UserPointTable,
    private val pointHistoryTable: PointHistoryTable
) {

    fun getUserPoint(id: Long): UserPoint {
        return userPointTable.selectById(id)
    }

    fun charge(id: Long, amount: Long): UserPoint {
        val userPoint = userPointTable.selectById(id)
        val newPoint = userPoint.point + amount
        val updatedUserPoint = userPointTable.insertOrUpdate(id, newPoint)
        // 이력 추가
        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis())

        return updatedUserPoint
    }

    fun use(id: Long, amount: Long): UserPoint {
        val currentPoint = userPointTable.selectById(id)
        if (currentPoint.point < amount) {
            throw IllegalArgumentException("잔액이 부족합니다.")
        }
        val updatedUserPoint = userPointTable.insertOrUpdate(id, currentPoint.point - amount)
        // 이력 추가
        pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis())

        return updatedUserPoint
    }

    fun getUserPointHistory(userId: Long): List<PointHistory> {
        return pointHistoryTable.selectAllByUserId(userId)
    }
}