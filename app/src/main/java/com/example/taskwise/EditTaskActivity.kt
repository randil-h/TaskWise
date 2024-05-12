package com.example.taskwise

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditTaskActivity : AppCompatActivity() {
    private lateinit var taskDao: TaskDao
    private lateinit var selectedDateTextView: TextView
    private var selectedDeadline: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.edit_task)

        val taskId = intent.getLongExtra("taskId", -1)
        if (taskId != -1L) {
            loadTaskData(taskId)
        } else {
            Toast.makeText(this, "Invalid Task ID", Toast.LENGTH_SHORT).show()
            finish()
        }

        val taskDatabase = TaskDatabase.getDatabase(applicationContext)
        taskDao = taskDatabase.taskDao()

        selectedDateTextView = findViewById(R.id.selectedDateTextView)
    }

    private fun loadTaskData(taskId: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            val task = taskDao.getTaskById(taskId)
            withContext(Dispatchers.Main) {
                if (task != null) {
                    populateViewsWithTaskData(task)
                } else {
                    Toast.makeText(this@EditTaskActivity, "Task not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun populateViewsWithTaskData(task: Task) {
        val titleEditText = findViewById<EditText>(R.id.titleEditText)
        titleEditText.setText(task.title)

        val descriptionEditText = findViewById<EditText>(R.id.descriptionEditText)
        descriptionEditText.setText(task.description)

        val priorityRadioGroup = findViewById<RadioGroup>(R.id.priorityRadioGroup)
        when (task.priority) {
            Priority.LOW.toString() -> priorityRadioGroup.check(R.id.lowPriorityRadioButton)
            Priority.MEDIUM.toString() -> priorityRadioGroup.check(R.id.mediumPriorityRadioButton)
            Priority.HIGH.toString() -> priorityRadioGroup.check(R.id.highPriorityRadioButton)
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        selectedDateTextView.text = dateFormat.format(Date(task.deadline))
        selectedDeadline = task.deadline
    }

    fun showDatePicker(view: View) {
        val dateRangePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setSelection(selectedDeadline)
            .build()

        dateRangePicker.addOnPositiveButtonClickListener { timestamp ->
            val selectedDate = formatDate(timestamp)
            selectedDateTextView.text = selectedDate
            selectedDeadline = timestamp
        }

        dateRangePicker.show(supportFragmentManager, "datePicker")
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun saveEditedTask(view: View) {
        updateTask()
    }

    private fun updateTask() {
        val taskId = intent.getLongExtra("taskId", -1)
        if (taskId != -1L) {
            lifecycleScope.launch(Dispatchers.IO) {
                val titleEditText = findViewById<EditText>(R.id.titleEditText)
                val descriptionEditText = findViewById<EditText>(R.id.descriptionEditText)
                val priorityRadioGroup = findViewById<RadioGroup>(R.id.priorityRadioGroup)
                val selectedPriority = when (priorityRadioGroup.checkedRadioButtonId) {
                    R.id.lowPriorityRadioButton -> Priority.LOW
                    R.id.mediumPriorityRadioButton -> Priority.MEDIUM
                    R.id.highPriorityRadioButton -> Priority.HIGH
                    else -> Priority.LOW
                }

                val task = Task(
                    id = taskId,
                    title = titleEditText.text.toString(),
                    description = descriptionEditText.text.toString(),
                    priority = selectedPriority.toString(),
                    deadline = selectedDeadline
                )

                taskDao.updateTask(task)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditTaskActivity, "Task updated", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}

