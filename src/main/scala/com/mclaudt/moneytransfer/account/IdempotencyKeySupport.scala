package com.mclaudt.moneytransfer.account

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.BiConsumer

trait IdempotencyKeySupport[K, V] {

  private val transactions = new ConcurrentHashMap[K, (V, Long)]()

  private val counter = new AtomicInteger(0)

  private def cleanTransactions(t: ConcurrentHashMap[K, (V, Long)]): Unit = {
    val bic = new BiConsumer[K, (V, Long)] {
      def accept(k: K, v: (V, Long)): Unit = {
        if (v._2 <= System.currentTimeMillis()) t.remove(k)
      }
    }
    t.forEach(bic)
  }


  def config: IdempotencyKeySupportConfig

  def tick(): Unit = if (counter.incrementAndGet() == config.garbageCollectionInterval) {
    counter.set(0)
    cleanTransactions(transactions)
  }

  def hasNotOutdatedKey(k: K): Boolean = transactions.containsKey(k) && transactions.get(k)._2 > System.currentTimeMillis()

  def getResult(k: K): V = transactions.get(k)._1

  def writeResult(k: K, v: V): (V, Long) = transactions.put(k, (v, System.currentTimeMillis() + config.timeToLiveInMillis))

}

case class IdempotencyKeySupportConfig(timeToLiveInMillis: Long, garbageCollectionInterval: Int)