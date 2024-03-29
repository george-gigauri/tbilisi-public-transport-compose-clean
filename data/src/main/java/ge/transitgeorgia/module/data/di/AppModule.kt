package ge.transitgeorgia.module.data.di

import android.content.Context
import androidx.room.Room
import com.chuckerteam.chucker.api.ChuckerInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ge.transitgeorgia.common.other.extensions.ignoreAllSSLErrors
import ge.transitgeorgia.data.remote.api.TransportApi
import ge.transitgeorgia.data.repository.TransportRepository
import ge.transitgeorgia.domain.repository.ITransportRepository
import ge.transitgeorgia.module.common.util.AppLanguage
import ge.transitgeorgia.module.data.local.datastore.AppDataStore
import ge.transitgeorgia.module.data.local.db.AppDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    object Name {
        const val DISPATCHER_DEFAULT = "dispatcher_default"
        const val DISPATCHER_MAIN = "dispatcher_main"
        const val DISPATCHER_IO = "dispatcher_io"
        const val DISPATCHER_UNCONFINED = "dispatcher_unconfined"
    }

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Provides
    @Singleton
    fun provideAppDataStore(context: Context): AppDataStore = AppDataStore(context)

    @Provides
    fun provideRetrofit(context: Context, dataStore: AppDataStore): Retrofit {
        return Retrofit.Builder()
            .baseUrl(
                // Return value based on app language
                runBlocking {
                    val city = dataStore.city.first()
                    val language = dataStore.language.first()
                    if (language == AppLanguage.Language.ENG) city.baseUrlEng else city.baseUrl
                }
            )
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().apply {
                this.connectTimeout(35, TimeUnit.SECONDS)
                this.callTimeout(35, TimeUnit.SECONDS)
                this.writeTimeout(50, TimeUnit.SECONDS)
                this.readTimeout(50, TimeUnit.SECONDS)

                ignoreAllSSLErrors()
                addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                    chain.proceed(request.build())
                }
                addInterceptor(ChuckerInterceptor.Builder(context).build())
            }.build())
            .build()
    }

    @Provides
    @Singleton
    fun provideDatabase(context: Context) = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "transport_db"
    ).fallbackToDestructiveMigration().build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): TransportApi {
        return retrofit.create()
    }

    @Named(Name.DISPATCHER_DEFAULT)
    @Provides
    @Singleton
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Named(Name.DISPATCHER_MAIN)
    @Provides
    @Singleton
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @Named(Name.DISPATCHER_IO)
    @Provides
    @Singleton
    fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Named(Name.DISPATCHER_UNCONFINED)
    @Provides
    @Singleton
    fun provideUnconfinedDispatcher(): CoroutineDispatcher = Dispatchers.Unconfined

    @Provides
    @Singleton
    fun provideTransportRepository(
        api: TransportApi,
        db: AppDatabase,
        @Named(Name.DISPATCHER_IO) ioDispatcher: CoroutineDispatcher
    ): ITransportRepository = TransportRepository(api, db, ioDispatcher)
}