package json;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CurrencyExchangeService {
    @GET("convert?")
    Single<CurrencyExchange> getCurrencyExchange(@Query("from") String from, @Query("to") String to);

    @GET("symbols")
    Single<CurrencyExchange> getCurrencySymbols();
}
