import com.mclaudt.moneytransfer.account.{Account, TransferDTO, UpdateAccountDTO, UpdateAccountPatchDTO}

trait AccountFixtures {

  val name = "Rich"
  val money = 100500L
  val id1 = 123L
  val id2 = 456L

  val unexistingId = 6060606060606060606L

  val accountFixture = Account(Some(id1),name,money)

  val accountWithNoIdFixture: Account = accountFixture.copy(id=None)

  val accountNonExistingFixture: Account = accountFixture.copy(id = Some(unexistingId))

  val transferDTOFixture = TransferDTO(id1,id2,money)

  val updateAccountDTOFixture = UpdateAccountDTO(id1,UpdateAccountPatchDTO(Some(name),Some(money)))

}
