package dev.thecodebuffet.zcash.zip321.parser

import com.copperleaf.kudzu.parser.text.BaseTextParser

class ParameterNameParser: BaseTextParser(
isValidChar = { _, char -> CharsetValidations.isParamNameInParamNameCharset(char)  },
isValidText = { it.isNotEmpty() },
allowEmptyInput = false,
invalidTextErrorMessage = { "Expected [A-Za-z0-9+-], got '$it'" },
)