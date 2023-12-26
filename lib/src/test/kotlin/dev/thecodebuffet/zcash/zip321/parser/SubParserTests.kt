package dev.thecodebuffet.zcash.zip321.parser

import com.copperleaf.kudzu.parser.ParserContext
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class SubParserTests: FreeSpec({
    "paramindex subparser" - {
        "parses non-zero single digit" {
            Parser(null).parameterIndexParser
                .parse(ParserContext.fromString("1")).first.value shouldBe 1u

            Parser(null).parameterIndexParser
                .parse(ParserContext.fromString("9")).first.value shouldBe 9u
        }

        "fails on zero single digit" {
            shouldThrowAny {
                Parser(null).parameterIndexParser
                    .parse(ParserContext.fromString("0"))
            }
        }

        "parses many digits" {
            Parser(null).parameterIndexParser
                .parse(ParserContext.fromString("12")).first.value shouldBe 12u
            Parser(null).parameterIndexParser
                .parse(ParserContext.fromString("123")).first.value shouldBe 123u
        }

        "fails on leading zero many digits" - {
            shouldThrowAny {
                Parser(null).parameterIndexParser
                    .parse(ParserContext.fromString("090"))
            }

        }

        "fails on too many digits" - {
            shouldThrowAny {
                Parser(null).parameterIndexParser
                    .parse(ParserContext.fromString("19999"))
            }
        }
    }
})