package com.example.myapplication.SyntaxHighlight

import android.content.Context
import android.util.Log
import com.example.myapplication.R
import com.example.myapplication.SyntaxHighlight.Tokens.Token
import com.example.myapplication.SyntaxHighlight.Tokens.TokenType
import com.example.myapplication.Trie
import java.util.regex.Matcher
import java.util.regex.Pattern


fun CharSequence.charAtSafe(i: Int):Char {
    if (i < this.length)
        return this[i]
    else
        return (-1).toChar()

}
class ParseResult(var token: Token? = null, var position: Int = -1)
class CPlusPlusHighlighter(val context: Context):Highlighter() {
    fun parseFromPosition(s: CharSequence, index: Int) : Int {
        var parseResult: ParseResult = ParseResult()
        var position = index
        if (parentheses.indexOf(s[position]) != -1) {
            parseResult.token = Token(TokenType.BRACKETS, s, position, position+1)
            parseResult.position = position + 1
        } else if (isIdentifier(position, s)){
            val match = reservedWordsTrie.match(s, position)
            if (match == -1)
                parseResult = parseIdentifier(position, s)
            else {
                parseResult.token = Token(TokenType.KEYWORD, s, position, match)
                parseResult.position = match
            }
        } else if (isComment(position, s))
            parseResult = parseComment(position, s)
        else if (isPreProcessor(position, s))
            parseResult = parsePreProcessor(position, s)
        else if (operators.indexOf(s[position]) != -1) {
            parseResult.token = Token(TokenType.OPERATOR, s, position, position+1)
            parseResult.position = position + 1
        }  else if (isStringLiteral(position, s))
            parseResult = parseStringLiteral(position, s)
        else if (isNumber(position, s))
            parseResult = parseWithRegex(digitsPattern, position, s, TokenType.NUMBER)

        if (parseResult.token != null) {
            tokens.insertTail(parseResult.token!!)
            position = parseResult.position
        } else {
            position++
        }
        return position
    }
    override fun parse(s: CharSequence) {
        var position = 0;
        while (position < s.length) {
            position = parseFromPosition(s, position)
        }
    }

    override fun update(s: CharSequence, start: Int, end: Int) {
//        TODO("Not yet implemented")
    }

    val commentPattern = Pattern.compile("""(//.*\n)|(/\*[^*]*\*+(?:[^/*][^*]*\*+)*/)""")
    val identifiersPattern = Pattern.compile("""[a-zA-Z_](\w|_)*""")
    val reservedWords = arrayOf("alignas",
            "alignof",
            "and",
            "and_eq",
            "asm",
            "atomic_cancel",
            "atomic_commit",
            "atomic_noexcept",
            "auto",
            "bitand",
            "bitor",
            "bool",
            "break",
            "case",
            "catch",
            "char",
            "char8_t",
            "char16_t ",
            "char32_t",
            "class",
            "compl",
            "concept",
            "const",
            "consteval",
            "constexpr",
            "constinit",
            "const_cast",
            "continue",
            "co_await",
            "co_return",
            "co_yield",
            "decltype",
            "default",
            "delete",
            "do",
            "double",
            "dynamic_cast",
            "else",
            "enum",
            "explicit",
            "export",
            "extern",
            "false",
            "float",
            "for",
            "friend",
            "goto",
            "if",
            "inline",
            "int",
            "long",
            "mutable",
            "namespace",
            "new",
            "noexcept",
            "not",
            "not_eq",
            "nullptr",
            "operator",
            "or",
            "or_eq",
            "private",
            "protected",
            "public",
            "reflexpr",
            "register",
            "reinterpret_cast",
            "requires",
            "return",
            "short",
            "signed",
            "sizeof",
            "static",
            "static_assert",
            "static_cast",
            "struct",
            "switch",
            "synchronized",
            "template",
            "this",
            "thread_local",
            "throw",
            "true",
            "try",
            "typedef",
            "typeid",
            "typename",
            "union",
            "unsigned",
            "using",
            "virtual",
            "void",
            "volatile",
            "wchar_t",
            "while",
            "xor",
            "xor_e")
    val reservedWordsTrie = Trie()
    val digitsPattern: Pattern
    val suffix = "ul{0,2}|l{1,2}u?"

    val parentheses = "{}[]()"
    val operators = "><=~:,.+-*/&|%^?;"

    val decimalNumber = """[1-9][0-9']*($suffix)?"""
    val octalNumber = """0[0-7']*($suffix)?"""
    val hexNumber = """0x[0-9a-f']+($suffix)?"""
    val binaryNumber = """0b[0-1']+($suffix)?"""

//    val floatSequence = decimalNumber
    val digitSequence = """\d+"""
    val floatExponent = """e(\+|-)?$digitSequence"""
    val floatSuffix = """(f|l)?"""
    val floatType1 = """$digitSequence$floatExponent"""
    val floatType2 = """$digitSequence\.($digitSequence)?($floatExponent)?($floatSuffix)?"""
    val floatType3 = """($digitSequence)?\.($digitSequence)($floatExponent)?($floatSuffix)?"""

    val hexSequence = """[0-9a-f]+"""
    val hexFloatExponent = """p(\+|-)?$decimalNumber"""
    val hexFloatType1 = """0x($hexSequence)($hexFloatExponent)?($floatSuffix)?"""
    val hexFloatType2 = """0x($hexSequence)\.($hexFloatExponent)?($floatSuffix)?"""
    val hexFloatType3 = """0x($hexSequence)?\.($hexSequence)($hexFloatExponent)?($floatSuffix)?"""

