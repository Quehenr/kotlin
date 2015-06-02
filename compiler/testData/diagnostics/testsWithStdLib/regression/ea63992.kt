trait A {
    fun shuffle<T>(x: List<T>): List<T>
    fun sort
    fun foo<T>(f : (List<T>) -> List<T>, x : List<T>)

fun f() : (Int, Int) -> Int = ::add
}