package dev.thecodebuffet.zcash.zip321.parser

class CharsetValidations {
    companion object {
        val isValidBase58OrBech32Char: (Char) -> Boolean = { char ->
            isValidBase58Char(char) || isValidBech32Char(char)
        }

        val isValidBase58Char: (Char) -> Boolean = { char ->
            char in '1'..'9' || char in 'A'..'H' || char in 'J'..'N' || char in 'P'..'Z' ||
                    char in 'a'..'k' || char in 'm'..'z'
        }

        val isValidBech32Char: (Char) -> Boolean = { char ->
            char in 'a'..'z' || char in '0'..'9'
        }
    }
}