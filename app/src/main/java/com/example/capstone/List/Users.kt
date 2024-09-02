package com.example.capstone.List

class Users {
    var uid : String? = null
    var email : String? = null
    var code: String? = null

    constructor(){}

    constructor(email: String?, code: String?, uid: String?){
        this.uid = uid
        this.email = email
        this.code = code
    }
}