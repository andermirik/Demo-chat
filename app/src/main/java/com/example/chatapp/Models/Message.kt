package com.example.chatapp.Models

class Message{
    public var sender : String = ""
    public var message : String = ""
    public var receiver : String = ""
    public var isseen = false
    public var url : String = ""
    public var message_id : String = ""

    constructor()

    constructor(
        sender: String,
        message: String,
        receiver: String,
        is_seen: Boolean,
        url: String,
        message_id: String
    ) {
        this.sender = sender
        this.message = message
        this.receiver = receiver
        this.isseen = is_seen
        this.url = url
        this.message_id = message_id
    }


}