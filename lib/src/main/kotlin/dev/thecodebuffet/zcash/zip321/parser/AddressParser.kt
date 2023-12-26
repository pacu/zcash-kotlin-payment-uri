package dev.thecodebuffet.zcash.zip321.parser

import com.copperleaf.kudzu.parser.text.BaseTextParser

class AddressTextParser: BaseTextParser(
    isValidChar = { _, char -> CharsetValidations.isValidBase58OrBech32Char(char) },
    isValidText = { it.isNotEmpty() },
    allowEmptyInput = false,
    invalidTextErrorMessage = { "Expected bech32 or Base58 text, got '$it'" },
)