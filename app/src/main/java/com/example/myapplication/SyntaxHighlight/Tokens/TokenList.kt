package com.example.myapplication.SyntaxHighlight.Tokens

import java.util.*

class TokenList: Collection<Token> {
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

    override fun iterator(): Iterator<Token> {
        return TokenListIterator(this, head)
    }

    data class TokenNode(var data: Token, var next: TokenNode? = null, var prev: TokenNode? = null ) {
        override fun toString(): String {
            return "${data.toString()}"
        }
    }
    class TokenListIterator(val tokenList: TokenList, var rawNode: TokenNode?): Iterator<Token> {
        var oneListItem = false
        init {
            oneListItem = tokenList.head == tokenList.tail
        }
        override fun hasNext(): Boolean {
            return oneListItem || (rawNode != null && rawNode?.next != tokenList.head)
        }

        override fun next(): Token {
            oneListItem = false
            val data = rawNode!!.data
            rawNode = rawNode?.next
            return data
        }
    }
    var head: TokenNode? = null
    var tail: TokenNode? = null
    var listSize = 0

    fun insertTail(token: Token) {
        listSize++
        val tokenNode = TokenNode(token)
        if (head == null) {
            tokenNode.next = tokenNode
            tokenNode.prev = tokenNode
            head = tokenNode
            tail = tokenNode
            return
        } else if (head == tail) {
            tokenNode.next = head
            tokenNode.prev = head
            head?.next = tokenNode
            tail = tokenNode
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

        if (node == null) {
            head = list.head
            tail = list.tail
            return
        }
        if (head == null) {
            head = list.head
            tail = list.tail
            return
        } else if (head == tail) {
            head?.next = list.head
            tail = list.tail
            tail?.next = head
            return
        }
        list.tail = node.next
        node.next = list.head
        if (node == tail)
            tail = list.tail
    }

    fun removeNodes(firstNode: TokenNode, lastNode: TokenNode) {
//        if (firstNode == null) {
//            if (lastNode == null) {
//                head = null
//                tail = null
//                return
//            }
//            head = lastNode.next
//            return
//        }
        if (firstNode == head) {
            if (lastNode == tail) {
                head = null
                tail = null
                return
            }
            head = lastNode.next
            head?.prev = tail
            tail?.next = head
        } else if (lastNode == tail) {
            tail = firstNode.prev
            tail?.next = head
            head?.prev = tail
            return
        }

        firstNode.prev = lastNode.next
        lastNode.next?.prev = firstNode.prev
    }
    fun insertAfter(node: TokenNode, token: Token) {

    }
}

