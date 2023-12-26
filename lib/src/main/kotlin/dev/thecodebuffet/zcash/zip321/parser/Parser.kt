    package dev.thecodebuffet.zcash.zip321.parser

    import RecipientAddress
    import com.copperleaf.kudzu.parser.chars.CharInParser
    import com.copperleaf.kudzu.parser.chars.DigitParser
    import com.copperleaf.kudzu.parser.many.AtMostParser
    import com.copperleaf.kudzu.parser.many.ManyParser
    import com.copperleaf.kudzu.parser.mapped.MappedParser
    import com.copperleaf.kudzu.parser.maybe.MaybeParser
    import com.copperleaf.kudzu.parser.sequence.SequenceParser
    import com.copperleaf.kudzu.parser.text.LiteralTokenParser
    import dev.thecodebuffet.zcash.zip321.ZIP321


    class Parser(val addressValidation: ((String) -> Boolean)?) {
        val maybeLeadingAddressParse = MappedParser (
            SequenceParser(
                LiteralTokenParser("zcash:"),
                MaybeParser (
                    AddressTextParser()
                )
            )
        ) {
            val addressValue: IndexedParameter? = it.node2.node?.let { textNode ->
                {
                    IndexedParameter(
                        index = 0u,
                        param = Param.Address(
                            RecipientAddress(
                                textNode.text,
                                validating = addressValidation
                            )
                        )
                    )
                }

            }?.invoke()
            addressValue
        }

        var parameterIndexParser = MappedParser(
            SequenceParser(
                    CharInParser(CharRange('1','9')),
                    MaybeParser(
                        ManyParser(
                            DigitParser()
                        )
                    )
                )
            ) {
            val firstDigit = it.node1.text

            (firstDigit + it.node2.let { node ->
                if (node.text.length > 3) {
                    throw ZIP321.Errors.InvalidParamIndex(firstDigit + node.text)
                } else {
                    node.text
                }
            }).toUInt()
        }


        val optionallyIndexedParamName = MappedParser(
            SequenceParser(
                ParameterNameParser(),
                MaybeParser(
                    SequenceParser(
                        LiteralTokenParser("."),
                        parameterIndexParser
                    )
                )
            )
        ) {
            Pair<String,UInt?>(
                it.node1.text,
                it.node2.node?.let { idx -> idx.node2.value }
            )
        }
    }