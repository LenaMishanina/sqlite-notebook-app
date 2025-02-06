package com.practicum.sqlitenotebookapp.db

data class Note(
    val id: UInt,
    val title: String,
    val description: String,
    val uri: String,
    val time: String
)