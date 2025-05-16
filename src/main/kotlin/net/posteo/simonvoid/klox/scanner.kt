package net.posteo.simonvoid.klox

//class Scanner(
//    private val errorReporter: ErrorReporter,
//) {
//    private val tokens = mutableListOf<Token>()
//    val scannedTokens: List<Token> get() = tokens
//
//    fun scanTokens(lines: Sequence<String>, filePath: String): List<Token> {
//        var start = 0
//        var current = 0
//        var line = 1
//
//        lines.forEach { line->
//            scanSingleToken()
//        }
//        tokens.add(EOF(line))
//        return tokens
//    }
//}

fun scanFile(
    lines: Sequence<String>,
    filePath: String,
    outTokenList: MutableList<Token>,
    outErrorList: MutableList<ScanResult.Failure>
) {
    lines.forEachIndexed { zeroIndex, line ->
        scanLine(line, zeroIndex + 1, filePath, outTokenList, outErrorList)
    }
    outTokenList.add(EOF)
}

private fun scanLine(
    line: String,
    lineNr: Int,
    filePath: String,
    outTokenList: MutableList<Token>,
    outErrorList: MutableList<ScanResult.Failure>
) {
    var index = 0
    while (index<line.length) {
        when(val scanResult= scanSingleToken(line, lineNr, index, filePath)) {
            is ScanResult.Success -> {
                index = scanResult.charsInLineScanned
                outTokenList.add(scanResult.token)
            }
            is ScanResult.Failure -> {
                outErrorList.add(scanResult)
                break
            }
            is ScanResult.EndOfLine -> {
                break
            }
        }
    }
}

