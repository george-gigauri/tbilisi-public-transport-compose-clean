package ge.transitgeorgia.module.data.di.module

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ge.transitgeorgia.common.other.extensions.ignoreAllSSLErrors
import ge.transitgeorgia.data.remote.api.RustaviTransportApi
import ge.transitgeorgia.module.common.other.enums.SupportedCity
import ge.transitgeorgia.module.common.util.AppLanguage
import ge.transitgeorgia.module.data.BuildConfig
import ge.transitgeorgia.module.data.di.qualifier.Rustavi
import ge.transitgeorgia.module.data.di.qualifier.Tbilisi
import ge.transitgeorgia.module.data.local.datastore.AppDataStore
import ge.transitgeorgia.module.data.remote.api.TbilisiTransportApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

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
                    val locale =
                        runBlocking { dataStore.language.firstOrNull() ?: AppLanguage.Language.GEO }
                    val city = runBlocking {
                        dataStore.city.firstOrNull()
                    } ?: SupportedCity.TBILISI

                    val request = chain.request().newBuilder()
                    if (city == SupportedCity.TBILISI) {
                        request.addHeader("X-api-key", BuildConfig.X_API_KEY)
                        request.url(
                            chain.request().url.newBuilder()
                                .addQueryParameter("locale", locale.value)
                                .build()
                        )
                    }
                    chain.proceed(request.build())
                }
                addInterceptor(ChuckerInterceptor.Builder(context).build())
            }.build())
            .build()
    }

    @Provides
    @Singleton
    fun provideTbilisiApiService(retrofit: Retrofit): TbilisiTransportApi {
        return retrofit.create()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): RustaviTransportApi {
        return retrofit.create()
    }
}