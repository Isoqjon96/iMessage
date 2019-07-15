package uz.isoft.imessage.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.model.Image
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_show_image.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import uz.isoft.imessage.*
import java.io.File
import java.io.FileOutputStream

class ImageActivity : AppCompatActivity() {

    private var isProfile = false
    private var compositeDisposable = CompositeDisposable()
    private var image: Image? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_image)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        isProfile = intent.getBooleanExtra(INTENT_IS_PROFILE, false)
        val image = intent.getStringExtra(INTENT_IMAGE)
        val isFromServer = intent.getBooleanExtra(INTENT_FROM_SERVER, false)

        if (isFromServer) {
            title = image
            if(isProfile) {
                Picasso.get()
                        .load(SERVER_IMAGE_ADDRESS + PManager.getImage())
                        .placeholder(R.drawable.ic_image)
                        .into(iv)
            }else{
                Picasso.get()
                        .load(SERVER_IMAGE_ADDRESS + image)
                        .placeholder(R.drawable.ic_image)
                        .into(iv)
            }
        } else {
            Picasso.get()
                    .load(File(image))
                    .placeholder(R.drawable.ic_image)
                    .into(iv)
        }


        title = "Change image"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return if (isProfile) {
            menuInflater.inflate(R.menu.image_menu, menu)
            true
        } else {
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_edit -> {
                item.setIcon(R.drawable.ic_pencil)
                ImagePicker.create(this)
                        .theme(R.style.AppTheme_NoActionBar)
                        .folderMode(true)
                        .single()
                        .start()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            image = ImagePicker.getFirstImageOrNull(data)
            upDataImage()
        }
    }

    private fun upDataImage() {
	    val file = File(image?.path)
        val surveyBody = RequestBody.create(MediaType.parse("image/*"), saveBitmapToFile(file)!!)
        val imagePart: MultipartBody.Part = MultipartBody.Part.createFormData("SurveyImage", file.name, surveyBody)

        compositeDisposable.add(ApiFactory
                .getApiService()
                .upDataAgentImage(PManager.getUID(), imagePart)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {
                    pb.visibility = View.VISIBLE
                    iv.visibility = View.GONE
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.code == 0) {
                        PManager.setImage(it.result!!)
                        Picasso.get()
                                .load(SERVER_IMAGE_ADDRESS + PManager.getImage())
                                .centerCrop()
                                .placeholder(R.drawable.ic_image)
                                .fit()
                                .into(iv)

                        Toast.makeText(
                                this,
                                getString(R.string.photo_loaded_successfully),
                                Toast.LENGTH_LONG
                        ).show()

                    } else {
                        AlertDialog.Builder(this)
                                .setTitle(resources.getString(R.string.error))
                                .setMessage(resources.getString(R.string.unknown_error))
                                .show()
                    }
                    iv.visibility = View.VISIBLE
                    pb.visibility = View.GONE
                }, {
                    Toast.makeText(this, resources.getString(R.string.error_no_internet_connection), Toast.LENGTH_LONG)
                            .show()
                    iv.visibility = View.VISIBLE
                    pb.visibility = View.GONE
                })
        )
    }
    private fun saveBitmapToFile(file: File): File? {
	
 val bitmapImage = BitmapFactory.decodeFile(file.path)
        try {
            return if (bitmapImage.height <= 512) {
                return file
            } else {
                val nh = ((bitmapImage.width * 512.0) / bitmapImage.height).toInt()
                val scaled = Bitmap.createScaledBitmap(bitmapImage, nh, 512, true)
                val dir = File(file.path)
                if (!dir.exists())
                    dir.mkdirs()
                file.createNewFile()
                val fOut = FileOutputStream(file)
                scaled.compress(Bitmap.CompressFormat.PNG, 85, fOut)
                fOut.flush()
                fOut.close()
                file
            }

        } catch (e: Exception) {
            return null
        }

    }


}
