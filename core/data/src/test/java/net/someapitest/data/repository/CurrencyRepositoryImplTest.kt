
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import net.someapitest.data.datastore.CurrencyDataStore
import net.someapitest.data.repository.CurrencyRepositoryImpl
import net.someapitest.domain.models.Rates
import net.someapitest.domain.models.SupportedCurrency
import net.someapitest.domain.result.NetworkStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CurrencyRepositoryImplTest {

    @Mock
    private lateinit var currencyDataStore: CurrencyDataStore
    private lateinit var repository: CurrencyRepositoryImpl

    @Before
    fun setup() {
        currencyDataStore = mock()
        repository = CurrencyRepositoryImpl(currencyDataStore)
    }

    @Test
    fun `test getRates returns flow of rates`() = runTest {
        val expectedRates = Rates(SupportedCurrency.EUR, "2024-10-01", emptyList())
        whenever(currencyDataStore.getRates()).thenReturn(
            NetworkStatus.Success(expectedRates)
        )

        val flow = repository.getRates().toList()

        assertTrue(flow[0] is NetworkStatus.Loading)
        assertEquals((flow[1] as NetworkStatus.Success).data, expectedRates)
    }
}
