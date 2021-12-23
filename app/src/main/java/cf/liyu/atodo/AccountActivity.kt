package cf.liyu.atodo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import cf.liyu.atodo.model.User
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.UpdateListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_account.*

class AccountActivity : AppCompatActivity() {
    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        val userId = intent.getStringExtra("userId")
        Log.d("AccountActivity", userId.toString())
        val settings = this.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val editor = settings.edit()
        val signText  = settings.getString("sign", "")
        val subTitleText = settings.getString("subtitle", "")
        if (signText == "") {
            text_sign_change.visibility = View.GONE
        } else {
            text_sign_change.visibility = View.VISIBLE
            text_sign_change.text = signText
        }
        if (subTitleText == "") {
            text_subtitle_change.visibility = View.GONE
        } else {
            text_subtitle_change.visibility = View.VISIBLE
            text_subtitle_change.text = subTitleText
        }

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
            signDialog.findViewById<EditText>(R.id.dialog_edittext)?.setText(signText)
        }

        button_password_change.setOnClickListener {
            MaterialAlertDialogBuilder(this).apply {
                setTitle("修改密码")
                setView(R.layout.dialog_edit_text)
                setNegativeButton("取消", null)
                setPositiveButton("确定") { p1, _ ->
                    val input: EditText? =
                        (p1 as AlertDialog).findViewById<EditText>(R.id.dialog_edittext)
                    val pass = input?.text.toString()
                    if (pass == "") {
                        Toast.makeText(this@AccountActivity, "密码不能为空", Toast.LENGTH_SHORT).show()
                    } else {
                        val new = User(null, pass)
                        new.update(userId, object : UpdateListener() {
                            override fun done(p0: BmobException?) {
                                if (p0 == null) {
                                    Toast.makeText(this@AccountActivity, "修改成功", Toast.LENGTH_SHORT)
                                        .show()
                                } else {
                                    Toast.makeText(this@AccountActivity, p0.message, Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        })
                    }
                }
                show()
            }
        }

        button_subtitle_change.setOnClickListener {
            val signDialog = MaterialAlertDialogBuilder(this).apply {
                setTitle("设置副标题")
                setView(R.layout.dialog_edit_text)
                setNegativeButton("取消", null)
                setPositiveButton("确定") { p1, _ ->
                    val input: EditText? =
                        (p1 as AlertDialog).findViewById<EditText>(R.id.dialog_edittext)
                    val sign = input?.text.toString()
                    if (sign == "") {
                        text_subtitle_change.visibility = View.GONE
                        editor.putString("subtitle", "")
                        editor.apply()
                    } else {
                        text_subtitle_change.visibility = View.VISIBLE
                        text_subtitle_change.text = sign
                        editor.putString("subtitle", sign)
                        editor.apply()
                    }
                }
            }.show()
            signDialog.findViewById<EditText>(R.id.dialog_edittext)?.setHint("副标题文本")
            signDialog.findViewById<EditText>(R.id.dialog_edittext)?.setText(subTitleText)
        }

        button_github.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data= Uri.parse("https://github.com/Liyulive/ATodo")
            startActivity(intent)
        }
    }
}