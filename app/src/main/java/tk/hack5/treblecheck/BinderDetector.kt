package tk.hack5.treblecheck

object BinderDetector {
    init {
        System.loadLibrary("binderdetector")
    }

    @Suppress("FunctionName")
    external fun get_binder_version(): Int
}