package helpers;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import json.CurrencyExchange;
import json.CurrencyExchangeService;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Map;

// There are three instances of CurrencyComboBox created despite the @Singleton tag
@Singleton
public class CurrencyExchangePresenter
{
    private final Provider<CurrencyComboBox> viewProvider;
    private final CurrencyExchangeService model;
    private Disposable disposable;

    @Inject
    public CurrencyExchangePresenter(
            Provider<CurrencyComboBox> viewProvider,
            CurrencyExchangeService model)
    {
        this.viewProvider = viewProvider;
        this.model = model;
    }

    public void loadResultFromQuery(double amount, String fromComboBox, String toComboBox)
    {
        disposable = model.getCurrencyExchange(amount, fromComboBox, toComboBox)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(this::onNext, this::onError);
    }


    private void onNext(CurrencyExchange currencyExchange) {}

    private void onError(Throwable throwable) {}

    public void cancel()
    {
        if (disposable != null)
        {
            disposable.dispose();
        }
    }

    public void loadSymbolsChoices()
    {
        Disposable symbolsDisposable = model.getCurrencySymbols()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(this::onSymbolsNext);
    }

    private void onSymbolsNext(CurrencyExchange currencyExchange)
    {
        Map<String, CurrencyExchange.Symbol> symbols = currencyExchange.getSymbols();
        viewProvider.get().setSymbolsChoices(symbols);
    }
}