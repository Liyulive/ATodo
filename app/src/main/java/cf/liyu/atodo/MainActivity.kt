package cf.liyu.atodo

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Adapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import cf.liyu.atodo.adapter.UndoAdapter
import cf.liyu.atodo.fragment.AddFragment
import cf.liyu.atodo.fragment.MenuFragment
import cf.liyu.atodo.model.Category
import cf.liyu.atodo.model.TodoItem
import cn.bmob.v3.Bmob
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var viewModel: MainViewModel
    lateinit var undoAdapter: UndoAdapter
    lateinit var completeAdapter: UndoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        /*adapter数据并取消嵌套滑动*/
        undoAdapter = UndoAdapter(
            viewModel.UndoList,
            supportFragmentManager
        )
        undoAdapter.setCallBack(object : UndoAdapter.NotifyCallBack {
            override fun notifyData() {
                getTodoList(
                    viewModel.user.value.toString(),
                    tabLayout.getTabAt(tabLayout.selectedTabPosition)?.text.toString()
                )
            }
        })
        completeAdapter = UndoAdapter(
            viewModel.completeList,
            supportFragmentManager
        )
        completeAdapter.setCallBack(object : UndoAdapter.NotifyCallBack {
            override fun notifyData() {
                getTodoList(
                    viewModel.user.value.toString(),
                    tabLayout.getTabAt(tabLayout.selectedTabPosition)?.text.toString()
                )
            }
        })
        recyclerview_complete.adapter = completeAdapter
        recyclerview_complete.layoutManager = LinearLayoutManager(this)
        recyclerview_undo.adapter = undoAdapter
        recyclerview_undo.layoutManager = LinearLayoutManager(this)
        ViewCompat.setNestedScrollingEnabled(recyclerview_undo, false)
        ViewCompat.setNestedScrollingEnabled(recyclerview_complete, false)
        expand_complete.setOnClickListener {
            if (recyclerview_complete.visibility == View.GONE) {
                recyclerview_complete.visibility = View.VISIBLE
            } else {
                recyclerview_complete.visibility = View.GONE
            }
        }

        Bmob.initialize(this, "016cb091df5d0cc2cf0c736831cb8734")

        Log.d("MainActivity", System.currentTimeMillis().toString())

        /*记住登录*/
        val sharedPreferences = getSharedPreferences("login", Context.MODE_PRIVATE)
        viewModel.isLogin = sharedPreferences.getBoolean("isLogin", false)
        if (!viewModel.isLogin) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show()
            val mActivityLauncher =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    if (it.resultCode == Activity.RESULT_OK) {
                        viewModel.user.value = it.data?.getStringExtra("parseUser").toString()
                        Log.d("MainActivity", "loginResult:${viewModel.user.value.toString()}")
                    }
                }
            mActivityLauncher.launch(Intent(this, LoginActivity::class.java))
        } else {
            viewModel.user.value = sharedPreferences.getString("username", "").toString()
            Log.d("MainActivity", "isLogin:${viewModel.user.value.toString()}")
        }

        /*观察*/
        viewModel.user.observe(this) {
            Log.d("MainActivity", "observe,it:$it")
            getCategoryList(it)
            getTodoList(it, "我的任务")
        }

        bottomAppbar.setNavigationOnClickListener {
            Snackbar.make(bottomAppbar, "测试", Snackbar.LENGTH_SHORT).show()
        }

        bottomAppbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_more -> {
                    var menuFragment: MenuFragment? = null
                    if (tabLayout.getTabAt(tabLayout.selectedTabPosition)?.text == "我的任务") {
                        menuFragment =
                            MenuFragment(Category(viewModel.user.value.toString(), "我的任务"))
                    } else {
                        menuFragment =
                            MenuFragment(viewModel.CategoryList[tabLayout.selectedTabPosition - 1])
                    }

                    menuFragment.setCallback(object : MenuFragment.ClickCallback {
                        override fun clickConfirm() {
                            getCategoryList(viewModel.user.value.toString())
                        }
                    })
                    menuFragment.show(supportFragmentManager, null)
                }
            }
            true
        }

        fab.setOnClickListener {
            val dialog = AddFragment(
                viewModel.user.value.toString(),
                tabLayout.getTabAt(tabLayout.selectedTabPosition)?.text.toString(),
                null
            )
            dialog.setCallback(object : AddFragment.ClickCallBack {

                override fun clickConfirm() {
                    getTodoList(
                        viewModel.user.value.toString(),
                        tabLayout.getTabAt(tabLayout.selectedTabPosition)?.text.toString()
                    )
                }

            })
            dialog.show(supportFragmentManager, "addTodo")
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                getTodoList(viewModel.user.value.toString(), tab?.text.toString())
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
    }

    private fun getCategoryList(user: String) {
        BmobQuery<Category>().addWhereEqualTo("user", user).findObjects(object :
            FindListener<Category>() {
            override fun done(p0: MutableList<Category>?, p1: BmobException?) {
                if (p1 == null) {
                    Log.d("MainActivity", "querySuccess,size:${p0?.size}")
                    viewModel.CategoryList.clear()
                    tabLayout.removeAllTabs()
                    tabLayout.addTab(tabLayout.newTab().setText("我的任务"))
                    p0?.forEach {
                        viewModel.CategoryList.add(it)
                        tabLayout.addTab(tabLayout.newTab().setText(it.category))
                    }
                } else {
                    Log.d("MainActivity", "queryError:${p1.message}")
                }
            }
        })
    }

    private fun getTodoList(user: String, category: String) {
        BmobQuery<TodoItem>().apply {
            addWhereEqualTo("user", user)
            addWhereEqualTo("category", category)
            findObjects(object : FindListener<TodoItem>() {
                override fun done(p0: MutableList<TodoItem>?, p1: BmobException?) {
                    viewModel.UndoList.clear()
                    viewModel.completeList.clear()
                    p0?.forEach {
                        if (it.undo) {
                            viewModel.UndoList.add(it)
                        } else {
                            viewModel.completeList.add(it)
                        }
                    }
                    undoAdapter.notifyDataSetChanged()
                    completeAdapter.notifyDataSetChanged()
                    Log.d("MainActivity", "UndoListSize:" + viewModel.UndoList.size.toString())
                }
            })
        }
    }

}