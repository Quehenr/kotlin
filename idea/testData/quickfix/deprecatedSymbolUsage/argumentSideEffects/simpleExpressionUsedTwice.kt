// "Replace with 'newFun(p, p)'" "true"

@deprecated("", ReplaceWith("newFun(p, p)"))
fun oldFun(p: Int) {
    newFun(p, p)
}

fun newFun(p1: Int, p2: Int){}

fun foo(c: C) {
    <caret>oldFun(c.bar)
}

class C {
    var bar = 0
}
