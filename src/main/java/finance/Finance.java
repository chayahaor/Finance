package finance;

import dagger.DaggerCurrencyComboBoxComponent;
import dagger.DaggerCurrencyExchangeComponent;
import helpers.*;
import json.CurrencyExchangeService;
import org.jfree.chart.ChartPanel;

import javax.inject.Provider;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;

import static main.Main.HOME_CURRENCY;


public class Finance extends JPanel {
    private double currentValue;
    private JLabel userValue;
    private Connection connection;
    private JComboBox<String> action;
    private JFormattedTextField amount;
    private JFormattedTextField fxRate;
    private DatePanel maturityDate;
    private JButton doAction;
    private CurrencyComboBox fromCurrency;
    private CurrencyComboBox toCurrency;

    public Finance(Connection connection) {
        this.connection = connection;
        this.currentValue = pullCurrentValue();
        setSize(900, 500);
        setLayout(new BorderLayout());
        add(doFinancePanel(), BorderLayout.NORTH);
        add(addGraph());
    }

    private double pullCurrentValue()
    {
        double retVal = 0;
        try
        {
            Statement stmt = connection.createStatement();
            // spGetMainData has
            ResultSet resultSet = stmt.executeQuery("Call spGetMainData();");
            ArrayList<Double> quantitiesPerCurrency = new ArrayList<>();
            //TODO: Pull value from database
            // Pull maindata table sorted by Currency
            // Until the currency is different,
            //      add up positive quantities from database
            //      converted to USD using CurrencyExchangeAPI
            //      unless current currency is HOME_CURRENCY
            //      add value to quantitiesPerCurrency
            // Loop through all doubles in quantitiesPerCurrency and add them up as retVal
            double sum = 0.0;
            String currentCurrency = "";
            double currentFX = 1;
            while (resultSet.next())
            {
                currentCurrency = resultSet.getString("StartCurrency");
                if (!currentCurrency.equals(HOME_CURRENCY))
                {
                    CurrencyExchanger currencyExchanger = DaggerCurrencyExchangeComponent.create().getCurrencyExchange();
                    currencyExchanger.doTheCurrencyExchange(Double.parseDouble(resultSet.getString("Amount")),
                            currentCurrency, HOME_CURRENCY);
                    currentFX = currencyExchanger.calculateFXRate();
                }
                sum += Double.parseDouble(resultSet.getString(1));
            }

            for (Double quantity : quantitiesPerCurrency)
            {
                retVal += quantity;
            }
        } catch (SQLException exception)
        {
            // if connection fails, use default
            retVal = 10000;
        }
        return retVal;
    }

    private JPanel doFinancePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setSize(new Dimension(500, 200));
        panel.add(addCurrentValue());
        panel.add(addActionComponents());
        return panel;
    }

    private JPanel addCurrentValue() {
        JPanel panel = new JPanel();
        NumberFormat moneyFormatter = NumberFormat.getCurrencyInstance();
        panel.add(new JLabel("Currently Have: "));
        userValue = new JLabel(moneyFormatter.format(currentValue));
        panel.add(userValue);
        return panel;
    }

    private JPanel addActionComponents() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        action = new JComboBox<>(new String[]{
                "Buy Spot",
                "Buy Forward",
                "Sell Short",
                "Sell Long",
                "Cover Short Position"});
        panel.add(action);

        toCurrency = DaggerCurrencyComboBoxComponent
                .create()
                .getCurrencyExchange();
        fromCurrency = DaggerCurrencyComboBoxComponent
                .create()
                .getCurrencyExchange();

        panel.add(toCurrency);
        panel.add(fromCurrency);

        toCurrency.addSymbols();
        fromCurrency.addSymbols();

        amount = new JFormattedTextField();
        amount.setValue(500);
        amount.setColumns(5);
        panel.add(amount);

        fxRate = new JFormattedTextField();
        fxRate.setValue(3.5);
        fxRate.setColumns(5);
        panel.add(fxRate);

        maturityDate = new DatePanel();
        panel.add(maturityDate);

        doAction = new JButton();
        doAction.setText("Perform Action");
        doAction.addActionListener(this::onClick);
        panel.add(doAction);
        return panel;
    }

    private void onClick(ActionEvent event) {
        //TODO: Store values in DB
        // GUI changes:
        // a) Validate that if one is selected, other is USD - ONLY
        // b) Cannot allow yesterday maturity date
        // c) Buy or Sell as only two options -- REMOVE this combobox altogether
        // d) Add labels to fields -- call FX Rate "Spot Price FX / " + HOME_CURRENCY
        // Database changes:
        // a) remove homecurrencytotal column,
        // b) remove endcurrency column,
        // c) rename fromcurrency to be currency
        // Perform action is going to make two database inserts
        // (Buy) 30 ILS - startcurrency is USD and endcurrency is ILS
        // add into database one row negative (30 / fxRate) USD
        // add into database one row positive 30 ILS
        // (Sell) 30 ILS - startcurrency is ILS and endcurrency is USD
        // add into database one row positive (30 / fxRate) USD
        // add into database one row negative 30 ILS
    }

    public JPanel addGraph() {
        PnL profitLoss = new PnL();
        JPanel graphPanel = new JPanel();
        graphPanel.setLayout(new BorderLayout());
        ChartPanel chartPanel = new ChartPanel(profitLoss.getChart());
        graphPanel.add(chartPanel, BorderLayout.CENTER);
        return graphPanel;
    }
}
