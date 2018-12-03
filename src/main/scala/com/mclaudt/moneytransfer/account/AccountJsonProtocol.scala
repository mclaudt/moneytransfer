package com.mclaudt.moneytransfer.account

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object AccountJsonProtocol extends DefaultJsonProtocol {
  implicit val accountFormat: RootJsonFormat[Account] = jsonFormat3(Account)
  implicit val transferDTOFormat: RootJsonFormat[TransferDTO] = jsonFormat4(TransferDTO)
  implicit val updateAccountPatchFormat: RootJsonFormat[UpdateAccountPatchDTO] = jsonFormat2(UpdateAccountPatchDTO)
  implicit val updateAccountDTOFormat: RootJsonFormat[UpdateAccountDTO] = jsonFormat2(UpdateAccountDTO)

}
