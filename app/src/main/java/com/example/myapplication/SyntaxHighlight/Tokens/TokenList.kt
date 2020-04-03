package com.example.myapplication.SyntaxHighlight.Tokens

class TokenList {
    data class TokenNode(var data: Token, var next: TokenNode? = null, var prev: TokenNode? = null )
    var head: TokenNode? = null
    var tail: TokenNode? = null

    fun insertTail(token: Token) {
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
        if (head == null || head == tail ) {
           insertTail(token)
            return
        }
        val tokenNode = TokenNode(token, head, head?.next)
        head?.next = tokenNode
    }
    fun insertAfter(node: TokenNode, token: Token) {

    }
}

