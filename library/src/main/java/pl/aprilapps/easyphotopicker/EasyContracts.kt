package pl.aprilapps.easyphotopicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract

object EasyContracts {

    class Chooser(private val ctx: Context,
                  private val config: ChooserConfig) : ActivityResultContract<Boolean, Result>(), HasFile {
        override var lastCameraFile: MediaFile? = null

        override fun createIntent(context: Context, multiple: Boolean): Intent =
                Files.createCameraPictureFile(context).also { lastCameraFile = it }
                        .let {
                            Intents.createChooserIntent(
                                    context = context,
                                    chooserTitle = config.chooserTitle,
                                    chooserType = config.chooserType,
                                    cameraFileUri = it.uri,
                                    allowMultiple = multiple
                            )
                        }

        override fun parseResult(resultCode: Int, intent: Intent?): Result {
            if (resultCode != Activity.RESULT_OK) return Result.Canceled
            Log.d(EASYIMAGE_LOG_TAG, "File returned from chooser")
            return if (intent != null && !Intents.isTherePhotoTakenWithCameraInsideIntent(intent) && intent.data != null)
                onPickedExistingPictures(ctx, intent).also { removeCameraFileAndCleanup() }
            else onPictureReturnedFromCamera(ctx, lastCameraFile, config)
        }
    }

    class Documents(private val ctx: Context) : ActivityResultContract<Unit, Result>() {
        override fun createIntent(context: Context, input: Unit?): Intent = Intents.createDocumentsIntent()

        override fun parseResult(resultCode: Int, intent: Intent?): Result {
            if (resultCode != Activity.RESULT_OK) return Result.Canceled
            return onPickedExistingPicturesFromLocalStorage(ctx, intent)
        }
    }

    class Gallery(private val ctx: Context) : ActivityResultContract<Boolean, Result>() {
        override fun createIntent(context: Context, multiple: Boolean): Intent = Intents.createGalleryIntent(multiple)

        override fun parseResult(resultCode: Int, intent: Intent?): Result {
            if (resultCode != Activity.RESULT_OK) return Result.Canceled
            return onPickedExistingPictures(ctx, intent)
        }
    }

    class CameraForImage(private val ctx: Context,
                         private val config: CopyConfig? = null) : ActivityResultContract<Unit, Result>(), HasFile {
        override var lastCameraFile: MediaFile? = null

        override fun createIntent(context: Context, input: Unit?): Intent =
                Files.createCameraPictureFile(context).also { lastCameraFile = it }
                        .let { Intents.createCameraForImageIntent(context, it.uri) }

        override fun parseResult(resultCode: Int, intent: Intent?): Result {
            if (resultCode != Activity.RESULT_OK) {
                removeCameraFileAndCleanup()
                return Result.Canceled
            }
            return onPictureReturnedFromCamera(ctx, lastCameraFile, config).also { cleanup() }
        }
    }

    class CameraForVideo(private val ctx: Context,
                         private val config: CopyConfig? = null) : ActivityResultContract<Unit, Result>(), HasFile {
        override var lastCameraFile: MediaFile? = null

        override fun createIntent(context: Context, input: Unit?): Intent =
                Files.createCameraVideoFile(context).also { lastCameraFile = it }
                        .let { Intents.createCameraForVideoIntent(context, it.uri) }

        override fun parseResult(resultCode: Int, intent: Intent?): Result {
            if (resultCode != Activity.RESULT_OK) {
                removeCameraFileAndCleanup()
                return Result.Canceled
            }
            return onVideoReturnedFromCamera(ctx, lastCameraFile, config).also { cleanup() }
        }
    }


    sealed class Result {
        data class Success(val files: List<MediaFile>) : Result()
        data class Error(val throwable: Throwable) : Result()
        object Canceled : Result()
    }


