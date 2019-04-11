package uz.isoft.imessage


import android.preference.PreferenceManager

object PManager{
    private val preferenceManager by lazy {
        return@lazy PreferenceManager.getDefaultSharedPreferences(App.getInstance())
    }

    fun getPhone() = preferenceManager.getString("phone", "") ?: ""
    fun setPhone(phone: String) = preferenceManager.edit().putString("phone", phone).apply()

    fun getUID() = preferenceManager.getString("uid", "") ?: ""
    fun setUID(uid: String) = preferenceManager.edit().putString("uid", uid).apply()

    fun getName() = preferenceManager.getString("name", "") ?: ""
    fun setName(name: String) = preferenceManager.edit().putString("name", name).apply()

    fun getSurname() = preferenceManager.getString("surname", "") ?: ""
    fun setSurname(name: String) = preferenceManager.edit().putString("surname", name).apply()

    fun getImage() = preferenceManager.getString("image", "") ?: ""
    fun setImage(image: String) = preferenceManager.edit().putString("image", image).apply()

    fun setToken(token:String) = preferenceManager.edit().putString("token", token).apply()
    fun getToken() = preferenceManager.getString("token", "")?:""

    fun clearData(){
        preferenceManager.edit().putString("image", "").apply()
        preferenceManager.edit().putString("name", "").apply()
        preferenceManager.edit().putString("surname", "").apply()
        preferenceManager.edit().putString("phone", "").apply()
        preferenceManager.edit().putString("token", "").apply()
    }


}