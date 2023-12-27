import dev.thecodebuffet.zcash.zip321.ZIP321
import dev.thecodebuffet.zcash.zip321.parser.Param

data class Payment(
    val recipientAddress: RecipientAddress,
    val nonNegativeAmount: NonNegativeAmount,
    val memo: MemoBytes?,
    val label: String?,
    val message: String?,
    val otherParams: List<OtherParam>?
) {
    companion object {}
}

data class OtherParam(val key: String, val value: String)

