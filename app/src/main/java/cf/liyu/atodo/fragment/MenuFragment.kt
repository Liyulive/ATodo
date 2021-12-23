package cf.liyu.atodo.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import cf.liyu.atodo.R
import cf.liyu.atodo.model.Category
import cf.liyu.atodo.model.TodoItem
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import cn.bmob.v3.listener.SaveListener
import cn.bmob.v3.listener.UpdateListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_menu.*
import kotlinx.android.synthetic.main.fragment_menu.view.*

class MenuFragment(val category: Category, val sortBy: Int) : BottomSheetDialogFragment() {

    private var clickCallback: ClickCallback? = null
    private var exitConfirm: ExitConfirm? = null
    private var sortCallback: SortCallback? = null

    interface ClickCallback {
        fun clickConfirm()
    }

    interface ExitConfirm {
        fun exitConfirm()
    }

    interface SortCallback {
        fun sortConfirm(sortBy: Int)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_menu, null)
        dialog.setContentView(view)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        initView(view)
        return dialog
    }

    fun initView(root: View) {
        if (tag == "default") {
            root.menu_deleteCategory.isEnabled = false
            root.menu_deleteCategory.text = "不能删除默认列表"
        }
        when (sortBy) {
            0 -> root.menu_textview_sortBy.text = "默认排序"
            1 -> root.menu_textview_sortBy.text = "日期倒序"
            2 -> root.menu_textview_sortBy.text = "日期顺序"
        }
        root.menu_addCategory.setOnClickListener {
            MaterialAlertDialogBuilder(this.requireContext()).apply {
                setTitle("新建任务列表")
                setView(R.layout.dialog_edit_text)
                setNegativeButton("取消", null)
                setPositiveButton("确定") { p1, _ ->
                    val input: EditText? =
                        (p1 as androidx.appcompat.app.AlertDialog).findViewById(R.id.dialog_edittext)
                    val new = Category(category.user, input?.text.toString())
                    new.save(object : SaveListener<String>() {
                        override fun done(p0: String?, p1: BmobException?) {
                            clickCallback?.clickConfirm()
                        }
                    })
                    dismiss()
                }
                show()
            }

        }
        root.menu_renameCategory.setOnClickListener {
            MaterialAlertDialogBuilder(this.requireContext()).apply {
                setTitle("重命名任务列表")
                setView(R.layout.dialog_edit_text)
                setNegativeButton("取消", null)
                setPositiveButton("确定") { p1, _ ->
                    val input: EditText? =
                        (p1 as androidx.appcompat.app.AlertDialog).findViewById(R.id.dialog_edittext)
                    val new = Category(category.user, input?.text.toString())
                    new.update(category.objectId, object : UpdateListener() {
                        override fun done(p1: BmobException?) {
                            clickCallback?.clickConfirm()
                        }
                    })
                    dismiss()
                }
                show()
            }

        }
        root.menu_deleteCategory.setOnClickListener {
            MaterialAlertDialogBuilder(this.requireContext()).apply {
                setTitle("删除任务列表")
                setMessage("删除列表将删除该列表下所有待办，且不可撤销！")
                setNegativeButton("取消", null)
                setPositiveButton("仍要删除") { _,_ ->
                    val delete = Category("", "")
                    delete.objectId = category.objectId
                    delete.delete(object : UpdateListener() {
                        override fun done(p0: BmobException?) {
                            clickCallback?.clickConfirm()
                        }
                    })
                    BmobQuery<TodoItem>().addWhereEqualTo("category", category.objectId)
                        .findObjects(object : FindListener<TodoItem>() {
                            override fun done(p0: MutableList<TodoItem>?, p1: BmobException?) {
                                if (p1 == null) {
                                    p0?.forEach {
                                        val willDelete = TodoItem(null, null, null, null, null, null)
                                        willDelete.objectId = it.objectId
                                        willDelete.delete(object : UpdateListener() {
                                            override fun done(p0: BmobException?) {

                                            }
                                        })
                                    }
                                } else {
                                    Toast.makeText(
                                        this@MenuFragment.requireContext(),
                                        p1.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        })
                    dismiss()
                }
                show()
            }
        }

        root.menu_exit.setOnClickListener {
            MaterialAlertDialogBuilder(this.requireContext()).apply {
                setTitle("退出账号")
                setMessage("确定要退出当前账号吗？")
                setNegativeButton("取消", null)
                setPositiveButton("确定") { _, _ ->
                    this@MenuFragment.dismiss()
                    exitConfirm?.exitConfirm()
                }
                show()
            }
        }

        root.menu_sortBy.setOnClickListener {
            MaterialAlertDialogBuilder(this.requireContext()).apply {
                setTitle("选择排序方式")
                setSingleChoiceItems(arrayOf("默认排序", "日期倒序", "日期顺序"), sortBy) { dialog, which ->
                    sortCallback?.sortConfirm(which)
                    dialog.dismiss()
                }
                setNegativeButton("取消", null)
                show()
            }
            dismiss()
        }
    }

    fun setCallback(callback: ClickCallback) {
        clickCallback = callback
    }

    fun setExitCallback(callback: ExitConfirm) {
        exitConfirm = callback
    }

    fun setSortCallback(callback: SortCallback) {
        sortCallback = callback
    }
}