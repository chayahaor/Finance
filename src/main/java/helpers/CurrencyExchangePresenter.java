package helpers;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import json.CurrencyExchange;
import json.CurrencyExchangeService;

import javax.inject.Inject;
import javax.inject.Provider;

public class CurrencyExchangePresenter
{
    private final Provider<CurrencyExchanger> viewProvider;
    private final CurrencyExchangeService model;
    private Disposable disposable;

    @Inject
    public CurrencyExchangePresenter(
            Provider<CurrencyExchanger> viewProvider,
            CurrencyExchangeService model)
    {
        this.viewProvider = viewProvider;
        this.model = model;
    }

    public void loadResultFromQuery(double amount, String fromCurrency, String toCurrency)
    {
        disposable = model.getCurrencyExchange(amount, fromCurrency, toCurrency)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(this::onNext, this::onError);
    }

    private void onNext(CurrencyExchange currencyExchange)
    {
        System.out.println(currencyExchange.getResult());
        viewProvider.get().setValue(currencyExchange.getResult());
    }

    private void onError(Throwable throwable)
    {
        throwable.printStackTrace();
        viewProvider.get().showError();
    }

    public void cancel()
    {
        if (disposable != null)
        {
            disposable.dispose();
        }
    }
}
