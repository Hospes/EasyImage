package pl.aprilapps.easyphotopicker.sample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pl.aprilapps.easyphotopicker.ChooserType
import pl.aprilapps.easyphotopicker.EasyContracts
import pl.aprilapps.easyphotopicker.MediaFile

private const val PHOTOS_KEY = "easy_image_photos_list"

class ContractsActivity : AppCompatActivity(R.layout.activity_main) {
    private lateinit var recyclerView: RecyclerView
    private lateinit var galleryButton: View
    private val photos = mutableListOf<MediaFile>()
    private val imagesAdapter = ImagesAdapter(this, photos)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        recyclerView = findViewById(R.id.recycler_view)
        galleryButton = findViewById(R.id.gallery_button)

        savedInstanceState?.getParcelableArrayList<MediaFile>(PHOTOS_KEY)?.apply { photos.addAll(this) }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = imagesAdapter


        findViewById<View>(R.id.gallery_button).setOnClickListener {
            /** Some devices such as Samsungs which have their own gallery app require write permission. Testing is advised!  */
            val necessaryPermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (arePermissionsGranted(necessaryPermissions)) {
                gallery.launch(true)
            } else {
                requestPermissionsCompat(necessaryPermissions, MainActivity.GALLERY_REQUEST_CODE)
            }
        }


        findViewById<View>(R.id.camera_button).setOnClickListener {
            val necessaryPermissions = arrayOf(Manifest.permission.CAMERA)
            if (arePermissionsGranted(necessaryPermissions)) {
                cameraImage.launch(null)
            } else {
                requestPermissionsCompat(necessaryPermissions, MainActivity.CAMERA_REQUEST_CODE)
            }
        }

        findViewById<View>(R.id.camera_video_button).setOnClickListener {
            val necessaryPermissions = arrayOf(Manifest.permission.CAMERA)
            if (arePermissionsGranted(necessaryPermissions)) {
                cameraVideo.launch(null)
            } else {
                requestPermissionsCompat(necessaryPermissions, MainActivity.CAMERA_VIDEO_REQUEST_CODE)
            }
        }

        findViewById<View>(R.id.documents_button).setOnClickListener {
            /** Some devices such as Samsungs which have their own gallery app require write permission. Testing is advised!  */
            val necessaryPermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (arePermissionsGranted(necessaryPermissions)) {
                documents.launch(null)
            } else {
                requestPermissionsCompat(necessaryPermissions, MainActivity.DOCUMENTS_REQUEST_CODE)
            }
        }

        findViewById<View>(R.id.chooser_button).setOnClickListener {
            val necessaryPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (arePermissionsGranted(necessaryPermissions)) {
                chooser.launch(true)
            } else {
                requestPermissionsCompat(necessaryPermissions, MainActivity.CHOOSER_PERMISSIONS_REQUEST_CODE)
            }
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(PHOTOS_KEY, ArrayList(photos))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == MainActivity.CHOOSER_PERMISSIONS_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            chooser.launch(true)
        } else if (requestCode == MainActivity.CAMERA_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            cameraImage.launch(null)
        } else if (requestCode == MainActivity.CAMERA_VIDEO_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            cameraVideo.launch(null)
        } else if (requestCode == MainActivity.GALLERY_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            gallery.launch(true)
        } else if (requestCode == MainActivity.DOCUMENTS_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            documents.launch(null)
        }
    }

    private val chooserConfig = EasyContracts.ChooserConfig(chooserType = ChooserType.CAMERA_AND_GALLERY)
    private val chooser = registerForActivityResult(EasyContracts.Chooser(this, chooserConfig)) { handleResult(it) }
    private val documents = registerForActivityResult(EasyContracts.Documents(this)) { handleResult(it) }
    private val gallery = registerForActivityResult(EasyContracts.Gallery(this)) { handleResult(it) }
    private val cameraImage = registerForActivityResult(EasyContracts.CameraForImage(this)) { handleResult(it) }
    private val cameraVideo = registerForActivityResult(EasyContracts.CameraForVideo(this)) { handleResult(it) }


    private fun handleResult(result: EasyContracts.Result) = when (result) {
        is EasyContracts.Result.Success -> onPhotosReturned(result.files.toTypedArray())
        is EasyContracts.Result.Error -> result.throwable.printStackTrace() /* Handle error result */
        is EasyContracts.Result.Canceled -> Unit /* Handle canceled result */
    }

    private fun onPhotosReturned(returnedPhotos: Array<MediaFile>) {
        photos.addAll(returnedPhotos)
        imagesAdapter.notifyDataSetChanged()
        recyclerView.scrollToPosition(photos.size - 1)
    }

    private fun arePermissionsGranted(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) return false
        }
        return true
    }

    private fun requestPermissionsCompat(permissions: Array<String>, requestCode: Int) =
            ActivityCompat.requestPermissions(this, permissions, requestCode)
}