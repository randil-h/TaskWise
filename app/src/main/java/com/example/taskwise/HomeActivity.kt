package com.example.taskwise

import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var taskDao: TaskDao
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var sortBySpinner: Spinner
    private val sortOptions = arrayOf("Title A-Z", "Title Z-A", "Priority Low-High", "Priority High-Low")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        sortBySpinner = findViewById(R.id.sortBySpinner)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortBySpinner.adapter = adapter

        sortBySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedOption = sortOptions[position]
                sortTasks(selectedOption)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle nothing selected
            }
        }

        val taskDatabase = TaskDatabase.getDatabase(applicationContext)
        taskDao = taskDatabase.taskDao()

        recyclerView = findViewById(R.id.taskRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        taskAdapter = TaskAdapter(emptyList())
        recyclerView.adapter = taskAdapter

        loadTasks()

        val addTaskButton = findViewById<Button>(R.id.addTaskButton)
        addTaskButton.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.task_context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                // Handle edit action
                true
            }
            R.id.action_delete -> {
                // Handle delete action
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        loadTasks()
    }

    private fun loadTasks() {
        lifecycleScope.launch(Dispatchers.IO) {
            val tasks = taskDao.getAll()
            runOnUiThread {
                taskAdapter.updateTasks(tasks)
            }
        }
    }

    private fun sortTasks(selectedOption: String) {
        // Implement sorting logic based on the selected option
        // For example, you can update the RecyclerView adapter with sorted data
    }
}