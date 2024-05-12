package com.example.taskwise

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeActivity : AppCompatActivity() {

    private lateinit var taskDao: TaskDao
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var sortBySpinner: Spinner
    private val sortOptions = arrayOf("Title A-Z", "Title Z-A", "Priority Low-High", "Priority High-Low", "Nearest Deadline", "Furthest Deadline")

    private var tasks: List<Task> = emptyList()

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
        taskAdapter = TaskAdapter(tasks) { task ->
            val intent = Intent(this, EditTaskActivity::class.java)
            intent.putExtra("taskId", task.id)
            startActivity(intent)
        }
        recyclerView.adapter = taskAdapter

        loadTasks()

        val addTaskButton = findViewById<FloatingActionButton>(R.id.addTaskButton)
        addTaskButton.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            startActivity(intent)
        }

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // Not used for swipe-to-delete
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val taskToDelete = tasks[position]
                deleteTask(taskToDelete, viewHolder.itemView)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun loadTasks() {
        lifecycleScope.launch(Dispatchers.IO) {
            tasks = taskDao.getAll()
            runOnUiThread {
                taskAdapter.updateTasks(tasks)
            }
        }
    }

    private fun deleteTask(task: Task, view: View) {
        val deletedTask = task.copy()

        lifecycleScope.launch(Dispatchers.IO) {
            taskDao.deleteTask(task)
            withContext(Dispatchers.Main) {
                val snackbar = Snackbar.make(view, "Task deleted", Snackbar.LENGTH_LONG)
                snackbar.setActionTextColor(Color.WHITE)
                snackbar.setAction("Undo") {
                    lifecycleScope.launch(Dispatchers.IO) {
                        taskDao.insertTask(deletedTask)
                        Snackbar.make(view, "Task restored", Snackbar.LENGTH_SHORT).show()
                        loadTasks()
                    }
                }

                // Customize snackbar background and text color
                val snackbarView = snackbar.view
                snackbarView.setBackgroundColor(Color.RED)
                val textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
                textView.setTextColor(Color.BLACK)

                snackbar.show()
                loadTasks()
            }
        }
    }

    private fun sortTasks(selectedOption: String) {
        when (selectedOption) {
            "Title A-Z" -> {
                tasks = tasks.sortedBy { it.title }
            }
            "Title Z-A" -> {
                tasks = tasks.sortedByDescending { it.title }
            }
            "Priority Low-High" -> {
                tasks = tasks.sortedBy { Priority.valueOf(it.priority) }
            }
            "Priority High-Low" -> {
                tasks = tasks.sortedByDescending { Priority.valueOf(it.priority) }
            }
            "Nearest Deadline" -> {
                tasks = tasks.sortedBy { it.deadline }
            }
            "Furthest Deadline" -> {
                tasks = tasks.sortedByDescending { it.deadline }
            }
        }
        taskAdapter.updateTasks(tasks)
    }

}