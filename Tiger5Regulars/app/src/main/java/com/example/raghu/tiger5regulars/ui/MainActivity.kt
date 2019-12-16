package com.example.raghu.tiger5regulars.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.allyants.boardview.BoardAdapter
import com.allyants.boardview.BoardView
import com.allyants.boardview.SimpleBoardAdapter
import com.example.raghu.tiger5regulars.R
import com.example.raghu.tiger5regulars.models.User
import com.example.raghu.tiger5regulars.utilities.Listener
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), Listener {

    private lateinit var boardView: BoardView
    private lateinit var boardAdapter: BoardAdapter
    private var membersList: ArrayList<User> = ArrayList(10)
    private var members: ArrayList<String> = ArrayList(10)
    private lateinit var database: DatabaseReference
    private lateinit var listener: Listener
    private val listA = ArrayList<String>(5)
    private val listB = ArrayList<String>(5)
    private val data: ArrayList<SimpleBoardAdapter.SimpleColumn> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listener = this@MainActivity

        setSupportActionBar(toolbar)

        supportActionBar?.let {
            title = "Team Players"
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

        boardView = findViewById(R.id.boardView)
        database = FirebaseDatabase.getInstance().reference
        val ref = database.child("Players")
        val playersQuery = ref.orderByChild("Playing").equalTo(true)

        playersQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                (listener as MainActivity).onSuccess(dataSnapshot)

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("MainActivity", "loadPost:onCancelled", databaseError.toException())
            }
        })


        boardView.setOnItemClickListener { v, column_pos, item_pos ->

            if(membersList.size>0 && column_pos==0){

                val userId = membersList[item_pos].userId
                val intent = Intent(this@MainActivity, UserDetails::class.java)
                intent.putExtra("userid",userId)
                startActivity(intent)
            }
        }

        boardView.setOnDragItemListener(object : BoardView.DragItemStartCallback {
            override fun startDrag(view: View, startItemPos: Int, startColumnPos: Int) {

            }

            override fun changedPosition(view: View, startItemPos: Int, startColumnPos: Int, newItemPos: Int, newColumnPos: Int) {

            }

            override fun dragging(itemView: View, event: MotionEvent) {

            }

            override fun endDrag(view: View, startItemPos: Int, startColumnPos: Int, endItemPos: Int, endColumnPos: Int) {

                val columnAtIndex1 = boardAdapter.getColumnAtIndex(1)
                when {
                    columnAtIndex1.getObjects().size >= 5 -> columnAtIndex1.items_locked = true
                    else -> columnAtIndex1.items_locked = false
                }

                val columnAtIndex2 = boardAdapter.getColumnAtIndex(2)
                when {
                    columnAtIndex2.getObjects().size >= 5 -> columnAtIndex2.items_locked = true
                    else -> columnAtIndex2.items_locked = false
                }


            }
        })
    }

    override fun onSuccess(dataSnapshot: DataSnapshot?) {
        if (dataSnapshot != null) {
            if(membersList.size>0){
                membersList.removeAll(membersList)
                members.removeAll(members)
                listA.removeAll(listA)
                listB.removeAll(listB)
                data.removeAll(data)
            }
            for (players in dataSnapshot.children) {
                val user = players.getValue(User::class.java)
                val key = players.key
                key?.let { user?.userId = it }
                if (user != null) {
                    membersList.add(user)
                }
            }


            partitionTeam(membersList)
            data.add(SimpleBoardAdapter.SimpleColumn("Team Members", members))
            data.add(SimpleBoardAdapter.SimpleColumn("Team A", listA))
            data.add(SimpleBoardAdapter.SimpleColumn("Team B", listB))

            boardAdapter = SimpleBoardAdapter(this@MainActivity, data)
            boardView.setAdapter(boardAdapter)
            val columnAtIndex1 = boardAdapter.getColumnAtIndex(1)
            val columnAtIndex2 = boardAdapter.getColumnAtIndex(2)
            columnAtIndex2.items_locked = true
            columnAtIndex1.items_locked = true
        }
    }

    private fun partitionTeam(mem: ArrayList<User>) {

        val hashCodeComparator = Comparator { user1: User, user2: User -> user1.hashCode() - user2.hashCode() }

        Collections.sort(mem,hashCodeComparator)
        Log.i("Size",""+mem.size)
        for (i in 0 until mem.size) {
            val user = mem[i]
            Log.i("Name",""+user.Name)
            user.Name?.let { members.add(it) }
            if (i % 2 == 0) {
                user.Name?.let { listA.add(it) }
            } else {
                user.Name?.let { listB.add(it) }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
           finish()
        }
        return super.onOptionsItemSelected(item)
    }

}
