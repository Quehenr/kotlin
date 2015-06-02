


// TODO: DIAGNOSTIC TEST IS NOT GOOD FOR THIS




// MODULE: m1
// FILE: a.kt

package a

interface A

// MODULE: m2(m1)
// FILE: b.kt

package b

import a.A

interface B {
    fun foo(): A
}

// MODULE: m3(m2)
// FILE: c.kt

package c

import b.B

fun bar(b: B) {
    val v: String = b.foo()
}
