package ge.tbilisipublictransport.common.other.di

import android.content.Context
import androidx.room.Room
import com.chuckerteam.chucker.api.ChuckerInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ge.tbilisipublictransport.common.other.Const
import ge.tbilisipublictransport.common.other.extensions.ignoreAllSSLErrors
import ge.tbilisipublictransport.data.local.datastore.AppDataStore
import ge.tbilisipublictransport.data.local.db.AppDatabase
import ge.tbilisipublictransport.data.remote.api.TransportApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Provides
    @Singleton
    fun provideAppDataStore(context: Context): AppDataStore = AppDataStore(context)

    @Provides
    @Singleton
    fun provideRetrofit(context: Context): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Const.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().apply {
                ignoreAllSSLErrors()
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
}