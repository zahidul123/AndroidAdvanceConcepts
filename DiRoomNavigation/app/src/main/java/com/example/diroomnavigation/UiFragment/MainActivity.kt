package com.example.diroomnavigation.UiFragment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diroomnavigation.Adapters.ListRecyclerviewAdapter
import com.example.diroomnavigation.Model.User
import com.example.diroomnavigation.R
import com.example.diroomnavigation.UiFragment.ViewModelP.MainViewModel
import com.example.diroomnavigation.Utils.Status
import dagger.Module
import dagger.hilt.EntryPoint
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var adapter: ListRecyclerviewAdapter


    private val mainViewModel: MainViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupUI()

        setupObserver()
    }

    private fun setupObserver() {
        mainViewModel.fetchUsers().observe(this, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    progressBar.visibility = View.GONE
                    it.data?.let { users -> renderList(users) }
                    recyclerView.visibility = View.VISIBLE
                }
                Status.LOADING -> {
                    progressBar.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }
                Status.ERROR -> {
                    //Handle Error
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun setupUI() {

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ListRecyclerviewAdapter(arrayListOf())
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                recyclerView.context,
                (recyclerView.layoutManager as LinearLayoutManager).orientation
            )
        )
        recyclerView.adapter = adapter
    }

    private fun renderList(users: List<User>) {

        var recUser = ArrayList<User>()
        var i: Int = 0

        for (myUser: User in users) {
            if (i == 3) {
                break
            }
            recUser.add(myUser)
            i++
        }
        i++
        adapter.addData(recUser)
        adapter.notifyDataSetChanged()


        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1)) {

                    for (j in i until users.size) {

                        if (i % 3 == 0) {
                            i++
                            break
                        }

                        recUser.add(users[j])
                        i++
                    }
                    adapter.addData(recUser)
                    Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
                        override fun run() {
                            progressBar.visibility=View.VISIBLE
                        }
                    },2000)
                    adapter.notifyDataSetChanged()
                    progressBar.visibility=View.GONE
                }


            }
        })

    }

}