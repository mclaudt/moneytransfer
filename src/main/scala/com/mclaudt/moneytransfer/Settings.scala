package com.mclaudt.moneytransfer

import akka.actor.{ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import com.mclaudt.moneytransfer.account.IdempotencyKeySupportConfig
import com.typesafe.config.Config

class SettingsImpl(config: Config) extends Extension {
  val host: String = config.getString("server.host")
  val port: Int = config.getInt("server.port")

  val timeToLiveInMillis: Long = config.getLong("idempotency.TTL")*1000
  val garbageCollectionInterval: Int = config.getInt("idempotency.gc")

  val idempotencyKeySupportConfig = IdempotencyKeySupportConfig(timeToLiveInMillis,garbageCollectionInterval)
}

object Settings extends ExtensionId[SettingsImpl] with ExtensionIdProvider {

  override def lookup: Settings.type = Settings

  override def createExtension(system: ExtendedActorSystem) =
    new SettingsImpl(system.settings.config)

}
