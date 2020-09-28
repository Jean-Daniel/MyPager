package com.example.mypager

import androidx.paging.PagingSource
import androidx.room.*

@Entity
class MyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val content: String
)

@Dao
interface MyDao {

    @Query("SELECT * from MyEntity ORDER BY id")
    fun all(): PagingSource<Int, MyEntity>

    @Query("SELECT count(*) FROM MyEntity")
    suspend fun count(): Int

    @Insert
    suspend fun insert(items: List<MyEntity>)
}

@Database(
    version = 1,
    exportSchema = false,
    entities = [MyEntity::class]
)
abstract class MyDatabase : RoomDatabase() {
    abstract val dao: MyDao
}
