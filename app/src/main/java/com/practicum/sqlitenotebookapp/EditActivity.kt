package com.practicum.sqlitenotebookapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.practicum.sqlitenotebookapp.databinding.ActivityEditBinding
import com.practicum.sqlitenotebookapp.db.DbManager
import com.practicum.sqlitenotebookapp.recv.IntentConst
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditBinding
    private val dbManager = DbManager(this)
    private lateinit var launcherImage: ActivityResultLauncher<Intent>
    private var tempImageUri = "empty"
    private var id = 0
    private var isEditState = false //true если есть extra(то есть перешли на активити нажав на элемент)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("MyLog", "EditActivity create")

        // Восстановление состояния картинки из Bundle при повороте экрана
        savedInstanceState?.let {
            tempImageUri = it.getString("tempImageUri").toString()
            if (tempImageUri != "empty") {
                binding.apply {
                    imageView.setImageURI(Uri.parse(tempImageUri))
                    imageLayout.visibility = View.VISIBLE
                    btnAddImage.visibility = View.GONE
                }
            }
        }

        launcherImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK && it.data != null) {
                val imageUri = it.data?.data
                if (imageUri != null) {
                    try {
                        // Предоставляем постоянное разрешение на чтение URI в MainActivity
                        contentResolver.takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        binding.imageView.setImageURI(imageUri)
                        tempImageUri = imageUri.toString()
                    } catch (e: SecurityException) {
                        Log.e("MyLog", "SecurityException: ", e)
                    }
                }
            }
        }

        initBtnImage()
        initBtnAccept()
        initBtnOnOffEditForm()
        initIntents()

        // обработка нажатия на нижнем меню на back
        onBackPressedDispatcher.addCallback(this /* lifecycle owner */, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Back is pressed... Finishing the activity
                finish()
            }
        })

    }

    override fun onResume() {
        super.onResume()
        Log.d("MyLog", "EditActivity resume")
        dbManager.openDB()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MyLog", "EditActivity destroy")
        dbManager.closeDB()
    }

    private fun initBtnAccept() = with(binding) {
        btnAccept.setOnClickListener {
            val title = edTitle.text.toString()
            val description = edDescription.text.toString()

            Log.d("MyLog", "isEditState $isEditState, id $id")

            if (title != "" && description != "") {
                if (isEditState) {
                    dbManager.updateNote(id.toUInt(), title, description, tempImageUri, getCurrentTime())
                } else {
                    dbManager.insertIntoTable(title, description, tempImageUri, getCurrentTime())
                }
            }

            finish()
        }
    }

    private fun initBtnImage() = with(binding) {
        btnAddImage.setOnClickListener {
            imageLayout.visibility = View.VISIBLE
            btnAddImage.visibility = View.GONE
        }

        btnEditImage.setOnClickListener {
            // Intent.ACTION_PICK - временная ссылка
            // Intent.ACTION_OPEN_DOCUMENT, takePersistableUriPermission - постоянная ссылка
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "image/*"
            launcherImage.launch(intent)
        }

        btnDeleteImage.setOnClickListener {
            tempImageUri = "empty"
            imageLayout.visibility = View.GONE
            btnAddImage.visibility = View.VISIBLE
        }
    }

    private fun initBtnOnOffEditForm() = with(binding) {
        btnEditForm.setOnClickListener {
            edTitle.isEnabled = true
            edDescription.isEnabled = true
            btnEditForm.visibility = View.GONE
            btnEditImage.visibility = View.VISIBLE
            btnDeleteImage.visibility = View.VISIBLE
            if (tempImageUri == "empty") btnAddImage.visibility = View.VISIBLE
        }
    }

    private fun initIntents() = with(binding) {
        val intent = intent

        // В какой момент intent может быть null
        if (intent != null) {
            if (intent.hasExtra(IntentConst.TITLE_KEY)) {
                isEditState = true
                edTitle.isEnabled = false
                edDescription.isEnabled = false
                btnEditForm.visibility = View.VISIBLE
                btnAddImage.visibility = View.GONE

                id = intent.getIntExtra(IntentConst.ID_KEY, 0)
                edTitle.setText(intent.getStringExtra(IntentConst.TITLE_KEY))
                edDescription.setText(intent.getStringExtra(IntentConst.DESC_KEY))

                val uriString = intent.getStringExtra(IntentConst.URI_KEY)
                if (uriString != null && uriString != "empty") {
                    try {
                        val imageUri = Uri.parse(uriString)
                        binding.imageView.setImageURI(imageUri)
                        tempImageUri = uriString

                        imageLayout.visibility = View.VISIBLE
                        btnEditImage.visibility = View.GONE
                        btnDeleteImage.visibility = View.GONE
                    } catch (e: Exception) {
                        Log.e("MyLog", "Invalid URI: $uriString", e)
                    }
                }
            }
        }
    }

    private fun getCurrentTime(): String {
        val time = Calendar.getInstance().time
        val formatter = SimpleDateFormat("dd-MM-yy HH:mm", Locale.getDefault())
        return formatter.format(time)
    }

    // при поворте экрана, чтобы картинка оставалась на экране
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("tempImageUri", tempImageUri)
    }

}