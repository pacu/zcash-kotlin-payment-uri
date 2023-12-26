data class Payment(
    val recipientAddress: RecipientAddress,
    val nonNegativeAmount: NonNegativeAmount,
    val memo: MemoBytes?,
    val label: String?,
    val message: String?,
    val otherParams: List<RequestParams>?
)
