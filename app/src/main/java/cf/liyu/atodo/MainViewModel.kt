package cf.liyu.atodo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cf.liyu.atodo.model.Category
import cf.liyu.atodo.model.TodoItem

class MainViewModel : ViewModel() {

    var isLogin = false
    val user = MutableLiveData<String>()
    var CategoryList = ArrayList<Category>()
    var UndoList = ArrayList<TodoItem>()
    var completeList = ArrayList<TodoItem>()
    var sortBy = 0
    var userId = ""

}