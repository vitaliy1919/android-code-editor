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

