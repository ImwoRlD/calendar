package cc.yy.calendar.activity

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.text.format.DateFormat
import cc.yy.calendar.R
import cc.yy.calendar.util.Constant
import kotlinx.android.synthetic.main.activity_setting.*
import org.jetbrains.anko.selector
import permissions.dispatcher.*
import java.io.File
import java.io.IOException
import java.util.*


/**
 * Created by zpy on 2018/3/13.
 */
@RuntimePermissions
class SettingActivity : BaseActivity() {
    override fun getLayout(): Int = R.layout.activity_setting
    private val requestImageFromAlbum = 111
    private val requestImageFromCamera = 222
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    private fun init() {
        tv_title.text = "设置"
        initListener()
    }

    private fun initListener() {
        rl_change_background.setOnClickListener {
            val options = listOf("从手机相册选择", "拍一张")
            selector(null, options) { _, items ->
                when (items) {
                    0 -> {
                        openAlbumWithPermissionCheck()
                    }
                    1 -> {
                        openCameraWithPermissionCheck()
                    }
                }

            }
        }
    }

    @NeedsPermission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {

            }
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(this, "cc.yy.calendar.fileprovider", photoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, requestImageFromCamera)
            }
        }
    }

    @OnShowRationale(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showRationaleForOpenCamera(request: PermissionRequest) {
        request.proceed()
    }

    @OnPermissionDenied(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onOpenCameraDenied() {
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onOpenCameraNeverAskAgain() {
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun openAlbum() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, requestImageFromAlbum)
    }

    @OnShowRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun showRationaleForOpenAlbum(request: PermissionRequest) {
        request.proceed()
    }

    @OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun onOpenAlbumDenied() {

    }

    @OnNeverAskAgain(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun onOpenAlbumNeverAskAgain() {

    }


    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestImageFromAlbum && Activity.RESULT_OK == resultCode) {
            if (data != null) {
                try {
                    val path = getImageAbsolutePath(this@SettingActivity, data.data)
                    logUtil(path ?: "fuck album")
                    path?.let {
                        app.putSpValue(Constant.SP_PATH_FOR_BACKGROUND, it)
                        val intent = Intent(Constant.SP_ACTION_SET_BACKGROUND)
                        intent.putExtra("path", path)
                        sendBroadcast(intent)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
        if (requestCode == requestImageFromCamera && Activity.RESULT_OK == resultCode) {
            if (data != null) {
//                logUtil("path=${data.getParcelableExtra<Uri>(MediaStore.EXTRA_OUTPUT).path}")
                logUtil(currentPhotoPath ?: "fuck camera")
                currentPhotoPath?.let {
                    app.putSpValue(Constant.SP_PATH_FOR_BACKGROUND, it)
                    val intent = Intent(Constant.SP_ACTION_SET_BACKGROUND)
                    intent.putExtra("path", currentPhotoPath)
                    sendBroadcast(intent)
                }
            }
        }
    }

    private var currentPhotoPath: String? = null

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = DateFormat.format("yyyyMMdd_HHmmss", Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir      /* directory */
        )
        currentPhotoPath = image.absolutePath
        return image
    }
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is ExternalStorageProvider.
 */
private fun isExternalStorageDocument(uri: Uri): Boolean {
    return "com.android.externalstorage.documents" == uri.authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is DownloadsProvider.
 */
private fun isDownloadsDocument(uri: Uri): Boolean {
    return "com.android.providers.downloads.documents" == uri.authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is MediaProvider.
 */
private fun isMediaDocument(uri: Uri): Boolean {
    return "com.android.providers.media.documents" == uri.authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is Google Photos.
 */
private fun isGooglePhotosUri(uri: Uri): Boolean {
    return "com.google.android.apps.photos.content" == uri.authority
}

private fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {
    var cursor: Cursor? = null
    val column = MediaStore.Images.Media.DATA
    val projection = arrayOf(column)
    try {
        cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
        if (cursor != null && cursor.moveToFirst()) {
            val index = cursor.getColumnIndexOrThrow(column)
            return cursor.getString(index)
        }
    } finally {
        if (cursor != null)
            cursor.close()
    }
    return null
}

/**
 * 根据Uri获取图片绝对路径，解决Android4.4以上版本Uri转换
 *
 * @param context
 * @param imageUri
 */
@TargetApi(19)
fun getImageAbsolutePath(context: Context?, imageUri: Uri?): String? {
    if (context == null || imageUri == null)
        return null
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, imageUri)) {
        if (isExternalStorageDocument(imageUri)) {
            val docId = DocumentsContract.getDocumentId(imageUri)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val type = split[0]
            if ("primary".equals(type, ignoreCase = true)) {
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            }
        } else if (isDownloadsDocument(imageUri)) {
            val id = DocumentsContract.getDocumentId(imageUri)
            val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)!!)
            return getDataColumn(context, contentUri, null, null)
        } else if (isMediaDocument(imageUri)) {
            val docId = DocumentsContract.getDocumentId(imageUri)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val type = split[0]
            var contentUri: Uri? = null
            if ("image" == type) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            } else if ("video" == type) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            } else if ("audio" == type) {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            val selection = MediaStore.Images.Media._ID + "=?"
            val selectionArgs = arrayOf(split[1])
            return getDataColumn(context, contentUri, selection, selectionArgs)
        }
    } // MediaStore (and general)
    else if ("content".equals(imageUri.scheme, ignoreCase = true)) {
        // Return the remote address
        return if (isGooglePhotosUri(imageUri)) imageUri.lastPathSegment else getDataColumn(context, imageUri, null, null)
    } else if ("file".equals(imageUri.scheme, ignoreCase = true)) {
        return imageUri.path
    }// File
    return null
}