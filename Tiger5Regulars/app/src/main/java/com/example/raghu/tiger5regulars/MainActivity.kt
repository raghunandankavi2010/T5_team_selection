package com.example.raghu.tiger5regulars

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.allyants.boardview.SimpleBoardAdapter
import com.allyants.boardview.BoardView
import java.util.*
import android.view.MotionEvent
import android.view.View
import com.allyants.boardview.BoardAdapter
import com.example.raghu.tiger5regulars.R.id.boardView
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    lateinit var boardView:BoardView
    lateinit var  boardAdapter:BoardAdapter
    lateinit var  membersList:ArrayList<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button2.setOnClickListener({view -> doSomething()})

        boardView = findViewById(R.id.boardView) as BoardView
        val data:ArrayList<SimpleBoardAdapter.SimpleColumn> = ArrayList<SimpleBoardAdapter.SimpleColumn>()
        membersList = ArrayList<String>()
        membersList.add("Mithil")
        membersList.add("Rahul")
        membersList.add("Raghunandan")
        membersList.add("Prabhav")
        membersList.add("Raghu Amaresh")
        membersList.add("Rishi")
        membersList.add("Govind")
        membersList.add("Suhas")
        membersList.add("Ujji")
        membersList.add("Shettar")
        val list = ArrayList<String>(5)


        data.add(SimpleBoardAdapter.SimpleColumn("Team Members", membersList))
        data.add(SimpleBoardAdapter.SimpleColumn("Team A", list))
        data.add(SimpleBoardAdapter.SimpleColumn("Team B", list))

        boardAdapter = SimpleBoardAdapter(this, data)
        boardView.setAdapter(boardAdapter)

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
                    columnAtIndex1.getObjects().size>=5 -> columnAtIndex1.items_locked = true
                    else -> columnAtIndex1.items_locked = false
                }

                val columnAtIndex2 = boardAdapter.getColumnAtIndex(2)
                when {
                    columnAtIndex2.getObjects().size>=5 -> columnAtIndex2.items_locked = true
                    else -> columnAtIndex2.items_locked = false
                }


            }
        })

    }

    fun doSomething(){
        if(!check_column1()) {
            if(boardAdapter.columnCount>0 && boardAdapter.columns[0].objects.size>0) {
                val value = boardAdapter.getItemObject(0, 1)
                boardAdapter.addItem(1, 0, value)
                boardAdapter.removeItem(0, 1)
            }
        }
    }

    fun check_column1():Boolean{
        val columnAtIndex1 = boardAdapter.getColumnAtIndex(1)
        when {
            columnAtIndex1.getObjects().size>=5 -> {
                columnAtIndex1.items_locked = true
                return true
            }
            else -> {
                columnAtIndex1.items_locked = false
                return false
            }
        }
    }

    fun check_column2():Boolean{
        val columnAtIndex2 = boardAdapter.getColumnAtIndex(2)
        when {
            columnAtIndex2.getObjects().size>=5 -> {
                columnAtIndex2.items_locked = true
                return true
            }
            else -> {
                columnAtIndex2.items_locked = false
                return false
            }
        }
    }

}