    init {
        val digitsRegexes = arrayOf(
                hexNumber,
                hexFloatType3,
                hexFloatType2,
                hexFloatType1,
                floatType3,
                floatType2,
                floatType1,
                binaryNumber,
                decimalNumber,
                octalNumber
                );
        val digitsRegex = digitsRegexes.joinToString(separator = ")|(",prefix = "(", postfix = ")")
        Log.d("Pattern","""($digitsRegex) """)
        digitsPattern = Pattern.compile("""($digitsRegex)""", Pattern.CASE_INSENSITIVE)

        for (word in reservedWords)
            reservedWordsTrie.insert(word)
    }


    fun checkNeedUpdate(s:CharSequence, start: Int, end: Int):Boolean {
        for (i in start..end-1) {
            if (s[i].isWhitespace() || parentheses.indexOf(s[i]) != -1 || operators.indexOf(s[i]) != -1)
                return true
        }
        return false
    }

//    fun hightliht(s: CharSequence, start: Int, end: Int): Int {
//        val startTime = System.currentTimeMillis()
//        var position = start;
//        while (position < end && position < s.length) {
//            if (parentheses.indexOf(s[position]) != -1) {
//                addToken(s, TokenType.BRACKETS, position, position+1)
//                position++
//            } else if (isIdentifier(position, s)){
//                val match = reservedWordsTrie.match(s, position)
//                if (match == -1)
//                    position = parseIdentifier(position, s)
//                else {
//                    addToken(s, TokenType.KEYWORD, position, match)
//                    position = match;
//                }
//            } else if (isComment(position, s))
//                position = parseComment(position, s)
//            else if (isPreProcessor(position, s))
//                position = parsePreProcessor(position, s)
//            else if (operators.indexOf(s[position]) != -1) {
//                addToken(s, TokenType.OPERATOR, position, position+1)
//                position++
//            }  else if (isStringLiteral(position, s))
//                position = parseStringLiteral(position, s)
//            else if (isNumber(position, s))
//                position = parseWithRegex(digitsPattern, position, s, R.color.darkula_number)
//            else
//                position++
//        }
//        val end = System.currentTimeMillis()
//        Log.d("Hightling duration",((end - startTime) / 1000.0).toString())
//        return position
//
//    }

    private fun isPreProcessor(position: Int, s: CharSequence):Boolean {
        return s[position] =='#'
    }

    private fun isNumber(position: Int, s: CharSequence): Boolean {
        return (s[position] == '.' || s[position].isDigit())
    }

    fun isComment(position: Int, s: CharSequence): Boolean {
        return (s[position] == '/' &&
            (s.charAtSafe(position+1) == '/' || s.charAtSafe(position+1) == '*'))
    }

    fun isIdentifier(position: Int, s: CharSequence):Boolean {
        return (s[position] == '_' || s[position].isLetter())
    }
    fun isStringLiteral(position: Int, s: CharSequence): Boolean {
        if (s[position] == '\'' || s[position] == '\"')
            return true
        return false
    }

    fun parseIdentifier(position: Int, s: CharSequence): ParseResult {
        var index = position
        while (index < s.length && (s[index].isLetterOrDigit() || s[index] == '_'))
            index++
        return ParseResult(Token(TokenType.IDENTIFIER, s, position, index), index)
    }

    fun parsePreProcessor(position: Int, s: CharSequence): ParseResult {
        var index = position
        while (index < s.length && !s[index].isWhitespace())
            index++
        return ParseResult(Token(TokenType.PREPROCESSOR, s, position, index), index)
    }

    fun parseComment(position: Int, s: CharSequence): ParseResult {
        var index = position
        if (s[index+1] == '*') {
            while (index < s.length) {
                if (s[index] == '*') {
                    if (s.charAtSafe(index+1)== '/') {
//                        addToken(s, R.color.darkula_comment, position, index + 2)
                        return ParseResult(Token(TokenType.MULTILINE_COMMENT, s, position, index + 2), index + 2)
                    }
                }
                index++
            }
            return ParseResult(null, index)
        } else {
            index++;
            while (index < s.length && s[index] != '\n')
                index++
//            addToken(s, R.color.darkula_comment, position, index)

            return ParseResult(Token(TokenType.COMMENT, s, position,index + 1), index + 1)
        }
    }
    fun parseStringLiteral(position: Int, s: CharSequence): ParseResult {
        var index = position + 1;
        val charType = s[position]
        while (index < s.length && s[index] != charType && s[index] != '\n') {
            if (s[index] == '\\')
                index++;
            index++;
        }
        if (s.charAtSafe(index) == charType)
            return ParseResult(Token(TokenType.STRING_LITERAL, s, position, index + 1), index +  1)
//            addToken(s, R.color.darkula_string, position, index + 1);
        return ParseResult(null, index + 1)
    }
    fun parseWithRegex(pattern: Pattern, position: Int, s: CharSequence, type: TokenType): ParseResult {
        val matcher = pattern.matcher(s)
        matcher.region(position, s.length)
        if (matcher.lookingAt()) {
//            addToken(s, colorId, matcher)
            return ParseResult(Token(type, s, matcher.start(), matcher.end()), matcher.end())
        }
        return ParseResult(null, position + 1)
    }
//    fun parseComment(position: Int, s: CharSequence) {
//
//    }
    fun addToken(s:CharSequence, type: TokenType, matcher: Matcher) {
        addToken(s, type, matcher.start(), matcher.end())
//        tokens.insertTail(Token(type, s, matcher.start(), matcher.end()))
//        s.setSpan(
//                ForegroundColorSpan(ContextCompat.getColor(context, colorId)),
//                matcher.start(),
//                matcher.end(),
//                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    fun addToken(s:CharSequence, type: TokenType, begin: Int, end: Int) {
        tokens.insertTail(Token(type, s, begin, end))

//        s.setSpan(
//                ForegroundColorSpan(ContextCompat.getColor(context, colorId)),
//                begin,
//                end,
//                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}