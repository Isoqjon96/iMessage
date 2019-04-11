package uz.isoft.imessage.start

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*
import uz.isoft.imessage.*
import uz.isoft.imessage.main.MainActivity

class SplashActivity : AppCompatActivity() {

    var compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        when {
            PManager.getPhone().isEmpty() -> {
                val providers = arrayListOf(AuthUI.IdpConfig.PhoneBuilder().build())

                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setLogo(R.drawable.ic_launcher_foreground)
                        .setTheme(R.style.AppTheme)
                        .build(), 101
                )
            }

            else -> {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 101) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser

                Toast.makeText(this, user.toString(), Toast.LENGTH_SHORT).show()
//                compositeDisposable = CompositeDisposable()
                compositeDisposable.add(
                    ApiFactory
                        .getApiService()
                        .checkUser(user?.uid.toString())
                       .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe {
                            showLoading()
                        }
                        .subscribe({
                            if (it.code == 0) {
                                it.result.let { userM ->
                                    PManager.apply {
                                        setName(userM?.name ?: "")
                                        setSurname(userM?.surname ?: "")
                                        setImage(userM?.image ?: "")
                                        setPhone(userM?.phone ?: "")
                                        setUID(userM?.uid ?: "")
                                        checkPermission()
                                    }

                                }
                            } else {
                                startActivity(
                                    Intent(this, LoginActivity::class.java)
                                        .putExtra("phone", user?.phoneNumber.toString())
                                        .putExtra("uid", user?.uid.toString())
                                )
                            }

                        }, {

                        })
                )


            } else {
                Toast.makeText(this, "Sign in failed.", Toast.LENGTH_SHORT).show()
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
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
