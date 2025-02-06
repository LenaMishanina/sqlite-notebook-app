package com.practicum.sqlitenotebookapp.recv

import android.content.Context
import android.content.Intent
import android.provider.BaseColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.practicum.sqlitenotebookapp.EditActivity
import com.practicum.sqlitenotebookapp.R
import com.practicum.sqlitenotebookapp.databinding.ItemNoteBinding
import com.practicum.sqlitenotebookapp.db.DbManager
import com.practicum.sqlitenotebookapp.db.Note

// Вместо RecyclerView.Adapter<NoteAdapter.NoteHolder>()
// можно использовать ListAdapter<String, NoteAdapter.NoteHolder>(DiffCallback())
// при изменении (добавлении) даже одного элемента recycleView полностью перерисовывается,
// когда в другом способе (DiffCallback) обновляются только изменившиеся данные
// ПЕРВЫЙ СПОСОБ СМ НИЖЕ

// передали context, чтобы во ViewHolder можно было обработать переход на другое активити
class NoteAdapter(contextMain: Context) : ListAdapter<Note, NoteAdapter.NoteHolder>(DiffCallback()) {
    private val context = contextMain

    class NoteHolder(item: View, contextView: Context) : RecyclerView.ViewHolder(item) {
        private val binding = ItemNoteBinding.bind(item)
        private val context = contextView

        fun bind(note: Note) {
            binding.tvTitle.text = note.title
            binding.tvTime.text = note.time

            itemView.setOnClickListener {
                val intent = Intent(context, EditActivity::class.java).apply {
                    putExtra(IntentConst.ID_KEY, note.id.toInt())
                    putExtra(IntentConst.TITLE_KEY, note.title)
                    putExtra(IntentConst.DESC_KEY, note.description)
                    putExtra(IntentConst.URI_KEY, note.uri)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteHolder(view, context)
    }

    override fun onBindViewHolder(holder: NoteHolder, position: Int) {
        // getItem (= notes[position]) - спец метод ListAdapter, возвращающий элемент, который должен отрисоваться, из списка
        holder.bind(getItem(position))
    }

    //для удаления через свайп
    fun getSwapManager(dbManager: DbManager): ItemTouchHelper {
        return ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            //завершается, когда передвигаем элемент
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            //завершается, когда свайп завершился
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // Удаление из БД
                    dbManager.deleteFromTable(getItem(position).id.toString())

                    // Удаление из адаптера
                    val newList = currentList.toMutableList()
                    newList.removeAt(position)
                    submitList(newList)
                }
            }
        })
    }

    // проверка на изменения поэлементно
    // Обновление списка (из любого места в коде)
//    *    val newList: List<String> = //... ваш новый список ...
//    *    adapter.submitList(newList)
    class DiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return (oldItem.title == newItem.title) && (oldItem.description == newItem.description) && (oldItem.uri == newItem.uri)
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return (oldItem.title == newItem.title) && (oldItem.description == newItem.description) && (oldItem.uri == newItem.uri)
        }
    }

}


//class NoteAdapter(notes: ArrayList<String>) : RecyclerView.Adapter<NoteAdapter.NoteHolder>() {
//    private val noteList = notes
//
//    class NoteHolder(item: View) : RecyclerView.ViewHolder(item) {
//        private val binding = ItemNoteBinding.bind(item)
//        fun bind(title: String) {
//            binding.tvTitle.text = title
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
//        return NoteHolder(view)
//    }
//
//    override fun getItemCount(): Int {
//        return noteList.size
//    }
//
//    override fun onBindViewHolder(holder: NoteHolder, position: Int) {
//        holder.bind(noteList[position])
//    }
//
//    //после добавления заметки новый адаптер не создается, обновляется старый
//    fun updateAdapter(list: List<String>) {
//        noteList.clear()
//        noteList.addAll(list)
//        notifyItemInserted(noteList.size - 1)
//    }
//}