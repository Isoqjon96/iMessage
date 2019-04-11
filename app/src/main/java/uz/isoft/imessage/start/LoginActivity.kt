package uz.isoft.imessage.start

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.model.Image
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import uz.isoft.imessage.*
import uz.isoft.imessage.main.MainActivity
import java.io.File
import java.io.FileOutputStream

class LoginActivity : AppCompatActivity() {
    private var uid = ""
    private var phone = ""
    private var compositeDisposable = CompositeDisposable()
    private var image: Image? = null
    private var userM: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        uid = intent.getStringExtra("uid")
        phone = intent.getStringExtra("phone")
        ivAvatar.setOnClickListener {
            ImagePicker.create(this)
                .theme(R.style.AppTheme_NoActionBar)
                .folderMode(true)
                .single()
                .start()

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_done -> {

                when {
                    etUserName.text?.isEmpty() != false -> {
                        AlertDialog.Builder(this)
                            .setTitle(resources.getString(R.string.error))
                            .setMessage(R.string.fill_fields)
                            .show()
                        etUserName.requestFocus()
                    }
                    etUserName.text.toString().length < 3 -> {
                        Toast.makeText(this, "Имя пользователя должно быть не менее трех букв", Toast.LENGTH_LONG)
                            .show()
                        etUserName.requestFocus()
                    }
                    else -> sendData(
                        name = etUserName.text.toString(),
                        surname = etUserSurname.text.toString(),
                        phone = phone,
                        uid = uid
                    )
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS),
                    101
                )
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS), 111)
            }
        } else {
            sendContact(getContacts())
        }
    }

    private fun sendData(phone: String, name: String, surname: String, uid: String) {
        var user: MultipartBody.Part = MultipartBody.Part.createFormData(
            "",
            Gson().toJson(User(name = name, surname = surname, phone = phone, uid = uid))
        )

        compositeDisposable = CompositeDisposable()
        try {
            val file = File(image?.path)
            val surveyBody = RequestBody.create(MediaType.parse("image/*"), saveBitmapToFile(file)!!)
            val imagePart: MultipartBody.Part = MultipartBody.Part.createFormData("SurveyImage", file.name, surveyBody)
            compositeDisposable.add(
                ApiFactory
                    .getApiService()
                    .sendData(imagePart, user)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe {
                        showLoading()
                    }
                    .subscribe({
                        if (it.code == 0) {
                            it.result.let { u ->
                                checkPermission()
                                userM = u ?: User()

                            }
                        }
                    }, {
                        Toast.makeText(this, getString(R.string.error_no_internet_connection), Toast.LENGTH_SHORT)
                            .show()
                    })
            )
        } catch (e: Exception) {
            compositeDisposable.add(
                ApiFactory
                    .getApiService()
                    .sendData(user)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe {
                        showLoading()
                    }
                    .subscribe({
                        if (it.code == 0) {
                            it.result.let { u ->
                                checkPermission()
                                userM = u ?: User()

                            }
                        }
                    }, {
                        Toast.makeText(this, getString(R.string.error_no_internet_connection), Toast.LENGTH_SHORT)
                            .show()
                    })
            )
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            image = ImagePicker.getFirstImageOrNull(data)
            Picasso.get()
                .load(File(image?.path))
                .into(ivAvatar)
        }
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 101 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            sendContact(getContacts())
        } else {
            Toast.makeText(this, "должно разрешить контакт", Toast.LENGTH_SHORT).show()
        }
        if (requestCode == 111) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendContact(getContacts())
            } else {
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
                Toast.makeText(this, "должно разрешить контакт", Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun sendContact(contacts: ArrayList<Contact>) {

        compositeDisposable = CompositeDisposable()
        val wrapper = ContactWrapper()
        wrapper.contacts = contacts
        compositeDisposable.add(
            ApiFactory
                .getApiService()
                .sendContacts(wrapper)
                .doOnSubscribe {
                    showLoading()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ u ->
                    val temp = ArrayList<Contact>()
                    if (u.code == 0) {
                        u.result.let { i ->
                            i?.forEach { l ->
                                if (l.flag == 1) {
                                    temp.add(l)
                                    Completable.fromAction {
                                        ApiFactory
                                            .getContactDataBase(this)
                                            .contactDao()
                                            .insert(l)
                                    }.subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe({
                                            Toast.makeText(this, "Done 1", Toast.LENGTH_SHORT).show()
                                            PManager.apply {
                                                setName(userM?.name ?: "")
                                                setSurname(userM?.surname ?: "")
                                                setImage(userM?.image ?: "")
                                                setPhone(userM?.phone ?: "")
                                                setUID(userM?.uid?:"")
                                            }
                                        }, {
                                            showFragment()
                                            Toast.makeText(this, "Error 1", Toast.LENGTH_SHORT).show()
                                        })
                                }

                            }
                        }
                    }
                    Handler().postDelayed({
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }, 777)

                }, {
                    Toast.makeText(this, "Error 2", Toast.LENGTH_SHORT).show()
                })
        )
    }

    private fun getContacts(): ArrayList<Contact> {
        val contacts = ArrayList<Contact>()
        val cr = contentResolver
        val cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if (cur?.count ?: 0 > 0) {
            while (cur?.moveToNext() == true) {

                val id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID))
                val name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                if (name != null) {
                    Log.i("Names", name)
                }
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    val phones = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                        null,
                        null
                    )
                    while (phones?.moveToNext() == true) {
                        var phoneNumber =
                            phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        if (phoneNumber != null) {
                            phoneNumber = phoneNumber.replace("\\s".toRegex(), "")
                            Log.i("Number", phoneNumber)
                            if (!contacts.contains(Contact(name = name, phone = phoneNumber))) {
                                contacts.add(Contact(name = name ?: "", phone = phoneNumber))
                            }
                        }
                    }
                    phones?.close()
                }

            }
        }
        return contacts
    }

    private fun showLoading() {
        vLoading.visibility = View.VISIBLE
        vError.visibility = View.GONE
    }

    private fun showError() {
        vLoading.visibility = View.GONE
        vError.visibility = View.VISIBLE
    }

    private fun showFragment() {
        vLoading.visibility = View.GONE
        vError.visibility = View.GONE
    }
}
