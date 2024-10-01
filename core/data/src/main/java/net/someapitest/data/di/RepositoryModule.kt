package net.someapitest.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.someapitest.data.repository.CurrencyRepositoryImpl
import net.someapitest.domain.repositories.CurrencyRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindDeviceRepository(repositoryImpl: CurrencyRepositoryImpl): CurrencyRepository
}