package finance;

import helpers.*;
import org.jfree.chart.ChartPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static main.Main.HOME_CURRENCY;


public class Finance extends JPanel
{
    private double currentValue;
    private JLabel userValue;
    private Connection connection;
    private JComboBox<String> action;
    private JFormattedTextField amount;
    private JFormattedTextField fxRate;
    private DatePanel maturityDate;
    private JButton doAction;
    private CurrencyExchanger exchanger;

    private JComboBox<String> fromCurrency;

    private JComboBox<String> toCurrency;

    public Finance(Connection connection, CurrencyExchanger exchanger)
    {
        this.connection = connection;
        this.exchanger = exchanger;
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
            // spGetMainData returns the `maindata` table sorted by currencies
            ResultSet resultSet = stmt.executeQuery("Call spGetMainData();");
            HashMap<String, Double> quantitiesPerCurrency = new HashMap<>();
            double sum;
            double currentRate = 1;
            String currentCurrency = "";
            while (resultSet.next())
            {
                boolean allSame = currentCurrency.equals(resultSet.getString("Currency"));
                currentCurrency = resultSet.getString("Currency");
                sum = quantitiesPerCurrency.get(currentCurrency) == null
                        ? 0.0 : quantitiesPerCurrency.get(currentCurrency);

                double amount = Double.parseDouble(resultSet.getString("Amount"));
                if (!currentCurrency.equals(HOME_CURRENCY))
                {
                    // get difference in days between today and action date
                    // (or between maturity date and action date if maturity date already passed)
                    Date actionDate = resultSet.getTimestamp("ActionDate");
                    Date maturityDate = resultSet.getTimestamp("MaturityDate");
                    Date today = new Date();
                    long diffInMs = (maturityDate.getTime() - today.getTime() < 0)
                            ? maturityDate.getTime() - actionDate.getTime()
                            : today.getTime() - actionDate.getTime();
                    double diffInDays = TimeUnit.DAYS.convert(diffInMs, TimeUnit.MILLISECONDS);

                    if (allSame)
                    {
                        // use the already existing conversion
                        amount *= currentRate;
                        amount = resultSet.getString("Action").equals("Sell")
                                ? -amount : amount;
                    }
                    else
                    {
                        // convert to currency
                        exchanger.exchange(amount, currentCurrency, HOME_CURRENCY);
                        amount = resultSet.getString("Action").equals("Sell")
                                ? -(exchanger.getResult()) : exchanger.getResult();
                        currentRate = exchanger.getRate();
                    }

                    // translate based on maturity date
                    // TODO: GET THE RISK FREE RATE OF HOME_CURRENCY
                    double riskFreeRate = 2.0;
                    amount += amount * (1 + (diffInDays / 365.0) * riskFreeRate);
                    sum += amount;
                } else {
                    // Home Currency can only be Action = Initial -- no exchange or maturity date calculation
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
        }
        return retVal;
    }

    private JPanel doFinancePanel()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setSize(new Dimension(500, 200));
        panel.add(addCurrentValue());
        panel.add(addActionComponents());
        return panel;
    }

    private JPanel addCurrentValue()
    {
        JPanel panel = new JPanel();
        NumberFormat moneyFormatter = NumberFormat.getCurrencyInstance();
        panel.add(new JLabel("Current NPV: "));
        userValue = new JLabel(moneyFormatter.format(currentValue));
        panel.add(userValue);
        return panel;
    }

    private JPanel addActionComponents()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        action = new JComboBox<>(new String[]{
                "Buy Spot",
                "Buy Forward",
                "Sell Short",
                "Sell Long",
                "Cover Short Position"});
        panel.add(action);

        fromCurrency = exchanger.getFromCurrency();
        toCurrency = exchanger.getToCurrency();
        fromCurrency.setEditable(false);
        fromCurrency.setSelectedItem(HOME_CURRENCY);
        toCurrency.setEditable(false);
        toCurrency.setSelectedItem(HOME_CURRENCY);
        panel.add(fromCurrency);
        panel.add(toCurrency);

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

    private void onClick(ActionEvent event)
    {
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

    public JPanel addGraph()
    {
        PnL profitLoss = new PnL();
        JPanel graphPanel = new JPanel();
        graphPanel.setLayout(new BorderLayout());
        ChartPanel chartPanel = new ChartPanel(profitLoss.getChart());
        graphPanel.add(chartPanel, BorderLayout.CENTER);
        return graphPanel;
    }
}
