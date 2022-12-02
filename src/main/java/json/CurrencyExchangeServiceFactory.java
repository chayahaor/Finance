/*
package json;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import javax.inject.Inject;

public class CurrencyExchangeServiceFactory {

    @Inject
    public CurrencyExchangeServiceFactory(){
    }

    public CurrencyExchangeService getInstance() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.exchangerate.host/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        return retrofit.create(CurrencyExchangeService.class);
    }

}
*/
