package net.someapitest.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.someapitest.data.api.CurrencyApi
import net.someapitest.data.datastore.CurrencyDataStore
import net.someapitest.data.datastore.CurrencyDataStoreImpl

@Module(includes = [NetworkModule::class])
@InstallIn(SingletonComponent::class)
class DataModule {

    @Provides
    fun provideCurrencyDataStore(api: CurrencyApi): CurrencyDataStore {
        return CurrencyDataStoreImpl(api)
    }
}