package com.boring.chatNow.pojoClasses

class Data
{
    private var user : String = ""
    private var icon = 0
    private var body : String = ""
    private var title : String = ""
    private var send : String = ""

    constructor(){}

    constructor(user: String, icon: Int, body: String, title: String, send: String) {
        this.user = user
        this.icon = icon
        this.body = body
        this.title = title
        this.send = send
    }

    fun getUser(): String?
    {
        return user
    }
    fun setUser(user: String)
    {
        this.user = user
    }
    fun getIcon(): Int
    {
        return icon
    }
    fun setIcon(icon: Int)
    {
        this.icon = icon
    }
    fun getBody(): String?
    {
        return body
    }
    fun setBody(body: String)
    {
        this.body = body
    }
    fun getTitle(): String?
    {
        return title
    }
    fun setTitle(title: String)
    {
        this.title = title
    }
    fun getSend(): String?
    {
        return send
    }
    fun setSend(send: String)
    {
        this.send = send
    }


}