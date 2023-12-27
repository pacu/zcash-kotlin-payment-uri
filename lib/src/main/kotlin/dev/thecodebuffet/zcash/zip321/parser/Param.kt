package dev.thecodebuffet.zcash.zip321.parser

import MemoBytes
import NonNegativeAmount
import RecipientAddress
import dev.thecodebuffet.zcash.zip321.ParamName
import dev.thecodebuffet.zcash.zip321.ZIP321
import dev.thecodebuffet.zcash.zip321.extensions.qcharDecode

sealed class Param {
    companion object {
        fun from(queryKey: String,
                 value: String,
                 index: UInt,
                 validatingAddress: ((String) -> Boolean)? = null): Param {
            return when (queryKey) {
                ParamName.ADDRESS.value -> {
                    try {
                        Param.Address(RecipientAddress(value, validatingAddress))
                    } catch(error: RecipientAddress.RecipientAddressError.InvalidRecipient) {
                        throw ZIP321.Errors.InvalidAddress(if (index > 0u) index else null)
                    }
                }
                ParamName.AMOUNT.value -> {
                    try {
                        Param.Amount(NonNegativeAmount(decimalString = value))
                    } catch (error: NonNegativeAmount.AmountError.NegativeAmount) {
                        throw ZIP321.Errors.AmountTooSmall(index)
                    } catch (error: NonNegativeAmount.AmountError.GreaterThanSupply) {
                        throw ZIP321.Errors.AmountExceededSupply(index)
                    } catch (error: NonNegativeAmount.AmountError.InvalidTextInput) {
                        throw ZIP321.Errors.ParseError("Invalid text input $value")
                    } catch (error: NonNegativeAmount.AmountError.TooManyFractionalDigits) {
                        throw  ZIP321.Errors.AmountTooSmall(index)
                    }
                }
                ParamName.LABEL.value -> {
                    when(val qcharDecoded = value.qcharDecode()) {
                        null -> throw ZIP321.Errors.InvalidParamValue("label", index)
                        else -> Param.Label(qcharDecoded)
                    }
                }
                ParamName.MESSAGE.value -> {
                    when(val qcharDecoded = value.qcharDecode()) {
                        null -> throw ZIP321.Errors.InvalidParamValue("message", index)
                        else -> Param.Message(qcharDecoded)
                    }
                }
                ParamName.MEMO.value -> {
                    try {
                        Param.Memo(MemoBytes(value))
                    } catch (error: MemoBytes.MemoError) {
                        throw ZIP321.Errors.MemoBytesError(error, index)
                    }
                }
                else -> {
                    if (queryKey.startsWith("req-")) {
                        throw ZIP321.Errors.UnknownRequiredParameter(queryKey)
                    }

                    when(val qcharDecoded = value.qcharDecode()) {
                        null -> throw ZIP321.Errors.InvalidParamValue("message", index)
                        else -> Param.Other(queryKey, qcharDecoded)
                    }
                }
            }
        }
    }

    data class Address(val recipientAddress: RecipientAddress) : Param()
    data class Amount(val amount: NonNegativeAmount) : Param()
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Param

        if (name != other.name) return false

        return when (this) {
            is Address -> recipientAddress == (other as? Address)?.recipientAddress
            is Amount -> amount == (other as? Amount)?.amount
            is Memo -> memoBytes == (other as? Memo)?.memoBytes
            is Label -> label == (other as? Label)?.label
            is Message -> message == (other as? Message)?.message
            is Other -> (other as? Other)?.let {
                    p -> p.paramName == paramName && p.value == value
            } ?: false
        }
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + when (this) {
            is Address -> recipientAddress.hashCode()
            is Amount -> amount.hashCode()
            is Memo -> memoBytes.hashCode()
            is Label -> label.hashCode()
            is Message -> message.hashCode()
            is Other -> {
                result = 31 * result + paramName.hashCode()
                result = 31 * result + value.hashCode()
                result
            }
        }
        return result
    }
}
