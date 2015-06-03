//NO_CHECK_LAMBDA_INLINING

import test.*

fun test1(): String {
    return a {
        try {
            test {
                return@a "OK"
            }
        }
        finally {

        }
    }
}

fun test2(): String {

    return test z@ {
        try {
            return@z "OK"
        }
        finally {

        }
    }

}

fun test3(): String {
    return test {
        try {
            return@test "OK"
        }
        finally {

        }
    }
}


fun test4(): String {
    return a z@ {
        try {
            test {
                return@z "OK"
            }
        }
        finally {

        }
    }
}


fun box(): String {
    if (test1() != "OK") return "fail 1: ${test1()}"

    if (test2() != "OK") return "fail 2: ${test2()}"

    if (test3() != "OK") return "fail 3: ${test3()}"

    if (test4() != "OK") return "fail 4: ${test4()}"

    return "OK"
}