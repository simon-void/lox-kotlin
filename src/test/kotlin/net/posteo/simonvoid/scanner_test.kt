package net.posteo.simonvoid

import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import kotlin.test.assertEquals

class ScannerTest {
    @Test(dataProvider = "scan CharSeq test data")
    fun `test CharSeq parsing`(
        charSeqLexeme: String,
        expectedCharSeqValue: String,
    ) {
        val actualTokens = mutableListOf<Token>()
        val actualFailures = mutableListOf<ScanResult.Failure>()

        scanFile(
            lines = sequenceOf(charSeqLexeme),
            filePath = "scanner_test/data.lox",
            outTokenList = actualTokens,
            outErrorList = actualFailures,
        )

        assertEquals(0, actualFailures.size, "unexpected failures: $actualFailures")
        assertEquals(2, actualTokens.size, "should contain 2 tokes (CharSeq and EOF) but contains: ${actualTokens.size}")
        val actualCharSeqValue = (actualTokens.first() as Literal.CharSeq).value
        assertEquals(expectedCharSeqValue, actualCharSeqValue, "CharSeq.value")
    }

    @DataProvider
    fun `scan CharSeq test data`(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(""" " aB9 " """, """ aB9 """),
            arrayOf(""" "öäüßå" """, """öäüßå"""),
            arrayOf(""" "a\\B\"1\n" """, """a\B"1${'\n'}"""),
        )
    }

    @Test(dataProvider = "scan file test data")
    fun `test successful scan file`(
        linesSeq: Sequence<String>,
        filePath: String,
        expectedTokens: List<Token>,
    ) {
        val actualTokens = mutableListOf<Token>()
        val actualFailures = mutableListOf<ScanResult.Failure>()

        scanFile(
            lines = linesSeq,
            filePath = filePath,
            outTokenList = actualTokens,
            outErrorList = actualFailures,
        )

        assertEquals(actualFailures.size, 0, "unexpected failures: $actualFailures")
        assertListEquals(expectedTokens, actualTokens, "tokens")
    }

    @DataProvider
    fun `scan file test data`(): Array<Array<Any>> {
        val filePath = "scanner_test/test-data.lox"
        return arrayOf(
            run {
                val listOfBasics = listOf(
                    "(", ")", "{", "}", ",", ".", ";",
                    "-", "+", "/", "*",
                    "!", "!=", "=", "==", ">", ">=", "<", "<=",
                    "and", "or", "if", "else", "for", "while",
                    "class", "fun", "print", "return", "var",
                    "super", "this", "someValue",
                    """"text"""", "1025.5", "true", "false", "nil"
                )
                var lineNr = 1
                fun loc() = TokenLocation(
                    filePath = filePath,
                    line = listOfBasics[lineNr-1],
                    lineNr = lineNr,
                    charIndexInLine = 0,
                ).also {
                    lineNr++
                }

                arrayOf(
                    listOfBasics.asSequence(),
                    filePath,
                    listOf(
                        LeftParen(loc()), RightParen(loc()), LeftBrace(loc()), RightBrace(loc()), Comma(loc()), Dot(loc()), Semicolon(loc()),
                        Minus(loc()), Plus(loc()), Slash(loc()), Star(loc()),
                        Not(loc()), NotEqual(loc()), Equal(loc()), EqualEqual(loc()), Greater(loc()), GreaterEqual(loc()), Less(loc()), LessEqual(loc()),
                        And(loc()), Or(loc()), If(loc()), Else(loc()), For(loc()), While(loc()),
                        Class(loc()), Fun(loc()), Print(loc()), Return(loc()), Var(loc()),
                        Super(loc()), This(loc()), Identifier(loc(), "someValue"),
                        Literal.CharSeq.from(loc(), """"text""""), Literal.Number(loc(), 1015.5), Literal.Bool.True(loc()), Literal.Bool.False(loc()), Literal.Nil(loc()),
                        EOF
                    ),
                )
            },
            run {
                val listOfBasicsWithPadding = listOf(
                    " ( ", " ) ", " { ", " } ", " , ", " . ", " ; ",
                    " - ", " + ", " / ", " * ",
                    " ! ", " != ", " = ", " == ", " > ", " >= ", " < ", " <= ",
                    " and ", " or ", " if ", " else ", " for ", " while ",
                    " class ", " fun ", " print ", " return ", " var ",
                    " super ", " this ", " someValue4 ",
                    """ "text" """, " 1025.5 ", " true ", " false ", " nil "
                )
                var lineNr = 1
                fun loc() = TokenLocation(
                    filePath = filePath,
                    line = listOfBasicsWithPadding[lineNr-1],
                    lineNr = lineNr,
                    charIndexInLine = 1,
                ).also {
                    lineNr++
                }

                arrayOf(
                    listOfBasicsWithPadding.asSequence(),
                    filePath,
                    listOf(
                        LeftParen(loc()), RightParen(loc()), LeftBrace(loc()), RightBrace(loc()), Comma(loc()), Dot(loc()), Semicolon(loc()),
                        Minus(loc()), Plus(loc()), Slash(loc()), Star(loc()),
                        Not(loc()), NotEqual(loc()), Equal(loc()), EqualEqual(loc()), Greater(loc()), GreaterEqual(loc()), Less(loc()), LessEqual(loc()),
                        And(loc()), Or(loc()), If(loc()), Else(loc()), For(loc()), While(loc()),
                        Class(loc()), Fun(loc()), Print(loc()), Return(loc()), Var(loc()),
                        Super(loc()), This(loc()), Identifier(loc(), "someValue4"),
                        Literal.CharSeq.from(loc(), """"text""""), Literal.Number(loc(), 1015.5), Literal.Bool.True(loc()), Literal.Bool.False(loc()), Literal.Nil(loc()),
                        EOF
                    ),
                )
            },
            run {
                val line = """ (  1.25 )  """
                fun loc(charIndex: Int) = TokenLocation(
                    filePath = filePath,
                    line = line,
                    lineNr = 1,
                    charIndexInLine = charIndex,
                )

                arrayOf(
                    sequenceOf(line),
                    filePath,
                    listOf(
                        LeftParen(loc(1)), Literal.Number(loc(4), 1.25), RightParen(loc(9)),
                        EOF
                    ),
                )
            },
        )
    }

}

fun <T> assertListEquals(expectedList: List<T>, actualList: List<T>, content: String? = null) {
    assertEquals(expectedList.size, actualList.size, "size of list" + (content?.let { " of $it" } ?: ""))
    for(i in expectedList.indices) {
        assertEquals(expectedList[i], actualList[i], "element $i of list" + (content?.let { " of $it" } ?: ""))
    }
}