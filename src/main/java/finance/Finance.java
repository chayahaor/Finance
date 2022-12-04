package finance;

import helpers.*;
import org.jfree.chart.ChartPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.Date;

import static main.Main.HOME_CURRENCY;

public class Finance extends JPanel
{
    private double currentValue;
    private JFormattedTextField riskFreeRate;
    private JLabel userValue;
    private Connection connection;
    private JComboBox<String> action;
    private String[] actions = new String[]{"Buy", "Sell"};
    private JFormattedTextField amount;
    private JFormattedTextField fxRate;
    private DatePanel maturityDate;
    private JButton doAction;
    private CurrencyExchanger exchanger;

    private JComboBox<String> currency;

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
        panel.setSize(new Dimension(500, 300));
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

        panel.add(new JLabel("Risk Free Rate of " + HOME_CURRENCY + ":"));
        riskFreeRate = new JFormattedTextField();
        riskFreeRate.setValue(3.5);
        riskFreeRate.setColumns(5);
        panel.add(riskFreeRate);

        return panel;
    }

    private JPanel addActionComponents()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel top = new JPanel();
        JPanel bottom = new JPanel();

        top.add(new JLabel("Action:"));
        action = new JComboBox<>(actions);
        top.add(action);

        top.add(new JLabel("Currency:"));
        currency = exchanger.getActionCurrency();
        currency.setEditable(false);
        currency.setSelectedItem(HOME_CURRENCY);
        top.add(currency);

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
        maturityDate = new DatePanel();
        bottom.add(maturityDate);

        doAction = new JButton();
        doAction.setText("Perform Action");
        doAction.addActionListener(this::onClick);
        bottom.add(doAction);

        panel.add(top);
        panel.add(bottom);

        return panel;
    }

    private void onClick(ActionEvent event)
    {
        int actionID = (Objects.equals(action.getSelectedItem(), "Buy") ? 1 : 2);

        try
        {
            LocalDate today = LocalDate.now();
            String formatted = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH).format(today);

            Statement stmt = connection.createStatement();
            stmt.executeQuery("Call spInsertMainData ("
                + "'" + formatted + "', " + actionID + ", '"
                + currency.getSelectedItem() + "', '" + maturityDate.toString()
                + "', " + Double.parseDouble(amount.getText())  + ", "
                + Double.parseDouble(fxRate.getText()) + ")");

            JOptionPane.showMessageDialog(this, "Row Inserted Successfully!");
        }
        catch (SQLException e)
        {
            JOptionPane.showMessageDialog(this, e);
        }
        //TODO: Cannot allow yesterday maturity date
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
