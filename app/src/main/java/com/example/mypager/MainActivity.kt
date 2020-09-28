package com.example.mypager

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.mypager.databinding.ActivityMainBinding
import com.example.mypager.databinding.RowBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<Model>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = Adapter()
        binding.list.adapter = adapter
        lifecycleScope.launchWhenStarted {
            viewModel.items.flow.collect {
                adapter.submitData(it)
            }
        }
    }

    class Model(application: Application) : AndroidViewModel(application) {

        private val database = Room
            .inMemoryDatabaseBuilder(application, MyDatabase::class.java)
            .build()

        init {
            viewModelScope.launch {
                val count = database.dao.count()
                val items = (count until 100).map { MyEntity(0, "My Item $it") }
                if (items.isNotEmpty())
                    database.dao.insert(items)
            }
        }

        val items by lazy {
            Pager(
                config = PagingConfig(
                    pageSize = 5,
                    prefetchDistance = 2,
                    initialLoadSize = 5,
                    enablePlaceholders = true
                )
            ) { database.dao.all() }
        }
    }

    class Holder(val binding: RowBinding) : RecyclerView.ViewHolder(binding.root)

    class Adapter : PagingDataAdapter<MyEntity, Holder>(DiffCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            return Holder(RowBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.binding.text.text = getItem(position)?.content ?: "<placeholder>"
        }

    }

    class DiffCallback : DiffUtil.ItemCallback<MyEntity>() {
        override fun areItemsTheSame(oldItem: MyEntity, newItem: MyEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MyEntity, newItem: MyEntity): Boolean {
            return oldItem.content == newItem.content
        }
    }
}