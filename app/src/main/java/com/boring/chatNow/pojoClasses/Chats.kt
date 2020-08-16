package com.boring.chatNow.pojoClasses

class Chats {
    private var sender: String = ""
    private var receiver: String = ""
    private var message: String = ""
    private var messageID: String = ""
    private var url: String = ""
    private var isSeen: Boolean = false

    constructor()
    constructor(
        sender: String,
        receiver: String,
        message: String,
        messageID: String,
        url: String,
        isSeen: Boolean
    ) {
        this.sender = sender
        this.receiver = receiver
        this.message = message
        this.messageID = messageID
        this.url = url
        this.isSeen = isSeen
    }

    fun getSender(): String?{
        return sender
    }
    fun setSender(sender: String?){
        this.sender = sender!!
    }
    fun getReceiver(): String?{
        return receiver
    }
    fun setReceiver(receiver: String?){
        this.receiver = receiver!!
    }
    fun getMessage(): String?{
        return message
    }
    fun setMessage(message: String?){
        this.message = message!!
    }
    fun getMessageID(): String?{
        return messageID
    }
    fun setMessageID(messageID: String?){
        this.messageID = messageID!!
    }
    fun getUrl(): String?{
        return url
    }
    fun setUrl(url: String?){
        this.url = url!!
    }
    fun getIsSeen(): Boolean{
        return isSeen
    }
    fun setIsSeen(isSeen: Boolean){
        this.isSeen = isSeen
    }


}