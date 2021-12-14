package cf.liyu.atodo.fragment

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import cf.liyu.atodo.R
import cf.liyu.atodo.adapter.util.TodoUtil
import cf.liyu.atodo.model.Category
import cf.liyu.atodo.model.TodoItem
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.SaveListener
import cn.bmob.v3.listener.UpdateListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.android.synthetic.main.fragment_item_add.*
import kotlinx.android.synthetic.main.fragment_item_add.view.*
import kotlinx.android.synthetic.main.item_todo.view.*
import java.lang.Exception

class AddFragment(val user: String?, val category: Category?, val todoItem: TodoItem?) : BottomSheetDialogFragment() {

    var todoTime: Long = 0
    var clickCallBack: ClickCallBack? = null

    interface ClickCallBack {
        fun clickConfirm()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_item_add, null)
        dialog.setContentView(view)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        initView(view)
        return dialog
    }

    private fun initView(rootView: View) {
        rootView.edittext_add_title.requestFocus()
        rootView.edittext_add_title.addTextChangedListener {
            rootView.button_add_todo.isEnabled = it.toString() != ""
        }
        rootView.chip_add_time.setOnCloseIconClickListener {
            rootView.chip_add_time.text = ""
            rootView.chip_add_time.visibility = View.GONE
            todoTime = 0
        }
        rootView.button_add_detail.setOnClickListener {
            rootView.edittext_add_detail.visibility = View.VISIBLE
        }
        rootView.button_add_time.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker().apply {
                setTitleText("选择截止日期")
                setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
            }.build()
            try {
                picker.addOnPositiveButtonClickListener {
                    rootView.chip_add_time.text = picker.headerText
                    rootView.chip_add_time.visibility = View.VISIBLE
                    todoTime = it
                }
                picker.show(childFragmentManager, "picker")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (tag == "editTodo") {
            rootView.edittext_add_title.setText(todoItem?.title)
            if (todoItem?.detail != "") {
                rootView.edittext_add_detail.setText(todoItem?.detail)
                rootView.edittext_add_detail.visibility = View.VISIBLE
            }
            if (todoItem?.deadline != 0L) {
                rootView.chip_add_time.visibility = View.VISIBLE
                rootView.chip_add_time.text =
                    todoItem!!.deadline?.let { TodoUtil.transferLongToDate("yyyy年MM月dd日", it) }
            }
            rootView.button_add_todo.setOnClickListener {
                val updateItem = TodoItem(
                    null,
                    rootView.edittext_add_title.text.toString(),
                    rootView.edittext_add_detail.text.toString(),
                    null,
                    null,
                    todoTime
                )
                updateItem.update(todoItem.objectId, object : UpdateListener() {
                    override fun done(p0: BmobException?) {
                        if (p0 == null) {
                            Toast.makeText(
                                this@AddFragment.requireContext(),
                                "保存成功",
                                Toast.LENGTH_SHORT
                            ).show()
                            clickCallBack?.clickConfirm()
                            this@AddFragment.dismiss()
                        } else {
                            Log.d("AddFragment","editError:${p0.message}")
                            Toast.makeText(
                                this@AddFragment.requireContext(),
                                "保存失败，${p0.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
            }
        } else if (tag == "addTodo") {
            rootView.button_add_todo.setOnClickListener {
                TodoItem(
                    user,
                    rootView.edittext_add_title.text.toString(),
                    rootView.edittext_add_detail.text.toString(),
                    category?.objectId,
                    true,
                    todoTime
                ).save(object : SaveListener<String>() {
                    override fun done(id: String?, p1: BmobException?) {
                        if (p1 == null) {
                            Toast.makeText(
                                this@AddFragment.requireContext(),
                                "添加成功",
                                Toast.LENGTH_SHORT
                            ).show()
                            clickCallBack?.clickConfirm()
                            this@AddFragment.dismiss()
                        } else {
                            Log.d("AddFragment", "AddError:" + p1.message.toString())
                            Toast.makeText(
                                this@AddFragment.requireContext(),
                                "保存失败，${p1.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
            }
        }
    }

    fun setCallback(callBack: ClickCallBack) {
        clickCallBack = callBack
    }
}