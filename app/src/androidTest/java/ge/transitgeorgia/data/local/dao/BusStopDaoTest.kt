package ge.transitgeorgia.data.local.dao

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import ge.transitgeorgia.data.local.db.AppDatabase
import ge.transitgeorgia.data.local.entity.BusStopEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BusStopDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var dao: BusStopDao

    @Before
    fun setupDatabase() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries()
            .build()
        dao = db.busStopDao()
    }

    @After
    fun destroy() {
        db.close()
    }

    @Test
    fun insertItemToDatabase_ReturnTrue() = runBlocking {
        assertThat(dao.getStops()).isEmpty()
        dao.insert(BusStopEntity("1942", "1:1942", "Khergiani", 0.0, 0.0))
        assertThat(dao.getStops().find { it.id == "1942" }).isNotNull()
    }

    @Test
    fun deleteFromDatabase_ReturnTrue() = runBlocking {
        dao.insert(BusStopEntity("1942", "1:1942", "Khergiani", 0.0, 0.0))
        dao.deleteById("1942")
        assertThat(dao.getStops().find { it.id == "1942" }).isNull()
    }

    @Test
    fun deleteAllFromDatabase() = runBlocking {
        dao.insertAll(
            listOf(
                BusStopEntity("1942", "1:1942", "Khergiani", 0.0, 0.0),
                BusStopEntity("1943", "1:1942", "Khergiani", 0.0, 0.0),
                BusStopEntity("1944", "1:1942", "Khergiani", 0.0, 0.0),
                BusStopEntity("1945", "1:1942", "Khergiani", 0.0, 0.0)
            )
        )
        assertThat(dao.getStops()).isNotEmpty()
        dao.deleteAll()
        assertThat(dao.getStops()).isEmpty()
    }

    @Test
    fun insertDuplicateValues_shouldOverwrite_ReturnTrue() = runBlocking {
        dao.insert(BusStopEntity("1942", "1:1942", "Khergiani", 0.0, 0.0))
        dao.insert(BusStopEntity("1942", "1:1942", "Khergiani", 0.0, 0.0))
        assertThat(dao.getStops().filter { it.id == "1942" }).hasSize(1)
    }
}