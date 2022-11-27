package helpers;

import json.CurrencyExchange;
import json.CurrencyExchangeServiceFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import java.util.Map;

import static main.Main.HOME_CURRENCY;

@Singleton
public class CurrencyComboBox extends JComboBox<String>
{
    private final CurrencyExchangePresenter presenter;
    @Inject
    public CurrencyComboBox(CurrencyExchangePresenter presenter)
    {
        this.presenter = presenter;
        new CurrencyExchangeServiceFactory();
    }

    public void setSymbolsChoices(Map<String, CurrencyExchange.Symbol> symbols)
    {
        String[] symbolsArray = symbols.keySet().toArray(new String[0]);

        this.removeAllItems();

        for (String symbol : symbolsArray)
        {
            addItem(symbol);
        }

        setSelectedItem(HOME_CURRENCY);
        setEditable(false);
    }

    public void addSymbols()
    {
        presenter.loadSymbolsChoices();
    }
}
