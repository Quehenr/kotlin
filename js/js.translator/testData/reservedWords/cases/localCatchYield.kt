package foo

// NOTE THIS FILE IS AUTO-GENERATED by the generateTestDataForReservedWords.kt. DO NOT EDIT!

fun box(): String {
    

    
    try {
        throw Exception()
    }
    catch(yield: Exception) {
        testRenamed("yield", { yield })
    }

    return "OK"
}