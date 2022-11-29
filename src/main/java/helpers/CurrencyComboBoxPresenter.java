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

// There are three instances of CurrencyComboBox created despite the @Singleton tag
@Singleton
public class CurrencyComboBoxPresenter
{
    private final Provider<CurrencyComboBox> viewProvider;
    private final CurrencyExchangeService model;
    private Disposable disposable;

    @Inject
    public CurrencyComboBoxPresenter(
            Provider<CurrencyComboBox> viewProvider,
            CurrencyExchangeService model)
    {
        this.viewProvider = viewProvider;
        this.model = model;
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
        Map<String, Symbol> symbols = currencyExchange.getSymbols();
        viewProvider.get().setSymbolsChoices(symbols);
    }
}