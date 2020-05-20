package com.example.myapplication.SyntaxHighlight

import android.content.Context
import android.util.Log
import com.example.myapplication.SyntaxHighlight.LanguageConstants.*
import com.example.myapplication.SyntaxHighlight.Tokens.Token
import com.example.myapplication.SyntaxHighlight.Tokens.TokenList
import com.example.myapplication.SyntaxHighlight.Tokens.TokenType
import com.example.myapplication.Trie
import com.example.myapplication.utils.InterSectionResult
import com.example.myapplication.utils.intersect
import com.example.myapplication.utils.toString
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.min


fun CharSequence.charAtSafe(i: Int):Char {
    if (i < this.length)
        return this[i]
    else
        return (-1).toChar()

}

class ParseResult(var token: Token? = null, var position: Int = -1)
class CPlusPlusHighlighter(val context: Context):Highlighter() {
    var tokenIdentifiers: HashSet<String> = HashSet()
    override fun identifiers(): HashSet<String> {
        return tokenIdentifiers
    }

    fun parseFromPosition(tokenList: ArrayList<Token>, identifiers: HashSet<String>, s: CharSequence, index: Int) : Int {
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
            if (parseResult.token!!.type == TokenType.IDENTIFIER)
                identifiers.add(parseResult.token!!.getString())
            tokenList.add(parseResult.token!!)
            position = parseResult.position
        } else {
            position++
        }
        return position
    }
    override fun parse(s: CharSequence) {
        tokens.clear()
        tokenIdentifiers.clear()
        var position = 0;
        while (position < s.length) {
            position = parseFromPosition(tokens, tokenIdentifiers, s, position)
        }
    }
    override fun update(s: CharSequence, start: Int, end: Int, offset: Int, cursor: Int) {
        var startTime = System.currentTimeMillis()
        var newIdentifiers = HashSet<String>()
        if (s.isEmpty()) {
            tokens.clear()
            return
        }
        var updateStartIndex = start - 1
        var updateEndIndex = end
//        if (start == end) {
//            updateEndIndex = start - 1
//        }
        while (updateStartIndex >= 0 && updateStartIndex < s.length && s[updateStartIndex] != '\n')
            updateStartIndex--;
        if (updateStartIndex < 0)
            updateStartIndex = 0
        else if (updateStartIndex >= s.length)
            updateStartIndex = s.length - 1

        while (updateEndIndex < s.length && s[updateEndIndex] != '\n')
            updateEndIndex++
        if (updateEndIndex >= s.length)
            updateEndIndex = s.length - 1


        var firstChangedTokenIter = -1
        var beforeFirstChangedTokenIter = -1
        val iter = tokens.iterator()
        var firstTokenToOffset = -1
        var index = 0
        while (iter.hasNext()) {
            val curNode = index
            index++
            val data = iter.next()
            val interSectionResult = intersect(data.start, data.end, updateStartIndex, updateEndIndex - offset + 1)
            if (interSectionResult == InterSectionResult.INTERSECTS) {
                firstChangedTokenIter = curNode
                break
            }

            if (interSectionResult == InterSectionResult.OUTSIDE_LEFT) {
                beforeFirstChangedTokenIter = curNode
                if (data.type == TokenType.IDENTIFIER)
                    newIdentifiers.add(data.getString())
            }
            if (interSectionResult == InterSectionResult.OUTSIDE_RIGHT) {
                firstTokenToOffset = curNode
                break
            }
        }

        var firstChangedTokenStart = updateStartIndex + 1
        if (firstChangedTokenIter != -1)
            firstChangedTokenStart = tokens[firstChangedTokenIter].start

        var startIndex = min(updateStartIndex, firstChangedTokenStart)

        val newTokenList = ArrayList<Token>()
        while (startIndex <= updateEndIndex) {
            startIndex = parseFromPosition(newTokenList, newIdentifiers, s, startIndex)
            if (startIndex == cursor && !newTokenList.isEmpty())
                newIdentifiers.remove(newTokenList.last().getString())
        }

        val lastTokenEnd = startIndex
        var lastChangedTokenIter = firstChangedTokenIter

        while (firstTokenToOffset == -1 && iter.hasNext()) {
            val curNode = index
            index++
            val data = iter.next()
            val interSectionResult = intersect(data.start, data.end, updateStartIndex, lastTokenEnd - offset)

            if (interSectionResult == InterSectionResult.INTERSECTS) {
                lastChangedTokenIter = curNode
            }
            if (interSectionResult == InterSectionResult.OUTSIDE_RIGHT) {
                firstTokenToOffset = curNode
                break
            }
        }
        var indexChange = 0
        if (!tokens.isEmpty() && firstChangedTokenIter != -1 && lastChangedTokenIter != -1) {
            tokens.subList(firstChangedTokenIter, lastChangedTokenIter + 1).clear()
            indexChange -= lastChangedTokenIter - firstChangedTokenIter + 1
        }

        if (!newTokenList.isEmpty()) {
            indexChange +=newTokenList.size
            tokens.addAll(beforeFirstChangedTokenIter + 1, newTokenList)
        }

        var offsetStartTime = System.currentTimeMillis()
        if (firstTokenToOffset != -1) {
            firstTokenToOffset += indexChange
            if (firstTokenToOffset >= 0) {
                for (i in firstTokenToOffset until tokens.size) {
                    val data = tokens[i]
                    data.start += offset
                    data.end += offset
                    if (data.type == TokenType.IDENTIFIER)
                        newIdentifiers.add(data.getString())
                }
            }
        }
        tokenIdentifiers = newIdentifiers
        Log.d("Update", "Offset shift: ${(System.currentTimeMillis() - offsetStartTime) / 1000.0}s")
        Log.d("Update", "Update took: ${(System.currentTimeMillis() - startTime) / 1000.0}s")
        Log.d("Update", "Tokens: ${tokens.size}")

//        Log.d("TokenList", toString(tokens))

    }

    private val reservedWordsTrie = Trie()
    private val digitsPattern: Pattern

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
            return ParseResult(Token(TokenType.ERROR, s, position, index), index)
        } else {
            index++;
            while (index < s.length && s[index] != '\n')
                index++
//            addToken(s, R.color.darkula_comment, position, index)

            return ParseResult(Token(TokenType.COMMENT, s, position,index), index + 1)
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
        tokens.add(Token(type, s, begin, end))

//        s.setSpan(
//                ForegroundColorSpan(ContextCompat.getColor(context, colorId)),
//                begin,
//                end,
//                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}