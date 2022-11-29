package helpers;

import json.CurrencyExchangeServiceFactory;

import javax.inject.Inject;
import javax.swing.*;

public class CurrencyExchanger extends JComponent
{
    private CurrencyExchangePresenter presenter;
    private double fxRate = 1;

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

    public double calculateFXRate()
    {
        return this.fxRate;
    }

    public void getCurrencyExchangeValue(double result)
    {
        this.fxRate = result;
    }

    public void showError()
    {
        JOptionPane.showMessageDialog(this, "Something went wrong with Currency Exchange");
    }
}
