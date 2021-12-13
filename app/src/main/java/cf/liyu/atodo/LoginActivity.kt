package cf.liyu.atodo

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.edit
import cf.liyu.atodo.model.Category
import cf.liyu.atodo.model.User
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import cn.bmob.v3.listener.SaveListener
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        /*切换注册登录*/
        button_switchLogin.setOnClickListener {
            card_login.visibility = View.VISIBLE
            card_register.visibility = View.GONE
        }
        button_switchRegister.setOnClickListener {
            card_login.visibility = View.GONE
            card_register.visibility = View.VISIBLE
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
                                                        this@LoginActivity,
                                                        "注册成功",
                                                        Toast.LENGTH_SHORT
                                                    )
                                                        .show()
                                                }
                                            })
                                        } else {
                                            Toast.makeText(
                                                this@LoginActivity,
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

        /*登录*/
        button_login.setOnClickListener {
            val query = BmobQuery<User>()
            query.addWhereEqualTo("username", edittext_login_username.text.toString())
            query.addWhereEqualTo("password", edittext_login_password.text.toString())
            query.findObjects(object : FindListener<User>() {
                override fun done(list: MutableList<User>?, e: BmobException?) {
                    if (e == null) {
                        if (list?.size == 0) {
                            Toast.makeText(this@LoginActivity, "账号或密码错误", Toast.LENGTH_SHORT).show()
                        } else {
                            val sharedPreferences =
                                getSharedPreferences("login", Context.MODE_PRIVATE)
                            if (checkbox_rememberLogin.isChecked) {
                                sharedPreferences.edit {
                                    putBoolean("isLogin", true)
                                    putString("username", edittext_login_username.text.toString())
                                    commit()
                                }
                            }
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.putExtra("parseUser", edittext_login_username.text.toString())
                            setResult(RESULT_OK, intent)
                            Toast.makeText(this@LoginActivity, "登陆成功", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
            })
        }
    }
}