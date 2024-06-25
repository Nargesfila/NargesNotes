package com.example.notes

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity(), NoteAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var noNotesTextView: TextView
    private lateinit var addNoteFab: FloatingActionButton
    private lateinit var noteAdapter: NoteAdapter
    private val db = Firebase.firestore
    private val ADD_NOTE_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Notes"

        recyclerView = findViewById(R.id.recyclerView)
        noNotesTextView = findViewById(R.id.noNotesTextView)
        addNoteFab = findViewById(R.id.addNoteFab)

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        noteAdapter = NoteAdapter(emptyList(), this)
        recyclerView.adapter = noteAdapter

        addNoteFab.setOnClickListener {
            val intent = Intent(this, AddNoteActivity::class.java)
            startActivityForResult(intent, ADD_NOTE_REQUEST_CODE)
        }

        loadNotes()
    }

    private fun loadNotes() {
        db.collection("Notes")
            .get()
            .addOnSuccessListener { documents ->
                val noteList = mutableListOf<Note>()
                for (document in documents) {
                    val note = document.toObject(Note::class.java)
                    if (note.title.isNotEmpty() || note.content.isNotEmpty()) {
                        noteList.add(note)
                    }
                }
                noteList.sortByDescending { it.timestamp }
                noteAdapter.updateList(noteList)

                if (noteList.isEmpty()) {
                    noNotesTextView.visibility = View.VISIBLE
                } else {
                    noNotesTextView.visibility = View.GONE
                }
            }
    }

    override fun onItemClick(note: Note) {
        val intent = Intent(this, EditNoteActivity::class.java)
        intent.putExtra("note", note)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        loadNotes()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_NOTE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            loadNotes() 
        }
    }
}
