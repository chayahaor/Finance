
package finance;

import api.API;
import com.toedter.calendar.JDateChooser;
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class Finance2 extends JPanel {
    private static final String HOME_CURRENCY = "USD";
    private double currentValue;
    private JFormattedTextField riskFreeRate;
    private JLabel userValue;
    private Connection connection;
    private JComboBox<String> action;
    private JFormattedTextField amount;
    private JFormattedTextField fxRate;
    private JDateChooser maturityDate;
    private JButton doAction;
    private API api;

    private JComboBox<String> jComboCurrency;


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
                    amount = amount * api.convert(currentCurrency, HOME_CURRENCY);
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
        panel.setSize(new Dimension(500, 300));
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

        panel.add(new JLabel("Risk Free Rate of " + HOME_CURRENCY + ":"));
        riskFreeRate = new JFormattedTextField();
        riskFreeRate.setValue(3.5);
        riskFreeRate.setColumns(5);
        panel.add(riskFreeRate);

        return panel;
    }

    private JPanel addActionComponents() throws IOException {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel top = new JPanel();
        JPanel bottom = new JPanel();

        top.add(new JLabel("Action:"));
        action = new JComboBox<>(new String[]{
                "Buy",
                "Sell"});
        top.add(action);

        top.add(new JLabel("Currency:"));

        ArrayList<String> currencyList = api.getSymbolResults();
        jComboCurrency = new JComboBox<>();
        for (String curr : currencyList)
        {
            if (!curr.equals("USD"))
                jComboCurrency.addItem(curr);

        }
        jComboCurrency.setEditable(false);
        top.add(jComboCurrency);

        top.add(new JLabel("Quantity:"));
        amount = new JFormattedTextField();
        amount.setValue(500);
        amount.setColumns(5);
        top.add(amount);

        top.add(new JLabel("Spot Price FX / " + HOME_CURRENCY + ":"));
        fxRate = new JFormattedTextField();
        fxRate.setValue(3.5);
        fxRate.setColumns(5);
        top.add(fxRate);

        bottom.add(new JLabel("Maturity Date:"));
        maturityDate = new JDateChooser();
        maturityDate.setMinSelectableDate(new Date());

        bottom.add(maturityDate);

        doAction = new JButton();
        doAction.setText("Perform Action");
        doAction.addActionListener(this::onClick);
        bottom.add(doAction);
        panel.add(top);
        panel.add(bottom);
        return panel;
    }

    private void onClick(ActionEvent event) {
        int actionID = (Objects.equals(action.getSelectedItem(), "Buy") ? 1 : 2);

        try
        {
            LocalDate today = LocalDate.now();
            String formattedToday = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH).format(today);

            Date maturitySelected = maturityDate.getDate();
            LocalDate mature = LocalDate.ofInstant(maturitySelected.toInstant(), ZoneId.systemDefault());

            String formattedMaturity = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
                    .format(mature);

            System.out.println(formattedMaturity);
            Statement stmt = connection.createStatement();
            stmt.executeQuery("Call spInsertMainData ("
                              + "'" + formattedToday + "', " + actionID + ", '"
                              + jComboCurrency.getSelectedItem() + "', '" + formattedMaturity
                              + "', " + Double.parseDouble(amount.getText()) + ", "
                              + Double.parseDouble(fxRate.getText()) + ")");

            JOptionPane.showMessageDialog(this, "Row Inserted Successfully!");
        } catch (SQLException e)
        {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
        //TODO: Cannot allow yesterday maturity date
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