    private fun onPickedExistingPicturesFromLocalStorage(ctx: Context, intent: Intent?): Result {
        return try {
            intent?.data?.let {
                Log.d(EASYIMAGE_LOG_TAG, "Existing picture returned from local storage")
                val photoFile = Files.pickedExistingPicture(ctx, it)
                val mediaFile = MediaFile(it, photoFile)
                Result.Success(listOf(mediaFile))
            } ?: Result.Error(EasyImageException("Intent or data is null"))
        } catch (error: Throwable) {
            error.printStackTrace()
            Result.Error(error)
        }
    }

    private fun onPickedExistingPictures(ctx: Context, intent: Intent?): Result {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                val clipData = intent?.clipData
                if (clipData != null) {
                    Log.d(EASYIMAGE_LOG_TAG, "Existing picture returned")
                    val files = mutableListOf<MediaFile>()
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        val file = Files.pickedExistingPicture(ctx, uri)
                        files.add(MediaFile(uri, file))
                    }
                    if (files.isNotEmpty()) Result.Success(files)
                    else Result.Error(EasyImageException("No files were returned from gallery"))
                } else
                    onPickedExistingPicturesFromLocalStorage(ctx, intent)
            } else
                onPickedExistingPicturesFromLocalStorage(ctx, intent)
        } catch (error: Throwable) {
            error.printStackTrace()
            Result.Error(error)
        }
    }

    private fun onPictureReturnedFromCamera(ctx: Context, mediaFile: MediaFile?, config: CopyConfig? = null): Result {
        Log.d(EASYIMAGE_LOG_TAG, "Picture returned from camera")
        return mediaFile?.let { cameraFile ->
            try {
                if (cameraFile.uri.toString().isEmpty()) Intents.revokeWritePermission(ctx, cameraFile.uri)
                val files = mutableListOf(cameraFile)
                if (config != null && config.copyImagesToPublicGalleryFolder && config.folderName != null)
                    Files.copyFilesInSeparateThread(ctx, config.folderName, files.map { it.file })
                Result.Success(files)
            } catch (error: Throwable) {
                error.printStackTrace()
                Result.Error(EasyImageException("Unable to get the picture returned from camera.", error))
            }
        } ?: Result.Error(EasyImageException("Unable to get the picture returned from camera."))
    }

    private fun onVideoReturnedFromCamera(ctx: Context, mediaFile: MediaFile?, config: CopyConfig? = null): Result {
        Log.d(EASYIMAGE_LOG_TAG, "Video returned from camera")
        return mediaFile?.let { cameraFile ->
            try {
                if (cameraFile.uri.toString().isEmpty()) Intents.revokeWritePermission(ctx, cameraFile.uri)
                val files = mutableListOf(cameraFile)
                if (config != null && config.copyImagesToPublicGalleryFolder && config.folderName != null)
                    Files.copyFilesInSeparateThread(ctx, config.folderName, files.map { it.file })
                Result.Success(files)
            } catch (error: Throwable) {
                error.printStackTrace()
                Result.Error(EasyImageException("Unable to get the video returned from camera.", error))
            }
        } ?: Result.Error(EasyImageException("Unable to get the video returned from camera."))
    }


    class ChooserConfig(
            val chooserTitle: String = "",
            val chooserType: ChooserType = ChooserType.CAMERA_AND_DOCUMENTS,
            copyImagesToPublicGalleryFolder: Boolean = false,
            folderName: String? = null
    ) : CopyConfig(copyImagesToPublicGalleryFolder, folderName)

    open class CopyConfig(
            val copyImagesToPublicGalleryFolder: Boolean = false,
            val folderName: String? = null
    )

    interface HasFile {
        var lastCameraFile: MediaFile?

        fun removeCameraFileAndCleanup() {
            lastCameraFile?.file?.let { file ->
                Log.d(EASYIMAGE_LOG_TAG, "Removing camera file of size: ${file.length()}")
                file.delete()
                Log.d(EASYIMAGE_LOG_TAG, "Clearing reference to camera file")
                lastCameraFile = null
            }
        }

        fun cleanup() {
            lastCameraFile?.let { cameraFile ->
                Log.d(EASYIMAGE_LOG_TAG, "Clearing reference to camera file of size: ${cameraFile.file.length()}")
                lastCameraFile = null
            }
        }
    }
}