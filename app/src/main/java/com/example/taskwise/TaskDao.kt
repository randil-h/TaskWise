package com.example.taskwise

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TaskDao {
    @Insert
    fun insertTask(task: Task)

    @Delete
    fun delete(user: Task): Int

    @Query("SELECT * FROM task_table")
    fun getAll(): List<Task>
}

