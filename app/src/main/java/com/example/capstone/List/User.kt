package com.example.capstone.List
class User {
    var uid : String? = null
    var fName : String? = null
    var lName : String? = null
    var email : String? = null
    var phoneNum: String? = null

    constructor(){}

    constructor(fName: String?, lName: String?, email: String?, phoneNum: String?, uid: String?){
        this.uid = uid
        this.fName = fName
        this.lName = lName
        this.email = email
        this.phoneNum = phoneNum
    }
}