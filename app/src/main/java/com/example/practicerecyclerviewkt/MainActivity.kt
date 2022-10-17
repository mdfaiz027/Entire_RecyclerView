package com.example.practicerecyclerviewkt

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import java.util.*

private lateinit var recyclerView: RecyclerView
private lateinit var recyclerAdapter: RecyclerAdapter
private  var countryList = mutableListOf<String>()
private var displayList = mutableListOf<String>()
private lateinit var swiperefreshLayout: SwipeRefreshLayout

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        countryList.add("India")
        countryList.add("Saudi")
        countryList.add("Dubai")
        countryList.add("Qatar")

        displayList.addAll(countryList)

        recyclerView = findViewById(R.id.rec)
        recyclerAdapter = RecyclerAdapter(displayList)
        recyclerView.adapter = recyclerAdapter

        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        swiperefreshLayout = findViewById(R.id.swiperefresh)
        swiperefreshLayout.setOnRefreshListener {
            displayList.clear()
            displayList.addAll(countryList)
            recyclerView.adapter!!.notifyDataSetChanged()
            swiperefreshLayout.isRefreshing = false
        }
    }

    private  var simpleCallback = object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP.or(ItemTouchHelper.DOWN),ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT)){
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {

            var startPosition = viewHolder.adapterPosition
            var endPosition = target.adapterPosition

            Collections.swap(displayList, startPosition, endPosition)
            recyclerView.adapter?.notifyItemMoved(startPosition, endPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            var position = viewHolder.adapterPosition

            when(direction){
                ItemTouchHelper.LEFT -> {
                    var deletedCountry = displayList.get(position)
                    displayList.removeAt(position)
                    recyclerAdapter.notifyItemRemoved(position)

                    Snackbar.make(recyclerView, "$deletedCountry is deleted", Snackbar.LENGTH_LONG).setAction("Undo", View.OnClickListener {
                        displayList.add(position, deletedCountry)
                        recyclerAdapter.notifyItemInserted(position)
                    }).show()
                }

                ItemTouchHelper.RIGHT -> {
                    var editText = EditText(this@MainActivity)
                    editText.setText(displayList[position])

                    val builder = AlertDialog.Builder(this@MainActivity)
                    builder.setTitle("Update an Item")
                    builder.setCancelable(true)
                    builder.setView(editText)

                    builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                        displayList.clear()
                        displayList.addAll(countryList)
                        recyclerView.adapter!!.notifyDataSetChanged()
                    })

                    builder.setPositiveButton("Update", DialogInterface.OnClickListener { dialog, which ->
                        displayList.set(position, editText.text.toString())
                        recyclerView.adapter!!.notifyItemChanged(position)
                    })
                    builder.show()
                }
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_item, menu)

        var item: MenuItem = menu!!.findItem(R.id.action_search)

        if(item != null){
            var searchView = item.actionView as SearchView

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if(newText!!.isNotEmpty()){
                        displayList.clear()

                        var search = newText.toLowerCase(Locale.getDefault())

                        for(country in countryList){
                            if(country.toLowerCase(Locale.getDefault()).contains(search)){
                                displayList.add(country)
                            }
                            recyclerView.adapter!!.notifyDataSetChanged()
                        }
                    }
                    else{
                        displayList.clear()
                        displayList.addAll(countryList)
                    }
                    return true
                }
            })
        }
        return super.onCreateOptionsMenu(menu)
    }
}
