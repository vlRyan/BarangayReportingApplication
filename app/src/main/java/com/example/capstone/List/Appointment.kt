package com.example.capstone.List

data class Appointment(
    val id: String ?= null,
    val firstName: String ?= null,
    val lastName: String ?= null,
    val purpose: String ?= null,
    val purok: String ?= null,
    val date: String ?= null,
    val time: String ?= null,
    var status: String ?= null,
)