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
    private JComboBox<String> actionCurrency;
    private double result = 1;
    private double rate = 1;

    @Inject
    public CurrencyExchanger(CurrencyExchangePresenter presenter)
    {
        this.presenter = presenter;

        CurrencyExchangeServiceFactory factory = new CurrencyExchangeServiceFactory();

        currencies = new JComboBox<>();
        actionCurrency = new JComboBox<>();

        presenter.loadSymbolsChoices();
    }

    public void setSymbolsChoices(Map<String, Symbol> symbols)
    {
        String[] symbolsArray = symbols.keySet().toArray(new String[0]);

        currencies.removeAllItems();
        actionCurrency.removeAllItems();

        for (String symbol : symbolsArray)
        {
            currencies.addItem(symbol);
            actionCurrency.addItem(symbol);
        }
    }

    public JComboBox<String> getCurrencies()
    {
        return this.currencies;
    }

    public JComboBox<String> getActionCurrency()
    {
        this.actionCurrency.removeItem(HOME_CURRENCY);
        return this.actionCurrency;
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

    public void setRate(double rate)
    {
        this.rate = rate;
    }

    public double getRate()
    {
        return this.rate;
    }

    public void showError()
    {
        JOptionPane.showMessageDialog(this, "Something went wrong");
    }
}

