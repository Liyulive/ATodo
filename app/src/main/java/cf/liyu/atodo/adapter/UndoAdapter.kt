package cf.liyu.atodo.adapter;

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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

    RecyclerView.Adapter<UndoAdapter.ViewHolder>(), Filterable {

    var notifyCallBack: NotifyCallBack? = null
    private var mSourceList: ArrayList<TodoItem> = ArrayList()
    private var mFilterList: ArrayList<TodoItem> = ArrayList()

    init {
        mSourceList = mList
        mFilterList = mList
    }

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
        val mTodo = mFilterList[position]
        holder.title.text = mTodo.title
        if (mTodo.detail == "") {
            holder.detail.visibility = View.GONE
        } else {
            holder.detail.text = mTodo.detail
            holder.detail.visibility = View.VISIBLE
        }
        if (mTodo.deadline == 0L) {
            holder.timeChip.visibility = View.GONE
        } else {
            holder.timeChip.text =
                mTodo.deadline?.let { TodoUtil.transferLongToDate("yyyy年MM月dd日", it) }
            holder.timeChip.visibility = View.VISIBLE
        }
        holder.checkBox.isChecked = !mTodo.undo!!
        holder.checkBox.setOnClickListener {
            val old = mTodo
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
                mTodo
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
        return mFilterList.size
    }

    fun getAllCount(): Int {
        return mSourceList.size
    }

    fun setCallBack(callBack: NotifyCallBack) {
        notifyCallBack = callBack
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            //执行过滤操作
            override fun performFiltering(charSequence: CharSequence): FilterResults? {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    //没有过滤的内容，则使用源数据
                    mFilterList = mSourceList
                } else {
                    val filteredList: ArrayList<TodoItem> = ArrayList()
                    for (todo in mSourceList) {
                        //这里根据需求，添加匹配规则
                        if (todo.title!!.contains(charSequence)) {
                            filteredList.add(todo)
                        }
                    }
                    mFilterList = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = mFilterList
                return filterResults
            }

            //把过滤后的值返回出来
            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults) {
                mFilterList = filterResults.values as ArrayList<TodoItem>
                notifyDataSetChanged()
            }
        }
    }


}