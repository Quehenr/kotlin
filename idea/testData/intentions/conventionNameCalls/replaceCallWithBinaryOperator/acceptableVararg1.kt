fun test() {
    class Test{
        fun plus(a: Int, vararg b: Int, c: Int = 0) : Int = 0
    }
    val test = Test()
    test.p<caret>lus(1)
}
