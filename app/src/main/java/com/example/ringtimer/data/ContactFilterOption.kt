package com.example.ringtimer.data

data class ContactFilterOption(
    val displayName: String,
    val numbers: List<String>   // grouped, in case a contact has 2+ numbers
)