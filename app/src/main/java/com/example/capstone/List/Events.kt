package com.example.capstone.List

import android.os.Parcel
import android.os.Parcelable

data class Events(
    var eventDate: String ?= null,
    var eventTitle: String ?= null,
    var eventPlace: String ?= null,
    var eventDescription: String ?= null,
    var eventTime: String ?= null,
    val imageUrl: String = ""
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(eventDate)
        parcel.writeString(eventTitle)
        parcel.writeString(eventPlace)
        parcel.writeString(eventDescription)
        parcel.writeString(eventTime)
        parcel.writeString(imageUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Events> {
        override fun createFromParcel(parcel: Parcel): Events {
            return Events(parcel)
        }

        override fun newArray(size: Int): Array<Events?> {
            return arrayOfNulls(size)
        }
    }
}