package ge.transitgeorgia.module.data.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ge.transitgeorgia.module.data.repository.RustaviTransportRepository
import ge.transitgeorgia.module.domain.repository.ITransportRepository
import ge.transitgeorgia.module.common.other.enums.SupportedCity
import ge.transitgeorgia.module.data.local.datastore.AppDataStore
import ge.transitgeorgia.module.data.repository.TbilisiTransportRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    fun provideTbilisiTransportRepository(
        dataStore: AppDataStore,
        tbilisi: TbilisiTransportRepository,
        rustavi: RustaviTransportRepository,
    ): ITransportRepository {
        val city = runBlocking { dataStore.city.firstOrNull() }
        return tbilisi
//        if (city == SupportedCity.RUSTAVI) {
//            rustavi
//        } else tbilisi
    }

}