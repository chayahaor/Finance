/*
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
    private JComboBox<String> toCurrency;
    private JComboBox<String> fromCurrency;
    private double result = 1;

    @Inject
    public CurrencyExchanger(CurrencyExchangePresenter presenter)
    {
        this.presenter = presenter;

        CurrencyExchangeServiceFactory factory = new CurrencyExchangeServiceFactory();

        currencies = new JComboBox<>();
        toCurrency = new JComboBox<>();
        fromCurrency = new JComboBox<>();

        presenter.loadSymbolsChoices();
    }

    public void setSymbolsChoices(Map<String, Symbol> symbols)
    {
        String[] symbolsArray = symbols.keySet().toArray(new String[0]);

        currencies.removeAllItems();
        toCurrency.removeAllItems();
        fromCurrency.removeAllItems();

        for (String symbol : symbolsArray)
        {
            currencies.addItem(symbol);
            toCurrency.addItem(symbol);
            fromCurrency.addItem(symbol);
        }
    }

    public JComboBox<String> getCurrencies()
    {
        return this.currencies;
    }

    public JComboBox<String> getToCurrency()
    {
        return this.toCurrency;
    }

    public JComboBox<String> getFromCurrency()
    {
        return this.fromCurrency;
    }

    public void exchange(double amount, String fromCurrency, String toCurrency)
    {
        presenter.loadResultFromQuery(amount, fromCurrency, toCurrency);
    }

    public void setResult(double result)
    {
        this.result = result;
    }

    public double getResult()
    {
        return this.result;
    }

    public void showError()
    {
        JOptionPane.showMessageDialog(this, "Something went wrong");
    }
}

*/
