package com.example.raghu.tiger5regulars

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.allyants.boardview.BoardAdapter
import com.allyants.boardview.BoardView
import com.allyants.boardview.SimpleBoardAdapter
import com.example.raghu.tiger5regulars.models.User
import com.example.raghu.tiger5regulars.utilities.Listener
import com.example.raghu.tiger5regulars.utilities.toStringFromat
import com.google.firebase.database.*
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

      /*  playersQuery.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                val user: User? = dataSnapshot.getValue<User>(User::class.java)
               user?.let {
                   if(membersList.size==0 || !membersList.contains(user)){
                       membersList.add(user)
                       partitionTeam(membersList)
                   }
               }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                val user: User? = dataSnapshot.getValue<User>(User::class.java)
                user?.let {
                    if(user.Playing && membersList.contains(user)){
                        membersList.add(user)
                        partitionTeam(membersList)
                    }else if(!user.Playing && membersList.contains(user)) {
                        membersList.remove(user)
                        partitionTeam(membersList)
                    }
                }
            }
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onChildMoved(dataSnapshot: DataSnapshot, prevChildKey: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        })*/


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


    fun check_column1(): Boolean {
        val columnAtIndex1 = boardAdapter.getColumnAtIndex(1)
        when {
            columnAtIndex1.getObjects().size >= 5 -> {
                columnAtIndex1.items_locked = true
                return true
            }
            else -> {
                columnAtIndex1.items_locked = false
                return false
            }
        }
    }

    fun check_column2(): Boolean {
        val columnAtIndex2 = boardAdapter.getColumnAtIndex(2)
        when {
            columnAtIndex2.getObjects().size >= 5 -> {
                columnAtIndex2.items_locked = true
                return true
            }
            else -> {
                columnAtIndex2.items_locked = false
                return false
            }
        }
    }


    override fun onSuccess(dataSnapshot: DataSnapshot?) {
        if (dataSnapshot != null) {
            for (players in dataSnapshot.children) {
                val user = players.getValue(User::class.java)
                val key = players.key
                membersList.removeAll(membersList)
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
        }
    }

    private fun partitionTeam(mem: ArrayList<User>) {

        for (i in 0 until mem.size) {
            val user = mem[i]
            val userid = user.userId
            val date = getCurrentDateTime()
            val dateInString = date.toStringFromat("dd/MM/yyyy")
            val array : Array<String?> = arrayOf(dateInString,userid)
            val hashLong = hash(array)
            user.Name?.let { members.add(it) }
            if (hashLong % 2 == 0L) {
                user.Name?.let { listA.add(it) }
            } else {
                user.Name?.let { listB.add(it) }
            }
        }
    }

    fun hash(values: Array<String?>): Long {
        var result: Long = 17
        for (v in values) result = 37 * result + v.hashCode()
        return result
    }

    private fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }

}