private fun scanSingleToken(
    line: String,
    lineNr: Int,
    charIndexInLine: Int,
    filePath: String,
): ScanResult {
    val scanIndex: Int = run {
        // get rid of prefix spaces and comments
        var scanIndex = charIndexInLine
        line.matchAt(scanIndex, spacesRegex)?.let { matchResult ->
            scanIndex = matchResult.range.last + 1
        }
        if (scanIndex == line.length || line.startsWith("//", scanIndex)) {
            return ScanResult.EndOfLine
        }
        scanIndex
    }
    val tokenLoc = TokenLocation(
        filePath = filePath,
        line = line,
        lineNr = lineNr,
        charIndexInLine = scanIndex,
    )

    when {
        line.startsWith("(", scanIndex) -> 1 to LeftParen(tokenLoc)
        line.startsWith(")", scanIndex) -> 1 to RightParen(tokenLoc)
        line.startsWith("{", scanIndex) -> 1 to LeftBrace(tokenLoc)
        line.startsWith("}", scanIndex) -> 1 to RightBrace(tokenLoc)
        line.startsWith(".", scanIndex) -> 1 to Dot(tokenLoc)
        line.startsWith(",", scanIndex) -> 1 to Comma(tokenLoc)
        line.startsWith(";", scanIndex) -> 1 to Semicolon(tokenLoc)
        line.startsWith("+", scanIndex) -> 1 to Plus(tokenLoc)
        line.startsWith("-", scanIndex) -> 1 to Minus(tokenLoc)
        line.startsWith("*", scanIndex) -> 1 to Star(tokenLoc)
        line.startsWith("/", scanIndex) -> 1 to Slash(tokenLoc)
        line.startsWith("!=", scanIndex) -> 2 to NotEqual(tokenLoc)
        line.startsWith("!",  scanIndex) -> 1 to Not(tokenLoc)
        line.startsWith("==", scanIndex) -> 2 to EqualEqual(tokenLoc)
        line.startsWith("=",  scanIndex) -> 1 to Equal(tokenLoc)
        line.startsWith(">=", scanIndex) -> 2 to GreaterEqual(tokenLoc)
        line.startsWith(">",  scanIndex) -> 1 to Greater(tokenLoc)
        line.startsWith("<=", scanIndex) -> 2 to LessEqual(tokenLoc)
        line.startsWith("<",  scanIndex) -> 1 to Less(tokenLoc)
        line.startsWith(orRegex,     scanIndex) -> 2 to Or(tokenLoc)
        line.startsWith(andRegex,    scanIndex) -> 3 to And(tokenLoc)
        line.startsWith(ifRegex,     scanIndex) -> 2 to If(tokenLoc)
        line.startsWith(elseRegex,   scanIndex) -> 4 to Else(tokenLoc)
        line.startsWith(forRegex,    scanIndex) -> 3 to For(tokenLoc)
        line.startsWith(whileRegex,  scanIndex) -> 5 to While(tokenLoc)
        line.startsWith(classRegex,  scanIndex) -> 5 to Class(tokenLoc)
        line.startsWith(funRegex,    scanIndex) -> 3 to Fun(tokenLoc)
        line.startsWith(printRegex,  scanIndex) -> 5 to Print(tokenLoc)
        line.startsWith(returnRegex, scanIndex) -> 6 to Return(tokenLoc)
        line.startsWith(varRegex,    scanIndex) -> 3 to Var(tokenLoc)
        line.startsWith(superRegex,  scanIndex) -> 5 to Super(tokenLoc)
        line.startsWith(thisRegex,   scanIndex) -> 4 to This(tokenLoc)
        line.startsWith(trueRegex,   scanIndex) -> 4 to Literal.Bool.True(tokenLoc)
        line.startsWith(falseRegex,  scanIndex) -> 5 to Literal.Bool.False(tokenLoc)
        line.startsWith(nilRegex,    scanIndex) -> 3 to Literal.Nil(tokenLoc)
        else -> null
    }?.let { (charsUsedUp: Int, token: Token)->
        return ScanResult.Success(scanIndex + charsUsedUp, token)
    }

    line.matchAt(scanIndex, numberRegex)?.let { matchResult ->
        val numberStr = matchResult.value
        return try {
            ScanResult.Success(
                scanIndex + numberStr.length,
                Literal.Number(tokenLoc, numberStr.toDouble()),
            )
        } catch (e: NumberFormatException) {
            ScanResult.Failure(
                partialLineThatCouldNotBeParsed = line.substring(scanIndex),
                msg = "couldn't turn $numberStr into a Double: $e",
                lineNr = lineNr,
                filePath = filePath,
            )
        }
    }

    line.matchCharSeqAt(
        index = scanIndex,
        lineNr = lineNr,
        tokenLoc = tokenLoc,
    )?.let { scanResult ->
        return scanResult
    }

    line.matchAt(scanIndex, identifierRegex)?.let { matchResult ->
        val identifier = matchResult.value
        return ScanResult.Success(
            scanIndex + identifier.length,
            Identifier(tokenLoc, identifier)
        )
    }

    return ScanResult.Failure(
        partialLineThatCouldNotBeParsed = line.substring(scanIndex),
        msg = "couldn't extract next token",
        lineNr = lineNr,
        filePath = filePath,
    )
}

private val spacesRegex = """\p{Space}+""".toRegex()
private val orRegex = """or([^\p{Alnum}]|$)""".toRegex()
private val andRegex = """and([^\p{Alnum}]|$)""".toRegex()
private val ifRegex = """if([^\p{Alnum}]|$)""".toRegex()
private val forRegex = """for([^\p{Alnum}]|$)""".toRegex()
private val whileRegex = """while([^\p{Alnum}]|$)""".toRegex()
private val elseRegex = """else([^\p{Alnum}]|$)""".toRegex()
private val classRegex = """class([^\p{Alnum}]|$)""".toRegex()
private val funRegex = """fun([^\p{Alnum}]|$)""".toRegex()
private val printRegex = """print([^\p{Alnum}]|$)""".toRegex()
private val returnRegex = """return([^\p{Alnum}]|$)""".toRegex()
private val varRegex = """var([^\p{Alnum}]|$)""".toRegex()
private val superRegex = """super([^\p{Alnum}]|$)""".toRegex()
private val thisRegex = """this([^\p{Alnum}]|$)""".toRegex()
private val trueRegex = """true([^\p{Alnum}]|$)""".toRegex()
private val falseRegex = """false([^\p{Alnum}]|$)""".toRegex()
private val nilRegex = """nil([^\p{Alnum}]|$)""".toRegex()
private val numberRegex = """\p{Digit}+(\.\p{Digit}+)?""".toRegex()
private val identifierRegex = """\p{Alpha}\p{Alnum}*""".toRegex() // starts with letter, then letters or digits

