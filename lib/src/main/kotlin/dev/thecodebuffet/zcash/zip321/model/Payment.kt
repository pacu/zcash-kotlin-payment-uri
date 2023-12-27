import dev.thecodebuffet.zcash.zip321.ZIP321
import dev.thecodebuffet.zcash.zip321.parser.Param

data class Payment(
    val recipientAddress: RecipientAddress,
    val nonNegativeAmount: NonNegativeAmount,
    val memo: MemoBytes?,
    val label: String?,
    val message: String?,
    val otherParams: List<RequestParams>?
) {
    companion object {}
}

