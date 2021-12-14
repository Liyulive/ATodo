package cf.liyu.atodo.adapter;

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import cf.liyu.atodo.R
import cf.liyu.atodo.adapter.util.TodoUtil
import cf.liyu.atodo.fragment.AddFragment
import cf.liyu.atodo.model.TodoItem
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.UpdateListener
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip

class UndoAdapter(
    private val mList: ArrayList<TodoItem>,
    private val manager: FragmentManager
) :

    RecyclerView.Adapter<UndoAdapter.ViewHolder>() {

    var notifyCallBack: NotifyCallBack? = null

    interface NotifyCallBack {
        fun notifyData()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.textview_todo_title)
        val detail: TextView = view.findViewById(R.id.textview_todo_detail)
        val checkBox: CheckBox = view.findViewById(R.id.checkbox_todo)
        val timeChip: Chip = view.findViewById(R.id.chip_todo_time)
        val card: MaterialCardView = view.findViewById(R.id.card_todo)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UndoAdapter.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_todo, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: UndoAdapter.ViewHolder, position: Int) {
        holder.title.text = mList[position].title
        if (mList[position].detail == "") {
            holder.detail.visibility = View.GONE
        } else {
            holder.detail.text = mList[position].detail
            holder.detail.visibility = View.VISIBLE
        }
        if (mList[position].deadline == 0L) {
            holder.timeChip.visibility = View.GONE
        } else {
            holder.timeChip.text =
                mList[position].deadline?.let { TodoUtil.transferLongToDate("yyyy年MM月dd日", it) }
            holder.timeChip.visibility = View.VISIBLE
        }
        holder.checkBox.isChecked = !mList[position].undo!!
        holder.checkBox.setOnClickListener {
            val old = mList[position]
            val change =
                TodoItem(old.user, old.title, old.detail, old.category, old.undo, old.deadline)
            change.undo = !holder.checkBox.isChecked
            change.update(old.objectId, object : UpdateListener() {
                override fun done(p0: BmobException?) {
                    if (p0 == null) {
                        Log.d("mTest", "1")
                        notifyCallBack?.notifyData()
                    }
                }
            })
        }
        holder.card.setOnClickListener {
            val dialog = AddFragment(
                null,
                null,
                mList[position]
            )
            dialog.setCallback(object : AddFragment.ClickCallBack {
                override fun clickConfirm() {
                    notifyCallBack?.notifyData()
                }
            })
            dialog.show(manager, "editTodo")
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun setCallBack(callBack: NotifyCallBack) {
        notifyCallBack = callBack
    }

}