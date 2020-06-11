package ua.vitdmit.codeedit.SyntaxHighlight


import android.content.Context
import android.util.Log
import ua.vitdmit.codeedit.Files.openAssetFile
import ua.vitdmit.codeedit.SyntaxHighlight.LanguageConstants.*
import ua.vitdmit.codeedit.SyntaxHighlight.Tokens.BracketToken
import ua.vitdmit.codeedit.SyntaxHighlight.Tokens.Token
import ua.vitdmit.codeedit.SyntaxHighlight.Tokens.TokenType
import ua.vitdmit.codeedit.Trie
import ua.vitdmit.codeedit.utils.*
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.math.min

class JavaScriptHighlighter(val context: Context): Highlighter() {
    override fun getSuggestions(): List<String> {
        return basicSuggestions + identifiers()
    }

    override fun brackets(): ArrayList<BracketToken> {
        return tokenBrackets
    }

    private lateinit var basicSuggestions: ArrayList<String>
    var tokenIdentifiers: HashSet<String> = HashSet()
    var tokenBrackets: ArrayList<BracketToken> = ArrayList()
    override fun identifiers(): HashSet<String> {
        return tokenIdentifiers
    }


    private fun handleBracketStack(curToken: BracketToken, stack: Stack<BracketToken>, indentationLevel: Int): Int {
        var indent = indentationLevel
        val curChar = curToken.s[curToken.start]
        if (isOpenParentheses(curChar)) {
            indent++
            curToken.indentationLevel = indent
            stack.add(curToken)
        } else if (isClosedParentheses(curChar)) {
            indent--
            if (stack.empty())
                return indent
            val openParenthesesToken = stack.pop()
            openParenthesesToken.matchingBracket = curToken
            curToken.matchingBracket = openParenthesesToken
            curToken.indentationLevel = indent
            val openParentheses = curToken.s[openParenthesesToken.start]
            if (!matchParentheses(openParentheses, curChar))
                Log.d("Parentheses", "Doesn't match")

        }
        return indent
    }
    fun parseFromPosition(
            tokenList: ArrayList<Token>,
            identifiers: HashSet<String>,
            brackets: ArrayList<BracketToken>,
            bracketsStack: Stack<BracketToken>,
            indentationLevel: Int,
            s: CharSequence, index: Int) : Pair<Int, Int> {
        var parseResult: ParseResult = ParseResult()
        var position = index
        var indent = indentationLevel
        if (parentheses.indexOf(s[position]) != -1) {
            val curToken = BracketToken(TokenType.BRACKETS, s, position, position+1)
            indent = handleBracketStack(curToken, bracketsStack, indent)
            brackets.add(curToken)
            parseResult.token = curToken
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
        return Pair(position, indent)
    }
    override fun parse(s: CharSequence) {
        isResultReady = false
        tokens.clear()
        tokenIdentifiers.clear()
        tokenBrackets.clear()
        var position = 0
        var indentationLevel = 0
        val bracketsStack = Stack<BracketToken>()
        while (position < s.length) {
            val result = parseFromPosition(tokens, tokenIdentifiers, tokenBrackets, bracketsStack, indentationLevel, s, position)
            position = result.first
            indentationLevel = result.second
        }
        isResultReady = true

    }
    override fun update(s: CharSequence, start: Int, end: Int, offset: Int, cursor: Int) {
        val startTime = System.currentTimeMillis()
        val newIdentifiers = HashSet<String>()
        val newBrackets = ArrayList<BracketToken>()
        val newBracketsStack = Stack<BracketToken>()
        var indent = 0
        if (s.isEmpty()) {
            tokens.clear()
            return
        }
        var updateStartIndex = start - 1
        var updateEndIndex = end
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
            data.s = s
            val interSectionResult = intersect(data.start, data.end, updateStartIndex, updateEndIndex - offset + 1)
            if (interSectionResult == InterSectionResult.INTERSECTS) {
                firstChangedTokenIter = curNode
                break
            }

            if (interSectionResult == InterSectionResult.OUTSIDE_LEFT) {
                beforeFirstChangedTokenIter = curNode
                if (data.type == TokenType.IDENTIFIER)
                    newIdentifiers.add(data.getString())
                else if (data is BracketToken) {
                    indent = handleBracketStack(data, newBracketsStack, indent)
                    newBrackets.add(data)
                }
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
            val result = parseFromPosition(newTokenList, newIdentifiers, newBrackets, newBracketsStack, indent, s, startIndex)
            startIndex = result.first
            indent = result.second
            if (startIndex == cursor && !newTokenList.isEmpty() && newTokenList.last().type == TokenType.IDENTIFIER)
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
                    data.s = s
                    data.start += offset
                    data.end += offset
                    if (data.type == TokenType.IDENTIFIER)
                        newIdentifiers.add(data.getString())
                    else if (data is BracketToken) {
                        indent = handleBracketStack(data, newBracketsStack, indent)
                        newBrackets.add(data)
                    }
                }
            }
        }
        tokenIdentifiers = newIdentifiers
        tokenBrackets = newBrackets
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
        basicSuggestions = openAssetFile(context, "javascriptmethods.txt")
        for (word in javaScriptReservedWords)
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

    private fun isComment(position: Int, s: CharSequence): Boolean {
        return (s[position] == '/' &&
                (s.charAtSafe(position+1) == '/' || s.charAtSafe(position+1) == '*'))
    }

    private fun isIdentifier(position: Int, s: CharSequence):Boolean {
        return (s[position] == '_' || s[position].isLetter())
    }
    private fun isStringLiteral(position: Int, s: CharSequence): Boolean {
        if (s[position] == '\'' || s[position] == '\"')
            return true
        return false
    }

    private fun parseIdentifier(position: Int, s: CharSequence): ParseResult {
        var index = position
        while (index < s.length && (s[index].isLetterOrDigit() || s[index] == '_'))
            index++
        return ParseResult(Token(TokenType.IDENTIFIER, s, position, index), index)
    }

    private fun parsePreProcessor(position: Int, s: CharSequence): ParseResult {
        var index = position
        while (index < s.length && !s[index].isWhitespace())
            index++
        return ParseResult(Token(TokenType.PREPROCESSOR, s, position, index), index)
    }

    private fun parseComment(position: Int, s: CharSequence): ParseResult {
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
    private fun parseStringLiteral(position: Int, s: CharSequence): ParseResult {
        var index = position + 1;
        val charType = s[position]
        while (index < s.length && s[index] != charType && s[index] != '\n') {
            if (s[index] == '\\')
                index++;
            index++;
        }
        if (s.charAtSafe(index) == charType)
            return ParseResult(Token(TokenType.STRING_LITERAL, s, position, index + 1), index +  1)
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
}