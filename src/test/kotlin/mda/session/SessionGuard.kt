package mda.session

object SessionGuard {
    private var locked: Boolean = false
    private var reason: String = ""
    var previousSessionId: String? = null

    fun stopIfLocked() {
        if (locked) {
            throw AssertionError(reason)
        }
    }

    fun failNotNew(detail: String): Nothing {
        val sentence = "The session was not new, so the rest of the suite stops here. $detail"
        locked = true
        reason = sentence
        throw AssertionError(sentence)
    }
}
