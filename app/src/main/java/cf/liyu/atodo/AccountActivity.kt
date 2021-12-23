package cf.liyu.atodo

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_account.*

class AccountActivity : AppCompatActivity() {
    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        val settings = this.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val editor = settings.edit()
        val text  = settings.getString("sign", "千里之行始于足下")
        text_sign_change.text = text

        button_sign_change.setOnClickListener {
            val signDialog = MaterialAlertDialogBuilder(this).apply {
                setTitle("设置签名")
                setView(R.layout.dialog_edit_text)
                setNegativeButton("取消", null)
                setPositiveButton("确定") { p1, _ ->
                    val input: EditText? =
                        (p1 as AlertDialog).findViewById<EditText>(R.id.dialog_edittext)
                    val sign = input?.text.toString()
                    if (sign == "") {
                        text_sign_change.visibility = View.GONE
                        editor.putString("sign", "")
                        editor.apply()
                    } else {
                        text_sign_change.visibility = View.VISIBLE
                        text_sign_change.text = sign
                        editor.putString("sign", sign)
                        editor.apply()
                    }
                }
            }.show()
            signDialog.findViewById<EditText>(R.id.dialog_edittext)?.setHint("签名")
            signDialog.findViewById<EditText>(R.id.dialog_edittext)?.setText(text)
        }
    }
}