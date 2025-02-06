package com.practicum.sqlitenotebookapp.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns

class DbManager(context: Context) {
    // dbHelper может открывать и закрывать бд
    private val dbHelper = DbHelper(context)
    // db - бд, которую получили после открытия, в ней будут происходить изменения
    private var db: SQLiteDatabase? = null

    fun openDB() {
        // dbHelper открывает БД для записи и считывания
        db = dbHelper.writableDatabase
    }

    fun closeDB() {
        dbHelper.close()
    }

    fun insertIntoTable(title: String, description: String, imageUri: String, time: String) {
        // создаем мапу, где ключ - имя столбца
        val values = ContentValues().apply {
            put(DbNameClass.COLUMN_NAME_TITLE, title)
            put(DbNameClass.COLUMN_NAME_DESCRIPTION, description)
            put(DbNameClass.COLUMN_NAME_IMAGE_URI, imageUri)
            put(DbNameClass.COLUMN_NAME_TIME, time)
        }
        // В таблицу записываем новые данные
        db?.insert(DbNameClass.TABLE_NAME, null, values)
    }

    fun updateNote(id: UInt, title: String, description: String, imageUri: String, time: String) {
        val selection = "${BaseColumns._ID}=$id"
        val values = ContentValues().apply {
            put(DbNameClass.COLUMN_NAME_TITLE, title)
            put(DbNameClass.COLUMN_NAME_DESCRIPTION, description)
            put(DbNameClass.COLUMN_NAME_IMAGE_URI, imageUri)
            put(DbNameClass.COLUMN_NAME_TIME, time)
        }
        // В таблицу записываем новые данные
        db?.update(DbNameClass.TABLE_NAME, values, selection, null)
    }


    fun deleteFromTable(id: String) {
        // удаляем по условия
        val selection = "${BaseColumns._ID}=$id"
        db?.delete(DbNameClass.TABLE_NAME, selection, null)
    }

    // считывает данные с помощью cursor (можно указывать  сортировки)
    fun readTableData(searchText: String) : ArrayList<Note> {
        val dataList = ArrayList<Note>()
        //ищем по заголовку COLUMN_NAME_TITLE совпадение с ? (=arrayOf(searchText))
        val selection = "${DbNameClass.COLUMN_NAME_TITLE} like ?"

        // считываем данные из таблицы с уловием (WHERE title like searchText)
        // % указывает на частичное совпадение
        val cursor = db?.query(DbNameClass.TABLE_NAME, null, selection, arrayOf("%$searchText%"), null, null, null)

        // данные столбца title, desc, uri помещаем в массив
        while (cursor?.moveToNext()!!) {
            val curId = cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID)).toUInt()
            val curTitle = cursor.getString(cursor.getColumnIndexOrThrow(DbNameClass.COLUMN_NAME_TITLE))
            val curDescription = cursor.getString(cursor.getColumnIndexOrThrow(DbNameClass.COLUMN_NAME_DESCRIPTION))
            val curUri = cursor.getString(cursor.getColumnIndexOrThrow(DbNameClass.COLUMN_NAME_IMAGE_URI))
            val curTime = cursor.getString(cursor.getColumnIndexOrThrow(DbNameClass.COLUMN_NAME_TIME))
            dataList.add(Note(curId, curTitle, curDescription, curUri, curTime))
        }

        // обязательно закрыть
        cursor.close()

        return dataList
    }
}