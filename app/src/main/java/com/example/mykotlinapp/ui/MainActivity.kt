package com.example.mykotlinapp.ui
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mykotlinapp.R
import com.example.mykotlinapp.database.DatabaseHelper
import com.example.mykotlinapp.database.User
import com.example.mykotlinapp.adapter.UserAdapter
import com.example.mykotlinapp.provider.UserContentProvider
import android.content.ContentValues
import android.widget.EditText
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Toast



class MainActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var saveButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter
    private var users = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nameInput = findViewById(R.id.nameInput)
        emailInput = findViewById(R.id.emailInput)
        saveButton = findViewById(R.id.saveButton)
        recyclerView = findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = UserAdapter(users)
        recyclerView.adapter = adapter

        saveButton.setOnClickListener { saveUser() }
        loadUsers()
    }

    private fun saveUser() {
        val name = nameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please enter both name and email", Toast.LENGTH_SHORT).show()
            return
        }

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_NAME, name)
            put(DatabaseHelper.COLUMN_EMAIL, email)
        }

        val newUri = contentResolver.insert(UserContentProvider.CONTENT_URI, values)

        if (newUri != null) {
            Toast.makeText(this, "User added", Toast.LENGTH_SHORT).show()
            loadUsers()
        } else {
            Toast.makeText(this, "Failed to add user", Toast.LENGTH_SHORT).show()
        }
    }


    private fun loadUsers() {
        users.clear()
        val cursor = contentResolver.query(UserContentProvider.CONTENT_URI, null, null, null, null)
        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID))
                val name = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME))
                val email = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL))
                users.add(User(id, name, email))
            }
        }
        adapter.notifyDataSetChanged()
    }
}
