package com.example.myapplication.SyntaxHighlight.Tokens

import android.util.Log
import java.util.*
import kotlin.NoSuchElementException

open class TokenList: Collection<Token> {
    override fun toString(): String {
        val iter = iterator()
        var str = "["
        while (iter.hasNext()) {
            str += iter.next().toString() + ", "
        }
        str += "]"
        return str
    }
    fun toString(sequence: CharSequence): String {
        val iter = iterator()
        var str = "[\n"
        while (iter.hasNext()) {
            str += iter.next().toString(sequence) + "\n"
        }
        str += "]"
        return str
    }

    override val size: Int
        get() = 1000

    override fun contains(element: Token): Boolean {
        return true
//        TODO("Not yet implemented")
    }

    override fun containsAll(elements: Collection<Token>): Boolean {
        return true
//        TODO("Not yet implemented")
    }

    override fun isEmpty(): Boolean {
        return head == null
    }

    override fun iterator(): TokenListIterator {
        return TokenListIterator(this, head)
    }

    open fun iterator(node: TokenNode?): TokenListIterator {
        return TokenListIterator(this, node)
    }

    open fun clear() {
        head = null
        tail = null
    }

    data class TokenNode(var data: Token, var next: TokenNode? = null, var prev: TokenNode? = null ) {
        override fun toString(): String {
            return "${data.toString()}"
        }
    }
    class TokenListIterator(val tokenList: TokenList, var rawNode: TokenNode?): Iterator<Token> {
        var oneListItem = false
        var firstStep = true

        init {
            firstStep = (rawNode == tokenList.head)
        }
        fun getNode(): TokenNode? {
            return rawNode
        }
        override fun hasNext(): Boolean {
            return rawNode != null && (firstStep || rawNode != tokenList.head)
        }


        override fun next(): Token {
            if (!hasNext())
                throw NoSuchElementException("No element")
            firstStep = false
            val data = rawNode!!.data
            rawNode = rawNode?.next
            return data
        }
    }
    var head: TokenNode? = null
    var tail: TokenNode? = null
    var listSize = 0

    fun insertTail(token: Token) {
        val tokenNode = TokenNode(token)
        if (head == null) {
            tokenNode.next = tokenNode
            tokenNode.prev = tokenNode
            head = tokenNode
            tail = tokenNode
            return
        } else if (head == tail) {
            tail = tokenNode
            tail?.next = head
            tail?.prev = head
            head?.next = tail
            head?.prev = tail
            return
        }
        tail?.next = tokenNode
        tokenNode.prev = tail
        tokenNode.next = head
        tail = tokenNode
    }

    fun insertAfterHead(token: Token) {
        listSize++
        if (head == null || head == tail ) {
           insertTail(token)
            return
        }
        val tokenNode = TokenNode(token, head, head?.next)
        head?.next = tokenNode
    }

    fun insertTokenListAfter(node: TokenNode?, list: TokenList) {
        Log.d("TokenList", "insertList")

        if (head == null) {
            head = list.head
            tail = list.tail
            check()
            return
        } 
        if (node == null) {
            head!!.prev = list.tail
            list.tail!!.next = head
            head = list.head
            head!!.prev = tail
            tail!!.next = head
            check()
            return
        }
        if (node == tail) {
            list.head!!.prev = tail
            tail!!.next = list.head
            tail = list.tail
            tail!!.next = head
            head!!.prev = tail
            check()
            return
        }
        node.next!!.prev = list.tail
        list.tail!!.next = node.next
        node.next = list.head
        list.head!!.prev = node
        check()
    }
    fun check() {
        var iter = head
        while (iter != null) {
            if (iter != iter.next?.prev)
                Log.d("TokenList", "Trouble")
            iter = iter.next
            if (iter == head)
                break
        }
    }

    fun removeNodes(firstNode: TokenNode, lastNode: TokenNode) {
        Log.d("TokenList", "removeNodes")
        if (firstNode == head) {
            if (lastNode == tail) {
                head = null
                tail = null
                check()
                return
            }
            head = lastNode.next
            head?.prev = tail
            tail?.next = head
            check()
            return
        } else if (lastNode == tail) {
            tail = firstNode.prev
            tail?.next = head
            head?.prev = tail
            check()
            return
        }
        firstNode.prev?.next = lastNode.next
        lastNode.next?.prev = firstNode.prev

//        firstNode.prev = lastNode.next
//        lastNode.next?.prev = firstNode.prev
        check()

    }
    fun insertAfter(node: TokenNode, token: Token) {

    }
}

