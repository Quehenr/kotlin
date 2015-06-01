// !DIAGNOSTICS: -FUNCTION_DECLARATION_WITH_NO_NAME
class ClassB() {
    private inner class ClassC: <!SYNTAX!>super<!>.<!UNRESOLVED_REFERENCE!><!SYNTAX!><!>ClassA<!>()<!SYNTAX!><!> {
    }
}
