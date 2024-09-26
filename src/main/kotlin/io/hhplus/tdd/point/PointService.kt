package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import org.springframework.stereotype.Service
import java.util.concurrent.locks.ReentrantLock

//TODO : bean test 방법
//TODO : test api
@Service
class PointService(
    private val userPointTable: UserPointTable,
    private val pointHistoryTable: PointHistoryTable
) {

    private val MAX_BALANCE = 100_000L
    private val lock = ReentrantLock()

    fun getUserPoint(id: Long): UserPoint {
        return userPointTable.selectById(id)
    }

    fun charge(id: Long, amount: Long): UserPoint {
        lock.lock()
        try {
            val userPoint = userPointTable.selectById(id)
            val newPoint = userPoint.point + amount

            if (newPoint > MAX_BALANCE) {
                throw IllegalArgumentException("최대 잔고를 초과할 수 없습니다. 초과하고싶으면 은행을 사시던가요.")
            }

            val updatedUserPoint = userPointTable.insertOrUpdate(id, newPoint)
            // 이력 추가
            pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis())

            return updatedUserPoint
        } finally {
            lock.unlock()
        }
    }

    fun use(id: Long, amount: Long): UserPoint {
        lock.lock()
        try {
            val currentPoint = userPointTable.selectById(id)
            if (currentPoint.point < amount) {
                throw IllegalArgumentException("잔액이 부족합니다.")
            }
            val updatedUserPoint = userPointTable.insertOrUpdate(id, currentPoint.point - amount)
            // 이력 추가
            pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis())

            return updatedUserPoint
        } finally {
            lock.unlock()
        }
    }

    fun getUserPointHistory(userId: Long): List<PointHistory> {
        return pointHistoryTable.selectAllByUserId(userId)
    }
}