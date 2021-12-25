package cf.liyu.atodo

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.transition.TransitionManager
import cf.liyu.atodo.model.Category
import cf.liyu.atodo.model.User
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import cn.bmob.v3.listener.SaveListener
import com.google.android.material.transition.MaterialFade
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        progress_login.hide()

        /*切换注册登录*/
        button_switch_register.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        /*登录*/
        button_login.setOnClickListener {
            progress_login.show()
            button_login.isEnabled = false
            val query = BmobQuery<User>()
            query.addWhereEqualTo("username", edittext_login_username.text.toString())
            query.addWhereEqualTo("password", edittext_login_password.text.toString())
            query.findObjects(object : FindListener<User>() {
                override fun done(list: MutableList<User>?, e: BmobException?) {
                    if (e == null) {
                        if (list?.size == 0) {
                            Toast.makeText(this@LoginActivity, "账号或密码错误", Toast.LENGTH_SHORT).show()
                            button_login.isEnabled = true
                            progress_login.hide()
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
                            Log.d("mTest", list!![0].objectId)
                            intent.putExtra("userId", list!![0].objectId)
                            setResult(RESULT_OK, intent)
                            progress_login.hide()
                            finish()
                        }
                    }
                }
            })
        }
    }
}