package cf.liyu.atodo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Scene
import androidx.transition.TransitionManager
import cf.liyu.atodo.adapter.UndoAdapter
import cf.liyu.atodo.fragment.AddFragment
import cf.liyu.atodo.fragment.MenuFragment
import cf.liyu.atodo.model.Category
import cf.liyu.atodo.model.TodoItem
import cn.bmob.v3.Bmob
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.transition.MaterialFade
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*

class MainActivity : AppCompatActivity() {

    lateinit var viewModel: MainViewModel
    lateinit var undoAdapter: UndoAdapter
    lateinit var completeAdapter: UndoAdapter
    lateinit var signSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        signSharedPreferences = this.getSharedPreferences("settings", Context.MODE_PRIVATE)

        /*初始化Bmob*/
        Bmob.initialize(this, "")
        Log.d("MainActivity", System.currentTimeMillis().toString())

        /*adapter数据并取消嵌套滑动*/
        undoAdapter = UndoAdapter(
            viewModel.UndoList,
            supportFragmentManager
        )
        undoAdapter.setCallBack(object : UndoAdapter.NotifyCallBack {
            override fun notifyData() {
                getTodoList(viewModel.CategoryList[tabLayout.selectedTabPosition], viewModel.sortBy)
            }
        })
        completeAdapter = UndoAdapter(
            viewModel.completeList,
            supportFragmentManager
        )
        completeAdapter.setCallBack(object : UndoAdapter.NotifyCallBack {
            override fun notifyData() {
                getTodoList(viewModel.CategoryList[tabLayout.selectedTabPosition], viewModel.sortBy)
            }
        })
        recyclerview_complete.adapter = completeAdapter
        recyclerview_complete.layoutManager = LinearLayoutManager(this)
        recyclerview_undo.adapter = undoAdapter
        recyclerview_undo.layoutManager = LinearLayoutManager(this)
        ViewCompat.setNestedScrollingEnabled(recyclerview_undo, false)
        ViewCompat.setNestedScrollingEnabled(recyclerview_complete, false)
        edittext_filter.addTextChangedListener {
            undoAdapter.filter.filter(it.toString())
            completeAdapter.filter.filter(it.toString())
        }

        /*生成动画*/
        val rotateDown = AnimationUtils.loadAnimation(this, R.anim.rotate_down)
        val rotateUp = AnimationUtils.loadAnimation(this, R.anim.rotate_up)
        rotateDown.fillAfter = true
        rotateUp.fillAfter = true
        rotateDown.interpolator = AccelerateDecelerateInterpolator()
        rotateUp.interpolator = AccelerateDecelerateInterpolator()

        /*折叠list*/
        expand_complete.setOnClickListener {

            val materialFade = MaterialFade()
            TransitionManager.beginDelayedTransition(
                recyclerview_complete as ViewGroup,
                materialFade
            )

            if (recyclerview_complete.visibility == View.GONE) {
                recyclerview_complete.visibility = View.VISIBLE
                icon_arrow.startAnimation(rotateDown)
            } else {
                recyclerview_complete.visibility = View.GONE
                icon_arrow.startAnimation(rotateUp)
            }
        }

