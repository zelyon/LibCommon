package bzh.zelyon.lib.extension

import android.os.Build

fun isMarshmallow() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

fun isNougat() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

fun isOreo() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

fun isPie() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

fun isQ() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

fun isR() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

fun Int.millisecondsToDuration(): String {
    val nbHour = this/1000/60/60
    val nbMinutes = this/1000/60 - 60*nbHour
    val nbSecond = this/1000 - 60*60*nbHour - 60*nbMinutes
    return listOf(nbHour, nbMinutes, nbSecond).filterIndexed { index, it ->
        !(index == 0 && it == 0)
    }.joinToString(separator = ":") { if (it < 10) "0$it" else "$it" }
}
