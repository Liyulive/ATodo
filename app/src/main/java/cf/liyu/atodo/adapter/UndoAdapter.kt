package cf.liyu.atodo.adapter;

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import cf.liyu.atodo.R
import cf.liyu.atodo.adapter.util.TodoUtil
import cf.liyu.atodo.fragment.AddFragment
import cf.liyu.atodo.model.TodoItem
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip

class UndoAdapter(
    val mList: ArrayList<TodoItem>,
    val manager: FragmentManager
) :

    RecyclerView.Adapter<UndoAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //Todo 在这里获取组件
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
        //Todo 通过holder设置事件
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
                TodoUtil.transferLongToDate("yyyy年MM月dd日", mList[position].deadline)
            holder.timeChip.visibility = View.VISIBLE
        }
        holder.checkBox.isChecked = !mList[position].undo
        holder.card.setOnClickListener {
            val dialog = AddFragment(
                mList[position].user,
                mList[position].category,
                mList[position]
            )
            dialog.show(manager, "editTodo")
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

}