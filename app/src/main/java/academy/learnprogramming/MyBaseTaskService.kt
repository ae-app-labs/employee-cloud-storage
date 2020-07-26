package academy.learnprogramming

import android.app.Service
import android.util.Log

/**
 * Base class for Services that keep track of the number of active jobs and self-stop when the
 * count is zero.
 */
abstract class MyBaseTaskService : Service() {

    private var numTasks = 0

    fun taskStarted() {
        changeNumberOfTasks(1)
    }

    fun taskCompleted() {
        changeNumberOfTasks(-1)
    }

    @Synchronized
    private fun changeNumberOfTasks(delta: Int) {
        Log.d(TAG, "changeNumberOfTasks:$numTasks:$delta")
        numTasks += delta

        // If there are no tasks left, stop the service
        if (numTasks <= 0) {
            Log.d(TAG, "stopping")
            stopSelf()
        }
    }

    companion object {
        private const val TAG = "MyBaseTaskService"
    }
}