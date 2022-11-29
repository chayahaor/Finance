package helpers;

import json.CurrencyExchangeServiceFactory;

import javax.inject.Inject;
import javax.swing.*;

public class CurrencyExchanger extends JComponent
{
    private final CurrencyExchangePresenter presenter;
    private double exchangedValue = 1;

    @Inject
    public CurrencyExchanger(CurrencyExchangePresenter presenter)
    {
        this.presenter = presenter;
        new CurrencyExchangeServiceFactory();
    }

    public void doTheCurrencyExchange(double amount, String fromCurrency, String toCurrency)
    {
        presenter.loadResultFromQuery(amount, fromCurrency, toCurrency);
    }

    public void showError()
    {
        JOptionPane.showMessageDialog(this, "Something went wrong with Currency Exchange");
    }

    public void setValue(double result)
    {
        this.exchangedValue = result;
    }

    public double getExchangedValue()
    {
        return this.exchangedValue;
    }
}