        /*初始化一个launcher打开login*/
        val mActivityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    viewModel.user.value = it.data?.getStringExtra("parseUser").toString()
                    viewModel.userId = it.data?.getStringExtra("userId").toString()
                    Log.d("MainActivity", "loginResult:${viewModel.user.value.toString()}")
                }
            }

        /*记住登录*/
        val sharedPreferences = getSharedPreferences("login", Context.MODE_PRIVATE)
        viewModel.isLogin = sharedPreferences.getBoolean("isLogin", false)
        if (!viewModel.isLogin) {
            startLogin(mActivityLauncher)
        } else {
            viewModel.user.value = sharedPreferences.getString("username", "").toString()
            Log.d("MainActivity", "isLogin:${viewModel.user.value.toString()}")
        }

        /*观察*/
        viewModel.user.observe(this) {
            Log.d("MainActivity", "observe,user:$it")
            if (it != "") {
                getCategoryList(it, true)
            }
        }

        /*初始化组件*/
        swipeRefresh.setColorSchemeResources(R.color.design_default_color_primary_variant)
        swipeRefresh.setOnRefreshListener {
            getTodoList(
                viewModel.CategoryList[tabLayout.selectedTabPosition],
                viewModel.sortBy
            )
        }

        /*底部菜单按钮*/
        val bottomDrawerBehavior = BottomSheetBehavior.from<View>(bottom_drawer)
        bottomDrawerBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        bottomAppbar.setNavigationOnClickListener {
            bottomDrawerBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED)
        }

        icon_filter_down.setOnClickListener {
            edittext_filter.setText("")
            bottomDrawerBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        bottomAppbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_more -> {
                    val menuFragment =
                        MenuFragment(
                            viewModel.CategoryList[tabLayout.selectedTabPosition],
                            viewModel.sortBy
                        )
                    menuFragment.setCallback(object : MenuFragment.ClickCallback {
                        override fun clickConfirm() {
                            getCategoryList(viewModel.user.value.toString(), false)
                        }
                    })
                    menuFragment.setExitCallback(object : MenuFragment.ExitConfirm {
                        override fun exitConfirm() {
                            sharedPreferences.edit {
                                putBoolean("isLogin", false)
                                putString("username", "")
                                commit()
                            }
                            startLogin(mActivityLauncher)
                            viewModel.user.value = ""
                        }
                    })
                    menuFragment.setSortCallback(object : MenuFragment.SortCallback {
                        override fun sortConfirm(sortBy: Int) {
                            viewModel.sortBy = sortBy
                            getTodoList(
                                viewModel.CategoryList[tabLayout.selectedTabPosition],
                                viewModel.sortBy
                            )
                        }
                    })
                    if (tabLayout.selectedTabPosition == 0) {
                        menuFragment.show(supportFragmentManager, "default")
                    } else {
                        menuFragment.show(supportFragmentManager, null)
                    }
                }
                R.id.menu_account -> {
                    val intent = Intent(this, AccountActivity::class.java).putExtra(
                        "userId",
                        viewModel.userId
                    )
                    startActivity(intent)
                }
            }
            true
        }

        /*悬浮按钮*/
        fab.setOnClickListener {
            val dialog = AddFragment(
                viewModel.user.value.toString(),
                viewModel.CategoryList[tabLayout.selectedTabPosition],
                null
            )
            dialog.setCallback(object : AddFragment.ClickCallBack {

                override fun clickConfirm() {
                    getTodoList(
                        viewModel.CategoryList[tabLayout.selectedTabPosition],
                        viewModel.sortBy
                    )
                }

            })
            dialog.show(supportFragmentManager, "addTodo")
        }

        /*tablayout选择*/
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                getTodoList(viewModel.CategoryList[tab!!.position], viewModel.sortBy)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
    }

    private fun getCategoryList(user: String, getTodo: Boolean) {
        BmobQuery<Category>().addWhereEqualTo("user", user).findObjects(object :
            FindListener<Category>() {
            override fun done(p0: MutableList<Category>?, p1: BmobException?) {
                if (p1 == null) {
                    Log.d("MainActivity", "querySuccess,size:${p0?.size}")
                    viewModel.CategoryList.clear()
                    tabLayout.removeAllTabs()
                    p0?.forEach {
                        viewModel.CategoryList.add(it)
                        tabLayout.addTab(tabLayout.newTab().setText(it.category))
                    }
                    if (getTodo) {
                        getTodoList(viewModel.CategoryList[0], viewModel.sortBy)
                    }
                } else {
                    Log.d("MainActivity", "queryError:${p1.message}")
                }
            }
        })
    }

    private fun getTodoList(cate: Category, sortBy: Int) {
        swipeRefresh.isRefreshing = true
        BmobQuery<TodoItem>().apply {
            addWhereEqualTo("category", cate.objectId)
            findObjects(object : FindListener<TodoItem>() {
                override fun done(p0: MutableList<TodoItem>?, p1: BmobException?) {
                    viewModel.UndoList.clear()
                    viewModel.completeList.clear()
                    p0?.forEach {
                        if (it.undo == true) {
                            viewModel.UndoList.add(it)
                        } else {
                            viewModel.completeList.add(it)
                        }
                    }
                    when (sortBy) {
                        1 -> {
                            viewModel.UndoList.sortBy { it.deadline }
                            viewModel.completeList.sortBy { it.deadline }
                        }
                        2 -> {
                            viewModel.UndoList.sortByDescending { it.deadline }
                            viewModel.completeList.sortByDescending { it.deadline }
                        }
                    }
                    undoAdapter.filter.filter(edittext_filter.text.toString())
                    completeAdapter.filter.filter(edittext_filter.text.toString())
                    textview_complete_count.text = "已完成（${completeAdapter.getAllCount()}）"
                    undoAdapter.notifyDataSetChanged()
                    completeAdapter.notifyDataSetChanged()
                    swipeRefresh.isRefreshing = false
                    Log.d("MainActivity", "UndoListSize:" + viewModel.UndoList.size.toString())
                }
            })
        }
    }

    override fun onStart() {
        super.onStart()
        val sign = signSharedPreferences.getString("sign", "")
        val subTitle = signSharedPreferences.getString("subtitle", "")
        if (sign == "") {
            text_sign.visibility = View.GONE
        } else {
            text_sign.visibility = View.VISIBLE
            text_sign.text = sign
        }
        toolbar.subtitle = subTitle
    }

    fun startLogin(mActivityLauncher: ActivityResultLauncher<Intent>) {
        Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show()
        mActivityLauncher.launch(Intent(this, LoginActivity::class.java))
    }

}