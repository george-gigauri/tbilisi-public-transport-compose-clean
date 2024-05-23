package ge.transitgeorgia.module.data.di.module

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ge.transitgeorgia.module.data.local.datastore.AppDataStore
import ge.transitgeorgia.module.data.local.db.AppDatabase
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
    @Singleton
    fun provideDatabase(context: Context) = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "transport_db"
    ).fallbackToDestructiveMigration().build()
}