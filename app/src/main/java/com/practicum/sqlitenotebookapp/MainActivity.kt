package com.practicum.sqlitenotebookapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.practicum.sqlitenotebookapp.databinding.ActivityMainBinding
import com.practicum.sqlitenotebookapp.db.DbManager
import com.practicum.sqlitenotebookapp.db.Note
import com.practicum.sqlitenotebookapp.recv.NoteAdapter

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var editLauncher: ActivityResultLauncher<Intent>
    private val adapter = NoteAdapter(this)
    private val dbManager = DbManager(this)
    private var notes = ArrayList<Note>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        initSearchView()

        editLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            Log.d("MyLog", "from EditActivity")
            //код запускается после перехода с EditActivity на MainActivity
        }

        binding.btnAddNote.setOnClickListener {
            Log.d("MyLog", "btnAddNote clicked")
            val intent = Intent(this, EditActivity::class.java)
            editLauncher.launch(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        dbManager.openDB()
        fillAdapter()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbManager.closeDB()
    }

    private fun init() = with(binding) {
        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)// по вертикали
        recyclerView.adapter = adapter // устанавливаем адаптер для данного recycleView

        //для удаления элементов через свайп (реализация в NoteAdapter)
        val swapManager = adapter.getSwapManager(dbManager)
        swapManager.attachToRecyclerView(recyclerView)
    }

    private fun fillAdapter() {
        notes = dbManager.readTableData("")
        //submitList обновляет данные в адаптере
        adapter.submitList(notes)

        binding.tvEmptyNotebook.visibility = if (notes.isEmpty()) View.VISIBLE else View.GONE
    }

    //поиск
    private fun initSearchView() = with(binding) {
        //отслеживает каждое изменения в поиске
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            //запускается при нажатии на лупу в клавиатуре
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            //запускается при каждом изменении в веденном тексте
            override fun onQueryTextChange(newText: String?): Boolean {
                notes = dbManager.readTableData(newText!!)
                //submitList обновляет данные в адаптере
                adapter.submitList(notes)
                return true
            }
        })
    }
}