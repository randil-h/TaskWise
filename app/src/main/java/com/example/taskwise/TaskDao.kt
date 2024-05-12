package com.example.taskwise

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TaskDao {
    @Insert
    fun insertTask(task: Task)

    @Delete
    fun deleteTask(user: Task): Int

    @Update
    fun updateTask(task: Task)

    @Query("SELECT * FROM task_table")
    fun getAll(): List<Task>

    @Query("SELECT * FROM task_table WHERE id = :taskId")
    fun getTaskById(taskId: Long): Task

    @Query("SELECT * FROM task_table WHERE title LIKE '%' || :searchText || '%' OR description LIKE '%' || :searchText || '%'")
    fun searchTasks(searchText: String): List<Task>
}