//class Super(loc: TokenLocation): Token(loc)
//class This(loc: TokenLocation): Token(loc)

sealed class ScanResult {
    object EndOfLine: ScanResult()
    class Success(
        val charsInLineScanned: Int,
        val token: Token,
    ) : ScanResult() {
        override fun toString() =
            "ScanResult.Success(charsInLineScanned=$charsInLineScanned, token=$token)"
    }
    class Failure(
        val partialLineThatCouldNotBeParsed: String,
        val msg: String,
        val lineNr: Int,
        val filePath: String,
    ) : ScanResult() {
        override fun toString() =
            "ScanResult.Failure(msg=$msg, partialLine=$partialLineThatCouldNotBeParsed, lineNr=$lineNr, filePath=$filePath)"
    }
}

private fun String.matchAt(index: Int, regex: Regex): MatchResult? =
    // TODO switch to regex.matchAt when that leaves experimental status
    regex.find(this, index)?.let { matchResult ->
        if(matchResult.range.first == index) {
            matchResult
        } else {
            null
        }
    }

private fun String.startsWith(regex: Regex, index: Int): Boolean =
    this.matchAt(index, regex)?.let { true } ?: false

private fun String.matchCharSeqAt(
    index: Int,
    lineNr: Int,
    tokenLoc: TokenLocation,
): ScanResult? {
    if(this.elementAt(index)!='"') {
        return null
    }
    fun findClosingQuotationMarkIndex(line: String, afterIndex: Int): Int? {
        fun isEscaped(line: String, closingIndex: Int): Boolean {
            var backslashCount = 0 // "\""
            for(i in (closingIndex-1) downTo 0) {
                if(line.elementAt(i)=='\\') {
                    backslashCount++
                } else {
                    break
                }
            }
            return backslashCount%2==1
        }
        val closingIndex = line.indexOf('"', afterIndex + 1)
        return when {
            closingIndex == -1 -> null
            isEscaped(line, closingIndex) -> findClosingQuotationMarkIndex(line, closingIndex)
            else -> closingIndex
        }
    }

    return findClosingQuotationMarkIndex(this, index)?.let { closingQuotationMarkIndex->
        val lexeme = this.substring(index..closingQuotationMarkIndex)
        try {
            val charSeq = Literal.CharSeq.from(tokenLoc, lexeme)
            ScanResult.Success(
                charsInLineScanned = 1 + closingQuotationMarkIndex,
                token = charSeq,
            )
        } catch (e: IllegalArgumentException) {
            ScanResult.Failure(
                partialLineThatCouldNotBeParsed = this,
                msg = "couldn't instantiate CharSeq: $e",
                lineNr = lineNr,
                filePath = tokenLoc.filePath,
            )
        }
    } ?: ScanResult.Failure(
        partialLineThatCouldNotBeParsed = this,
        msg = "couldn't find closing quotation marks",
        lineNr = lineNr,
        filePath = tokenLoc.filePath,
    )
}

data class TokenLocation(
    val filePath: String,
    val line: String,
    val lineNr: Int,
    val charIndexInLine: Int,
)

sealed class Token() {
    sealed class InFile(val loc: TokenLocation): Token() {
        override fun equals(other: Any?): Boolean {
            if(other !is InFile) {
                return false
            }
            return this.loc==other.loc && this.javaClass==other.javaClass
        }

        override fun hashCode() = loc.hashCode()
        override fun toString() = "${this::class.simpleName}($loc)"
    }
}

