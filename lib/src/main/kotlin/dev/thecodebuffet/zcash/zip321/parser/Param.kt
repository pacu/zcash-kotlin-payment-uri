package dev.thecodebuffet.zcash.zip321.parser

import MemoBytes
import RecipientAddress
import dev.thecodebuffet.zcash.zip321.ParamName

sealed class Param {

    data class Address(val recipientAddress: RecipientAddress) : Param()
    data class Amount(val amount: Amount) : Param()
    data class Memo(val memoBytes: MemoBytes) : Param()
    data class Label(val label: String) : Param()
    data class Message(val message: String) : Param()
    data class Other(val paramName: String, val value: String) : Param()

    val name: String
        get() = when (this) {
            is Address -> ParamName.ADDRESS.name
            is Amount -> ParamName.AMOUNT.name
            is Memo -> ParamName.MEMO.name
            is Label -> ParamName.LABEL.name
            is Message -> ParamName.MESSAGE.name
            is Other -> name
        }
}
