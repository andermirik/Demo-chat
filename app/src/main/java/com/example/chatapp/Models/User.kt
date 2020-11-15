package com.example.chatapp.Models

class User{
    public var uid : String = ""
    public var username : String = ""
    public var cover : String = ""
    public var profile : String = ""
    public var search : String = ""
    public var status : String = ""
    public var vk : String = ""
    public var website : String = ""

    constructor()
    constructor(
        uid: String,
        username: String,
        cover: String,
        profile: String,
        search: String,
        status: String,
        vk: String,
        website: String
    ) {
        this.uid = uid
        this.username = username
        this.cover = cover
        this.profile = profile
        this.search = search
        this.status = status
        this.vk = vk
        this.website = website
    }

}