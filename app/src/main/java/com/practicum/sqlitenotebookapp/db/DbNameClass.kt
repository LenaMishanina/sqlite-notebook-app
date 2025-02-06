package com.practicum.sqlitenotebookapp.db

import android.provider.BaseColumns

object DbNameClass {
    const val DATABASE_VERSION = 4
    const val DATABASE_NAME = "db_notebook.db"

    const val TABLE_NAME = "note"
    const val COLUMN_NAME_TITLE = "title"
    const val COLUMN_NAME_DESCRIPTION = "description"
    const val COLUMN_NAME_IMAGE_URI = "uri"
    const val COLUMN_NAME_TIME = "time"

    const val QUERY_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY," +
            "$COLUMN_NAME_TITLE TEXT," +
            "$COLUMN_NAME_DESCRIPTION TEXT," +
            "$COLUMN_NAME_IMAGE_URI TEXT," +
            "$COLUMN_NAME_TIME TEXT)"
    const val QUERY_DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
}