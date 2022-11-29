package helpers;

import json.CurrencyExchangeServiceFactory;
import json.Symbol;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import java.util.Map;

import static main.Main.HOME_CURRENCY;

@Singleton
public class CurrencyExchanger extends JComponent
{
    private CurrencyExchangePresenter presenter;
    private JComboBox<String> currencies;
    private double result = 1;

    @Inject
    public CurrencyExchanger(CurrencyExchangePresenter presenter)
    {
        this.presenter = presenter;

        CurrencyExchangeServiceFactory factory = new CurrencyExchangeServiceFactory();

        currencies = new JComboBox<>();

        presenter.loadSymbolsChoices();
    }

    public void setSymbolsChoices(Map<String, Symbol> symbols)
    {
        String[] symbolsArray = symbols.keySet().toArray(new String[0]);

        currencies.removeAllItems();

        for (String symbol : symbolsArray)
        {
            currencies.addItem(symbol);
        }

        currencies.setSelectedItem(HOME_CURRENCY);
        currencies.setEditable(false);
    }

    public JComboBox<String> getCurrencies()
    {
        return currencies;
    }

    public void exchange(double amount, String fromCurrency, String toCurrency)
    {
        presenter.loadResultFromQuery(amount, fromCurrency, toCurrency);
    }

    public void setResult(double result)
    {
        this.result = result;
    }

    public void showError()
    {
        JOptionPane.showMessageDialog(this, "Something went wrong");
    }
}

