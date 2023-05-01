package ge.transitgeorgia.data.local.datastore

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import ge.transitgeorgia.common.other.enums.SupportedCity
import ge.transitgeorgia.common.util.AppLanguage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDataStoreTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var appDataStore: AppDataStore

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        appDataStore = AppDataStore(context)
    }

    @After
    fun tearDown() {

    }

    @Test
    fun saveLanguage_ReturnsSuccess() = runBlocking {
        appDataStore.setLanguage(AppLanguage.Language.GEO)
        assertThat(appDataStore.language.first()).isEqualTo(AppLanguage.Language.GEO)
        appDataStore.setLanguage(AppLanguage.Language.ENG)
        assertThat(appDataStore.language.first()).isEqualTo(AppLanguage.Language.ENG)
    }

    @Test
    fun saveLastUpdatedCity_ReturnSuccess() = runBlocking {
        appDataStore.setLastUpdatedCityId(SupportedCity.RUSTAVI)
        assertThat(appDataStore.lastUpdatedCityId.first()).isEqualTo(SupportedCity.RUSTAVI.id)
        appDataStore.setLastUpdatedCityId(SupportedCity.TBILISI)
        assertThat(appDataStore.lastUpdatedCityId.first()).isEqualTo(SupportedCity.TBILISI.id)
    }

    @Test
    fun deleteDataLastUpdatedAt_ReturnSuccess() = runBlocking {
        appDataStore.deleteDataLastUpdatedAt()
        assertThat(appDataStore.dataLastUpdatedAt.first()).isNull()
    }

    @Test
    fun saveLastUpdatedAt_ReturnSuccess() = runBlocking {
        appDataStore.setDataLastUpdatedAt(System.currentTimeMillis())
        assertThat(appDataStore.dataLastUpdatedAt.first()).isNotNull()
    }
}