class LeftParen(loc: TokenLocation): Token.InFile(loc)
class RightParen(loc: TokenLocation): Token.InFile(loc)
class LeftBrace(loc: TokenLocation): Token.InFile(loc)
class RightBrace(loc: TokenLocation): Token.InFile(loc)
class Comma(loc: TokenLocation): Token.InFile(loc)
class Dot(loc: TokenLocation): Token.InFile(loc)
class Semicolon(loc: TokenLocation): Token.InFile(loc)

class Minus(loc: TokenLocation): Token.InFile(loc)
class Plus(loc: TokenLocation): Token.InFile(loc)
class Slash(loc: TokenLocation): Token.InFile(loc)
class Star(loc: TokenLocation): Token.InFile(loc)

class Not(loc: TokenLocation): Token.InFile(loc)
class NotEqual(loc: TokenLocation): Token.InFile(loc)
class Equal(loc: TokenLocation): Token.InFile(loc)
class EqualEqual(loc: TokenLocation): Token.InFile(loc)
class Greater(loc: TokenLocation): Token.InFile(loc)
class GreaterEqual(loc: TokenLocation): Token.InFile(loc)
class Less(loc: TokenLocation): Token.InFile(loc)
class LessEqual(loc: TokenLocation): Token.InFile(loc)

class And(loc: TokenLocation): Token.InFile(loc)
class Or(loc: TokenLocation): Token.InFile(loc)

class If(loc: TokenLocation): Token.InFile(loc)
class Else(loc: TokenLocation): Token.InFile(loc)
class For(loc: TokenLocation): Token.InFile(loc)
class While(loc: TokenLocation): Token.InFile(loc)

class Class(loc: TokenLocation): Token.InFile(loc)
class Fun(loc: TokenLocation): Token.InFile(loc)
class Print(loc: TokenLocation): Token.InFile(loc)
class Return(loc: TokenLocation): Token.InFile(loc)
class Var(loc: TokenLocation): Token.InFile(loc)

class Super(loc: TokenLocation): Token.InFile(loc)
class This(loc: TokenLocation): Token.InFile(loc)
class Identifier(loc: TokenLocation, val name: String): Token.InFile(loc)

sealed class Literal<T>(loc: TokenLocation, val value: T): Token.InFile(loc) {
    class Number(line: TokenLocation, value: Double): Literal<Double>(line, value)
    sealed class Bool(line: TokenLocation, value: Boolean): Literal<Boolean>(line, value) {
        class True(loc: TokenLocation): Bool(loc, true)
        class False(loc: TokenLocation): Bool(loc, false)
    }

    class Nil(loc: TokenLocation): Literal<Nothing?>(loc, null)
    class CharSeq private constructor(
        loc: TokenLocation,
        private val lexeme: String,
        value: String,
    ): Literal<String>(loc, value) {

        override fun toString() = "CharSeq(lexeme='$lexeme', value='$value', loc=$loc)"

        companion object {
            fun from(
                loc: TokenLocation,
                lexeme: String,
            ) = CharSeq(loc, lexeme, toValue(lexeme))

            private fun toValue(lexeme: String): String {
                require(lexeme.elementAt(0)=='"' && lexeme.elementAt(lexeme.length-1)=='"') {
                    """lexeme [$lexeme]should be surrounded by " but isn't"""
                }
                var isEscaped = false
                return buildString {
                    for(i in 1 until lexeme.length-1) {
                        val char = lexeme.elementAt(i)
                        if(isEscaped) {
                            val escapedChar = when(char) {
                                '\\' -> '\\'
                                'n' -> '\n'
                                't' -> '\t'
                                '"' -> '"'
                                else -> throw IllegalArgumentException("""unsupported escape sequence: \$char in lexeme: '$lexeme' """)
                            }
                            append(escapedChar)
                            isEscaped = false
                        } else {
                            if(char=='\\') {
                                isEscaped = true
                            } else {
                                append(char)
                            }
                        }
                    }
                }
                //throw IllegalArgumentException("couldn't construct value from lexeme: $lexeme")
            }
        }
    }
}

object EOF: Token() {
    override fun toString() = "EndOfFile"
}
