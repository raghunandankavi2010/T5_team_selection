package com.example.raghu.tiger5regulars

import android.content.Intent
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), Listener {

    private lateinit var boardView: BoardView
    private lateinit var boardAdapter: BoardAdapter
    private lateinit var membersList: ArrayList<String>
    private lateinit var database: DatabaseReference
    private lateinit var listener: Listener
    private val list = ArrayList<String>(5)
    private val data: ArrayList<SimpleBoardAdapter.SimpleColumn> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if(account==null) {
            val intent = Intent(this@MainActivity,LoginActivity::class.java);
            startActivity(intent)
        }else {
            setContentView(R.layout.activity_main)
            listener = this@MainActivity
            button2.setOnClickListener({ view -> doSomething() })

            boardView = findViewById<BoardView>(R.id.boardView)


            database = FirebaseDatabase.getInstance().reference
            val ref = database.child("Players")
            val playersQuery = ref.orderByChild("Playing").equalTo(true)

            membersList = ArrayList<String>()

            playersQuery.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    (listener as MainActivity).onSuccess(dataSnapshot)

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("MainActivity", "loadPost:onCancelled", databaseError.toException())
                }
            })


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

    }


    fun doSomething() {
        if (!check_column1()) {
            if (boardAdapter.columnCount > 0 && boardAdapter.columns[0].objects.size > 0) {
                val value = boardAdapter.getItemObject(0, 1)
                boardAdapter.addItem(1, 0, value)
                boardAdapter.removeItem(0, 1)
            }
        }
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
        if(dataSnapshot!=null) {
            for (players in dataSnapshot.children) {
                val user = players.getValue(User::class.java)
                val name = user?.Name
                if (name != null) {
                    membersList.add(name)
                }
            }

            data.add(SimpleBoardAdapter.SimpleColumn("Team Members", membersList))
            data.add(SimpleBoardAdapter.SimpleColumn("Team A", list))
            data.add(SimpleBoardAdapter.SimpleColumn("Team B", list))

            boardAdapter = SimpleBoardAdapter(this@MainActivity, data)
            boardView.setAdapter(boardAdapter)
        }
    }



}
