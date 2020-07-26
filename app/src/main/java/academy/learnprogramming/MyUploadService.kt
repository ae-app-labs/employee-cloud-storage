package academy.learnprogramming

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

/**
 * Service to handle uploading files to Firebase Storage.
 */
class MyUploadService : MyBaseTaskService() {

    private lateinit var storageRef: StorageReference

    override fun onCreate() {
        super.onCreate()

        // Initialize Storage
        storageRef = FirebaseStorage.getInstance().reference
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand:$intent:$startId")
        if (ACTION_UPLOAD == intent.action) {
            val fileUri = intent.getParcelableExtra<Uri>(EXTRA_FILE_URI)

            // Make sure we have permission to read the data
            contentResolver.takePersistableUriPermission(
                fileUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION)

            uploadFromUri(fileUri)
        }

        return Service.START_REDELIVER_INTENT
    }

    private fun uploadFromUri(fileUri: Uri) {
        // implement me
        taskStarted()

        val photoRef = storageRef.child("photos")
            .child( getFilename(contentResolver, fileUri) )

        photoRef.putFile(fileUri)
            .addOnProgressListener { _ ->

            }
            .continueWithTask { task ->
                if(!task.isSuccessful){
                    Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
                    throw task.exception!!
                }

                photoRef.downloadUrl
            }.addOnSuccessListener {downloadUri ->
                broadcastUploadFinished(downloadUri, fileUri)

                taskCompleted()
            }.addOnFailureListener {
                Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
                broadcastUploadFinished(null, fileUri)
                taskCompleted()
            }

    }

    /**
     * Broadcast finished upload (success or failure).
     * @return true if a running receiver received the broadcast.
     */
    private fun broadcastUploadFinished(downloadUrl: Uri?, fileUri: Uri?): Boolean {
        val success = downloadUrl != null

        val action = if (success) UPLOAD_COMPLETED else UPLOAD_ERROR

        val broadcast = Intent(action)
            .putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
            .putExtra(EXTRA_FILE_URI, fileUri)
        return LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(broadcast)
    }

    companion object {

        private const val TAG = "MyUploadService"

        /** Intent Actions  */
        const val ACTION_UPLOAD = "action_upload"
        const val UPLOAD_COMPLETED = "upload_completed"
        const val UPLOAD_ERROR = "upload_error"

        /** Intent Extras  */
        const val EXTRA_FILE_URI = "extra_file_uri"
        const val EXTRA_DOWNLOAD_URL = "extra_download_url"

        val intentFilter: IntentFilter
            get() {
                val filter = IntentFilter()
                filter.addAction(UPLOAD_COMPLETED)
                filter.addAction(UPLOAD_ERROR)

                return filter
            }
    }
}