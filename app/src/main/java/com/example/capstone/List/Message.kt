package com.example.capstone.List

class Message {
    var message : String? = null
    var senderID : String? = null
    var receiverID : String? = null
    var dateSent : String? = null
    var status : String? = null
    var seen : Boolean = false

    constructor(){}

    constructor(message: String?, senderID: String?, dateSent: String?, receiverID: String?){
        this.message = message
        this.senderID = senderID
        this.dateSent = dateSent
        this.receiverID = receiverID
    }
}