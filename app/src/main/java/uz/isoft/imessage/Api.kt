package uz.isoft.imessage

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import io.reactivex.Single
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import uz.isoft.imessage.database.contact.ContactDatabase
import uz.isoft.imessage.database.message.MessageDatabase
import uz.isoft.imessage.database.temp.message.TempDatabase
import java.util.concurrent.TimeUnit

class ApiFactory {
    companion object {
        private var okClient: OkHttpClient? = null
        private var services: ApiService? = null
        private var contactDB: ContactDatabase? = null
        private var messageDB: MessageDatabase? = null
        private var tempDB: TempDatabase? = null


        fun getApiService(): ApiService {
            var service: ApiService? = services
            if (service == null) {
                synchronized(ApiFactory::class.java) {
                    service = services
                    if (service == null) {
                        services = buildSite()
                            .create(ApiService::class.java)
                        service = services
                    }
                }
            }
            return service ?: buildSite().create(ApiService::class.java)
        }

        fun getContactDataBase(context: Context): ContactDatabase {
            if (contactDB == null) {
                synchronized(ContactDatabase::class) {
                    contactDB = Room.databaseBuilder(
                        context.applicationContext,
                        ContactDatabase::class.java, "data.db"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return contactDB ?: synchronized(ContactDatabase::class) {
                Room.databaseBuilder(
                    context.applicationContext,
                    ContactDatabase::class.java, "data.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
            }
        }

        fun getMessage(context: Context):MessageDatabase{
            if (messageDB==null){
                synchronized(MessageDatabase::class){
                    messageDB = Room.databaseBuilder(
                        context.applicationContext,
                        MessageDatabase::class.java, "data"
                        )
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return messageDB?: synchronized(MessageDatabase::class){
                Room.databaseBuilder(context.applicationContext, MessageDatabase::class.java,"data")
                    .fallbackToDestructiveMigration()
                    .build()
            }
        }

        fun getTemp(context: Context):TempDatabase{
            if(tempDB==null){
                synchronized(TempDatabase::class){
                    tempDB = Room.databaseBuilder(context.applicationContext,
                    TempDatabase::class.java , "data")
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return tempDB?: synchronized(TempDatabase::class){
                Room.databaseBuilder(context.applicationContext,
                    TempDatabase::class.java,"data"
                )
                    .fallbackToDestructiveMigration()
                    .build()
            }
        }

        private fun buildSite(): Retrofit {
            return Retrofit.Builder()
                .baseUrl(SERVER_ADDRESS)
                .client(getClient())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        }


        private fun getClient(): OkHttpClient {
            var c: OkHttpClient? = okClient
            if (c == null) {
                synchronized(ApiFactory::class.java) {
                    okClient =
                            buildClient()
                    c = okClient
                }
            }
            return c ?: buildClient()
        }

        private fun buildClient(): OkHttpClient {
            val interceptor = Interceptor { chain ->
                val request = chain.request()
                request.newBuilder().addHeader("Cache-Control", "no-cache")
                    .cacheControl(CacheControl.FORCE_NETWORK)
                chain.proceed(request)
            }
            return OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .addInterceptor(interceptor)
                .hostnameVerifier { _, _ -> true }
                .readTimeout(20, TimeUnit.SECONDS)
                .connectTimeout(20 / 2, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .cache(null)
                .build()
        }
    }

}

interface ApiService {
    @Multipart
    @POST("reg")
    fun sendData(@Part image: MultipartBody.Part, @Part user: MultipartBody.Part?): Single<BaseResponse<User>>

    @Multipart
    @POST("reg")
    fun sendData(@Part user: MultipartBody.Part?): Single<BaseResponse<User>>

    @POST("contacts")
    fun sendContacts(@Body contact: ContactWrapper): Single<BaseResponse<ArrayList<Contact>>>

    @GET("auth")
    fun checkUser(@Header("uid") uid:String) : Single<BaseResponse<User>>

//    @GET("agent/statistics")
//    fun getStatistics(@Header("key") key: String): Single<BaseResponse<StatisticsWrapper>>

}
