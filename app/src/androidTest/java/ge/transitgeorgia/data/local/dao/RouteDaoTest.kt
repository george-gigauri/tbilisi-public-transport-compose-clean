package ge.transitgeorgia.data.local.dao

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import ge.transitgeorgia.common.other.enums.SupportedCity
import ge.transitgeorgia.data.local.db.AppDatabase
import ge.transitgeorgia.data.local.entity.RouteClicksEntity
import ge.transitgeorgia.data.local.entity.RouteEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RouteDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var dao: RouteDao

    @Before
    fun setupDatabase() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries()
            .build()
        dao = db.routeDao()
    }

    @After
    fun destroy() {
        db.close()
    }

    @Test
    fun insertRoutes_ReturnTopRoutes() = runBlocking {
        dao.insert(RouteEntity("1", "", "351", "", "", ""))
        dao.insert(RouteEntity("2", "", "351", "", "", ""))
        dao.insert(RouteEntity("3", "", "446", "", "", ""))
        dao.insert(RouteEntity("4", "", "446", "", "", ""))
        dao.insert(RouteEntity("5", "", "271", "", "", ""))
        dao.insert(RouteEntity("6", "", "304", "", "", ""))

        dao.insertClickEntity(RouteClicksEntity(null, 351, 0L))
        dao.insertClickEntity(RouteClicksEntity(null, 446, 0L))
        dao.insertClickEntity(RouteClicksEntity(null, 271, 0L))
        dao.insertClickEntity(RouteClicksEntity(null, 304, 0L))

        dao.increaseClickCount(351)
        dao.increaseClickCount(351)
        dao.increaseClickCount(351)
        dao.increaseClickCount(351)
        dao.increaseClickCount(446)
        dao.increaseClickCount(446)
        dao.increaseClickCount(351)
        dao.increaseClickCount(351)

        val topRoutes = dao.getTopRoutes()
        assertThat(topRoutes).isNotEmpty()
        assertThat(topRoutes.first().number).isEqualTo("351")
    }

    @Test
    fun deleteRoute_ReturnTrue() = runBlocking {
        dao.insert(RouteEntity("1", "", "351", "", "", ""))
        assertThat(dao.getAll().find { it.id == "1" }).isNotNull()
        dao.delete(RouteEntity("1", "", "351", "", "", ""))
        assertThat(dao.getAll().find { it.id == "1" }).isNull()
    }

    @Test
    fun Route_is_top_if_clicks_more_than_2() = runBlocking {
        dao.insert(RouteEntity("1", "", "351", "", "", ""))
        dao.insertClickEntity(RouteClicksEntity(null, 351, 3))
        assertThat(dao.isTop(351, SupportedCity.TBILISI.id)).isTrue()
    }

    @Test
    fun Route_is_not_Top_if_clicks_less_than_3() = runBlocking {
        dao.deleteAll()
        dao.insert(RouteEntity("1", "", "351", "", "", ""))
        dao.insertClickEntity(RouteClicksEntity(null, 351, 2))
        assertThat(dao.isTop(351, SupportedCity.TBILISI.id)).isFalse()
    }
}