    package dev.thecodebuffet.zcash.zip321.parser

    import RecipientAddress
    import com.copperleaf.kudzu.parser.mapped.MappedParser
    import com.copperleaf.kudzu.parser.maybe.MaybeParser
    import com.copperleaf.kudzu.parser.sequence.SequenceParser
    import com.copperleaf.kudzu.parser.text.AnyTextParser
    import com.copperleaf.kudzu.parser.text.LiteralTokenParser
    import dev.thecodebuffet.zcash.zip321.ZIP321

    class Parser(val addressValidation: ((String) -> Boolean)?) {
        val maybeLeadingAddressParse = MappedParser (
            SequenceParser(
                LiteralTokenParser("zcash:"),
                MaybeParser (
                    AnyTextParser()
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
    }