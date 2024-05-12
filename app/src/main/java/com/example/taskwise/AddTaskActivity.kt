package com.example.taskwise

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddTaskActivity : AppCompatActivity() {

    private lateinit var taskDao: TaskDao
    private lateinit var selectedDateTextView: TextView
    private lateinit var selectedPriority: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_task)

        selectedDateTextView = findViewById(R.id.selectedDateTextView)

        val taskDatabase = TaskDatabase.getDatabase(applicationContext)
        taskDao = taskDatabase.taskDao()

        val priorityRadioGroup = findViewById<RadioGroup>(R.id.priorityRadioGroup)
        priorityRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val radioButton = findViewById<RadioButton>(checkedId)
            selectedPriority = radioButton.text.toString()
            Log.d("AddTaskActivity", "Selected Priority: $selectedPriority")
        }

        val titleEditText = findViewById<TextInputEditText>(R.id.titleEditText)
        val titleTextInputLayout = findViewById<TextInputLayout>(R.id.titleTextInputLayout)

        titleEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Hide the hint when text is typed
                if (!s.isNullOrEmpty()) {
                    titleTextInputLayout.hint = null
                } else {
                    titleTextInputLayout.hint = getString(R.string.task_title)
                }
            }
        })

        val descriptionEditText = findViewById<TextInputEditText>(R.id.descriptionEditText)
        val descriptionTextInputLayout = findViewById<TextInputLayout>(R.id.descriptionTextInputLayout)

        descriptionEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Hide the hint when text is typed
                if (!s.isNullOrEmpty()) {
                    descriptionTextInputLayout.hint = null
                } else {
                    descriptionTextInputLayout.hint = getString(R.string.task_description)
                }
            }
        })
    }

    fun saveTask(view: View) {
        val title = findViewById<EditText>(R.id.titleEditText).text.toString().trim()
        val description = findViewById<EditText>(R.id.descriptionEditText).text.toString().trim()
        val deadline = System.currentTimeMillis()

        val task = Task(title = title, description = description, priority = selectedPriority, deadline = deadline)
        insertTask(task)
    }

    private fun insertTask(task: Task) {
        lifecycleScope.launch(Dispatchers.IO) {
            taskDao.insertTask(task)
            Log.d("AddTaskActivity", "Task inserted: $task")

            withContext(Dispatchers.Main) {
                val rootView = findViewById<View>(android.R.id.content)
                Snackbar.make(rootView, "Task saved", Snackbar.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    fun showDatePicker(view: View) {
        val dateRangePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        dateRangePicker.addOnPositiveButtonClickListener { timestamp ->
            val selectedDate = formatDate(timestamp)
            selectedDateTextView.text = selectedDate
        }

        dateRangePicker.show(supportFragmentManager, "datePicker")
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
