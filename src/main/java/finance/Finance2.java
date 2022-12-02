
package finance;

import api.API;
import org.jdatepicker.JDatePicker;
import org.jfree.chart.ChartPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;


public class Finance2 extends JPanel {
    private static final String HOME_CURRENCY = "USD";
    private double currentValue;
    private JLabel userValue;
    private Connection connection;
    private JComboBox<String> action;
    private JFormattedTextField amount;
    private JFormattedTextField fxRate;
    private JButton doAction;
    private API api;
    private JDatePicker maturityDate;


    public Finance2(Connection connection) throws IOException {
        api = new API();
        this.connection = connection;
        this.currentValue = pullCurrentValue();
        setSize(900, 500);
        setLayout(new BorderLayout());
        add(doFinancePanel(), BorderLayout.NORTH);
        add(addGraph());
    }

    private double pullCurrentValue() {
        double retVal = 0;
        try
        {
            Statement stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery("Call spGetMainData();");
            HashMap<String, Double> quantitiesPerCurrency = new HashMap<>();
            //TODO: Pull value from database
            // Pull maindata table sorted by Currency
            // Until the currency is different,
            //      add up quantities from database
            //      converted to USD using CurrencyExchangeAPI
            //      based on maturity date
            //      unless current currency is HOME_CURRENCY
            //      add value to quantitiesPerCurrency
            // Loop through all doubles in quantitiesPerCurrency and add them up as retVal
            double sum;
            String currentCurrency;
            while (resultSet.next())
            {
                currentCurrency = resultSet.getString("Currency");

                //TODO: what is sum??
                sum = quantitiesPerCurrency.get(currentCurrency) == null
                        ? 0.0 : quantitiesPerCurrency.get(currentCurrency);

                double amount = Double.parseDouble(resultSet.getString("Amount"));
                if (!currentCurrency.equals(HOME_CURRENCY))
                {
                    // TODO: somehow convert to current value from today - buy/sell date
                    //  OR maturity - buy/sell if today is later than maturity using formula * amount

                    //TODO: call in different order if buying or selling.
                    // With new DB, amount will be negative is selling
                    amount = amount * api.convert(HOME_CURRENCY, currentCurrency);
                    sum += amount;

                    //amount = resultSet.getString("Action").equals("Buy") ? amount : -amount;
                    //sum = (amount < 0) ? sum - exchanger.getResult() : sum + exchanger.getResult();
                } else
                {
                    sum = amount;
                }

                quantitiesPerCurrency.put(currentCurrency, sum);
            }

            for (String currency : quantitiesPerCurrency.keySet())
            {
                retVal += quantitiesPerCurrency.get(currency);
            }
        } catch (SQLException exception)
        {
            // if connection fails, use default
            retVal = 10000;
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return retVal;
    }

    private JPanel doFinancePanel() throws IOException {
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
        panel.add(new JLabel("Current NPV: "));
        userValue = new JLabel(moneyFormatter.format(currentValue));
        panel.add(userValue);
        return panel;
    }

    private JPanel addActionComponents() throws IOException {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        action = new JComboBox<>(new String[]{
                "Buy",
                "Sell"});
        panel.add(action);

        ArrayList<String> currencyList = api.getSymbolResults();
        JComboBox<String> currencies = new JComboBox<>();
        for (String curr : currencyList)
        {
            currencies.addItem(curr);

        }
        panel.add(currencies);

        amount = new JFormattedTextField();
        amount.setValue(500);
        amount.setColumns(5);
        panel.add(amount);

        fxRate = new JFormattedTextField();
        fxRate.setValue(3.5);
        fxRate.setColumns(5);
        panel.add(fxRate);

        maturityDate = new JDatePicker();
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
        // a) *** NO NEED, ONLY ONE CURRENCY ON GUI, NOT allowed to be USD ***
        // b) Cannot allow yesterday maturity date
        // c) Buy or Sell as only two options
        // d) Add labels to fields -- call FX Rate "Spot Price FX / " + HOME_CURRENCY
        // e) USD is NOT allowed to be selected
        // Database changes:
        // a) remove homecurrencytotal column,
        // b) remove endcurrency column,
        // c) rename fromcurrency to be currency
        // Perform action is going to make two database inserts -- NOT TRUE -> ONLY ONE
        // (Buy) 30 ILS - startcurrency is USD and endcurrency is ILS -- ONLY ILS IS NECESSARY
        // add into database one row negative (30 / fxRate) USD -- THIS ROW DOES NOT GO IN DATABASE
        // add into database one row positive 30 ILS
        // (Sell) 30 ILS - startcurrency is ILS and endcurrency is USD -- ONLY ILS IS NECESSARY
        // add into database one row positive (30 / fxRate) USD -- THIS ROW DOES NOT GO IN DATABASE
        // add into database one row negative 30 ILS
        // NOTE: RACHEL must use API to exchange database row of currency to USD and add that up
        // REMOVE start and end currency - only one currency allowed
        // buy/sell amount currency
        // REMOVE PNL TABLE - PNL IS CALCULATED
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

