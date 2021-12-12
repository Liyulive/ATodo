package cf.liyu.atodo.model

import cn.bmob.v3.BmobObject

class TodoItem(
    var user: String,
    var title: String,
    var detail: String,
    var category: String,
    var undo: Boolean,
    var deadline: Long
) : BmobObject() {
}