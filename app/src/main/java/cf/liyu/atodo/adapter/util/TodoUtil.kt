package cf.liyu.atodo.adapter.util

import java.text.SimpleDateFormat
import java.util.*

object TodoUtil {
    fun transferLongToDate(dateFormat: String, millSecond: Long): String? {
        //"yyyy年MM月dd日  kk:mm:ss"
        val time = Date(millSecond)
        val formats = SimpleDateFormat(dateFormat)
        return formats.format(time)
    }
}