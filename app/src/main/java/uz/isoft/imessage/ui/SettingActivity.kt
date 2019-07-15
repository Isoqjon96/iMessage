package uz.isoft.imessage.ui

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.esafirm.imagepicker.features.ImagePickerActivity
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_profile.*
import uz.isoft.imessage.ApiFactory
import uz.isoft.imessage.PManager
import uz.isoft.imessage.R
import uz.isoft.imessage.SERVER_IMAGE_ADDRESS

class SettingActivity : Fragment() {
    companion object {
        private val fragment = SettingActivity()
        fun getInstance() = fragment
    }
    private var compositeDisposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onCreateData()
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profile_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (!etUserName.isEnabled) {
            etUserName.isEnabled = true
            etPhone.isEnabled = true
            item.setIcon(R.drawable.ic_done)
        } else {
            when {
                etUserName.text?.isEmpty()?:true -> {
                    AlertDialog.Builder(requireContext())
                        .setTitle(resources.getString(R.string.error))
                        .setMessage(R.string.fill_fields)
                        .show()
                    etUserName.requestFocus()
                }
                etPhone.text?.isEmpty()?:true-> {
                    AlertDialog.Builder(requireContext())
                        .setTitle(resources.getString(R.string.error))
                        .setMessage(R.string.fill_fields)
                        .show()
                    etPhone.requestFocus()
                }
                else -> {
                    when {
                        etUserName.text.toString().isEmpty() -> {
                            Toast.makeText(
                                requireContext(),
                                "Fill name",
                                Toast.LENGTH_LONG
                            ).show()
                            etUserName.requestFocus()
                        }
                        else -> {
                            setData()
                            item.setIcon(R.drawable.ic_pencil)
                            etUserName.isEnabled = false
                            etPhone.isEnabled = false
                        }
                    }
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }
    private fun setData() {
        compositeDisposable = CompositeDisposable()
        compositeDisposable.add(
            ApiFactory
            .getApiService()
            .setProfileData(PManager.getUID(), etUserName.text.toString(), etPhone.text.toString())
            .subscribeOn(Schedulers.io())
            .doOnSubscribe {
                vLoading.visibility = View.VISIBLE
                vError.visibility = View.GONE
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.code == 0) {
                    PManager.apply {
                        setName(etUserName.text.toString())
                        setSurname(etPhone.text.toString())
                    }
                    (requireActivity() as MainActivity).setData()
                } else {
                    Toast.makeText(requireContext(),"Unknown error",Toast.LENGTH_SHORT).show()
                }
                vLoading.visibility = View.GONE
                vError.visibility = View.GONE
            }, {
                vLoading.visibility = View.GONE
                vError.visibility = View.VISIBLE
            }))
    }

    private fun onCreateData() {
        activity?.title = getString(R.string.profile_edit_title)
        etUserName.setText(PManager.getName(), TextView.BufferType.EDITABLE)
        etPhone.setText(PManager.getSurname(), TextView.BufferType.EDITABLE)
        Picasso
            .get()
            .load(SERVER_IMAGE_ADDRESS + PManager.getImage())
            .placeholder(R.drawable.ic_image)
            .into(ivAvatar)


        ivAvatar.setOnClickListener {
//            startActivity(Intent(requireContext(), ImagePickerActivity::class.java).putExtra(1, true).putExtra(INTENT_FROM_SERVER, true))
        }
    }

    override fun onDestroyView() {
        compositeDisposable.dispose()
        compositeDisposable.clear()
        super.onDestroyView()
    }
}
