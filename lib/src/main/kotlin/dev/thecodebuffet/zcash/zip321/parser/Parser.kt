    package dev.thecodebuffet.zcash.zip321.parser

    import RecipientAddress
    import com.copperleaf.kudzu.parser.ParserContext
    import com.copperleaf.kudzu.parser.chars.AnyCharParser
    import com.copperleaf.kudzu.parser.chars.CharInParser
    import com.copperleaf.kudzu.parser.chars.DigitParser
    import com.copperleaf.kudzu.parser.choice.PredictiveChoiceParser
    import com.copperleaf.kudzu.parser.many.ManyParser
    import com.copperleaf.kudzu.parser.many.SeparatedByParser
    import com.copperleaf.kudzu.parser.many.UntilParser
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
                UntilParser(
                    AnyCharParser(),
                    PredictiveChoiceParser(
                        LiteralTokenParser("."),
                        LiteralTokenParser("=")
                    )
                ),
                MaybeParser(
                    SequenceParser(
                        LiteralTokenParser("."),
                        parameterIndexParser
                    )
                )
            )
        ) {
            val paramName = it.node1.text

            if (!paramName.all { c -> CharsetValidations.isValidParamNameChar(c) }) {
                throw ZIP321.Errors.ParseError("Invalid paramname $paramName")
            } else {
                Pair<String, UInt?>(
                    it.node1.text,
                    it.node2.node?.node2?.value
                )
            }
        }

        val queryKeyAndValueParser = MappedParser(
            SequenceParser(
                optionallyIndexedParamName,
                LiteralTokenParser("="),
                ManyParser(
                    CharInParser(CharsetValidations.Companion.QcharCharacterSet.characters.toList())
                )
            )
        ) {
            Pair(it.node1.value,it.node3.text)
        }

        /**
         * parses a sequence of query parameters lead by query separator char (?)
         */
        val queryParamsParser = MappedParser(
            SequenceParser(
                LiteralTokenParser("?"),
                SeparatedByParser(
                    queryKeyAndValueParser,
                    LiteralTokenParser("&")
                )
            )
        ) {
            it.node2.nodeList.map { node -> node.value }
        }

        /**
         * maps a parsed Query Parameter key and value into an `IndexedParameter`
         * providing validation of Query keys and values. An address validation can be provided.
        */
        fun zcashParameter(
            parsedQueryKeyValue: Pair<Pair<String, UInt?>, String>,
            validatingAddress: ((String) -> Boolean)? = null
        ): IndexedParameter {
            val queryKey = parsedQueryKeyValue.first.first
            val queryKeyIndex = parsedQueryKeyValue.first.second?.let {
                if (it == 0u) {
                    throw ZIP321.Errors.InvalidParamIndex("$queryKey.0")
                } else {
                    it
                }
            } ?: 0u
            val queryValue = parsedQueryKeyValue.second

            val param = Param.from(
                queryKey,
                queryValue,
                queryKeyIndex,
                validatingAddress
            )

            return IndexedParameter(queryKeyIndex, param)
        }

        fun parseParameters(
            remainingString: String,
            leadingAddress: IndexedParameter?,
            validatingAddress: ((String) -> Boolean)? = null
        ): List<IndexedParameter> {

            val list = ArrayList<IndexedParameter>()

            leadingAddress?.let { list.add(it) }
            list.addAll(
                queryParamsParser.parse(ParserContext.fromString(remainingString))
                    .first
                    .value
                    .map { zcashParameter(it, validatingAddress) }
            )

            if (list.isEmpty()) {
                throw ZIP321.Errors.RecipientMissing(null)
            }

            return list
        }
    }