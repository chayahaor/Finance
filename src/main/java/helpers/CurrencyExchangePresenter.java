package helpers;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import json.CurrencyExchange;
import json.CurrencyExchangeService;
import json.Symbol;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class CurrencyExchangePresenter
{
    private final Provider<CurrencyExchanger> viewProvider;
    private final CurrencyExchangeService model;
    private Disposable disposable;
    private Disposable symbolsDisposable;

    @Inject
    public CurrencyExchangePresenter(
            Provider<CurrencyExchanger> viewProvider,
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

    public void cancel()
    {
        if (disposable != null)
        {
            disposable.dispose();
        }
    }

    private void onNext(CurrencyExchange currencyExchange)
    {
        double result = currencyExchange.getResult();
        viewProvider.get().setResult(result);
    }

    private void onError(Throwable throwable)
    {
        throwable.printStackTrace();
        viewProvider.get().showError();
    }

    public void loadSymbolsChoices()
    {
        symbolsDisposable = model.getCurrencySymbols()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(this::onSymbolsNext);
    }

    private void onSymbolsNext(CurrencyExchange object)
    {
        Map<String, Symbol> symbols = object.getSymbols();
        viewProvider.get().setSymbolsChoices(symbols);
    }
}
