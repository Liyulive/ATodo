package cf.liyu.atodo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import cf.liyu.atodo.model.Category
import cf.liyu.atodo.model.User
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import cn.bmob.v3.listener.SaveListener
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        button_back_login.setOnClickListener {
            finish()
        }

        /*注册*/
        button_register.setOnClickListener {
            val query = BmobQuery<User>()
            query.addWhereEqualTo("username", edittext_register_username.text.toString())
            query.findObjects(object : FindListener<User>() {
                override fun done(p0: MutableList<User>?, p1: BmobException?) {
                    if (p1 == null) {
                        if (p0?.size == 0) {
                            if (edittext_register_password.text.toString() == edittext_register_confirm.text.toString()) {
                                val user = User(
                                    edittext_register_username.text.toString(),
                                    edittext_register_password.text.toString()
                                )
                                inputLayout_username.error = null
                                inputLayout_confirm.error = null
                                user.save(object : SaveListener<String>() {
                                    override fun done(id: String?, p1: BmobException?) {
                                        if (p1 == null) {
                                            val default = Category(user.username, "我的任务")
                                            default.save(object : SaveListener<String>() {
                                                override fun done(p0: String?, p1: BmobException?) {
                                                    Toast.makeText(
                                                        this@RegisterActivity,
                                                        "注册成功",
                                                        Toast.LENGTH_SHORT
                                                    )
                                                        .show()
                                                }
                                            })
                                        } else {
                                            Toast.makeText(
                                                this@RegisterActivity,
                                                "注册失败",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            Log.d("LoginActivity", "RegisterFailure:${p1.message}")
                                        }
                                    }
                                })
                            } else {
                                inputLayout_confirm.error = "两次密码不一致"
                            }
                        } else {
                            inputLayout_username.error = "账号已存在"
                        }
                    } else {
                        Log.d("LoginActivity", "RegisterQueryFailure:${p1.message}")
                    }
                }
            })
        }
    }
}