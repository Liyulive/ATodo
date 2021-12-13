package cf.liyu.atodo.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import cf.liyu.atodo.R
import cf.liyu.atodo.model.Category
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.SaveListener
import cn.bmob.v3.listener.UpdateListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_menu.view.*

class MenuFragment(val category: Category) : BottomSheetDialogFragment() {

    var clickCallback: ClickCallback? = null

    interface ClickCallback {
        fun clickConfirm()
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
            root.menu_deleteCategory.text = "默认列表不能删除"
        }
        root.menu_addCategory.setOnClickListener {
            MaterialAlertDialogBuilder(this.requireContext()).apply {
                setTitle("新建任务列表")
                setView(R.layout.dialog_edit_text)
                setNegativeButton("取消", null)
                setPositiveButton("确定") { p1, _ ->
                    val input: EditText? = (p1 as androidx.appcompat.app.AlertDialog).findViewById(R.id.dialog_edittext)
                    val new = Category(category.user, input?.text.toString())
                    new.save(object : SaveListener<String>() {
                        override fun done(p0: String?, p1: BmobException?) {
                            clickCallback?.clickConfirm()
                        }
                    })
                }
                show()
            }
            dismiss()
        }
        root.menu_renameCategory.setOnClickListener {
            MaterialAlertDialogBuilder(this.requireContext()).apply {
                setTitle("重命名任务列表")
                setView(R.layout.dialog_edit_text)
                setNegativeButton("取消", null)
                setPositiveButton("确定") { p1, _ ->
                    val input: EditText? = (p1 as androidx.appcompat.app.AlertDialog).findViewById(R.id.dialog_edittext)
                    val new = Category(category.user, input?.text.toString())
                    new.update(category.objectId,object : UpdateListener() {
                        override fun done(p1: BmobException?) {
                            clickCallback?.clickConfirm()
                        }
                    })
                }
                show()
            }
            dismiss()
        }
        root.menu_deleteCategory.setOnClickListener {
            val delete = Category("", "")
            delete.objectId = category.objectId
            delete.delete(object : UpdateListener() {
                override fun done(p0: BmobException?) {
                    clickCallback?.clickConfirm()
                }
            })
            dismiss()
        }
    }

    fun setCallback(callback: ClickCallback) {
        clickCallback = callback
    }
}