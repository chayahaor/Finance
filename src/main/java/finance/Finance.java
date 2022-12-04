package finance;

import helpers.*;
import org.jfree.chart.ChartPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.Date; // Tells code to use java.util.Date rather than java.sql.Date

import static main.Main.HOME_CURRENCY;

public class Finance extends JPanel
{
    private JFormattedTextField riskFreeRate;
    private Connection connection;
    private JComboBox<String> action;
    private final String[] actions = new String[]{"Buy", "Sell"};
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
        setSize(900, 500);
        setLayout(new BorderLayout());
        add(doFinancePanel(), BorderLayout.NORTH);
        add(addGraph());
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
        DecimalFormat decimalFormat = new DecimalFormat("0.######");
        panel.add(new JLabel("Risk Free Rate of " + HOME_CURRENCY + ":"));
        riskFreeRate = new JFormattedTextField(decimalFormat);
        riskFreeRate.setValue(3.5);
        riskFreeRate.setColumns(5);
        panel.add(riskFreeRate);

        JButton btnGetCurrentValue = new JButton("Get Current NPV");
        btnGetCurrentValue.addActionListener(this::pullCurrentValue);
        panel.add(btnGetCurrentValue);
        return panel;
    }

    private void pullCurrentValue(ActionEvent actionEvent)
    {
        double currentValue = 0;
        try
        {
            Statement stmt = connection.createStatement();
            // spGetMainData returns the `maindata` table sorted by currencies
            ResultSet resultSet = stmt.executeQuery("Call spGetMainData();");
            HashMap<String, Double> quantitiesPerCurrency = new HashMap<>();
            double sum;
            while (resultSet.next())
            {
                calculateRowCurrentValue(resultSet, quantitiesPerCurrency);
            }

            for (String currency : quantitiesPerCurrency.keySet())
            {
                currentValue += quantitiesPerCurrency.get(currency);
            }
        } catch (Exception exception)
        {
            currentValue = 10000; // default value if something goes wrong
        }

        NumberFormat moneyFormatter = NumberFormat.getCurrencyInstance();
        JOptionPane.showMessageDialog(this, "Current NPV: " + moneyFormatter.format(currentValue));
    }

    private void calculateRowCurrentValue(ResultSet resultSet, HashMap<String, Double> quantitiesPerCurrency) throws SQLException
    {
        double sum;
        String currentCurrency = resultSet.getString("Currency");
        sum = quantitiesPerCurrency.get(currentCurrency) == null
                ? 0.0 : quantitiesPerCurrency.get(currentCurrency);

        double quantity = Double.parseDouble(resultSet.getString("Amount"));
        // get difference in days between today and action date
        // (or between maturity date and action date if maturity date already passed)
        Date actionDate = resultSet.getTimestamp("ActionDate");
        Date maturityDate = resultSet.getTimestamp("MaturityDate");
        Date today = new Date();
        long diffInMs = (maturityDate.getTime() - today.getTime() < 0)
                ? maturityDate.getTime() - actionDate.getTime()
                : today.getTime() - actionDate.getTime();
        double diffInDays = TimeUnit.DAYS.convert(diffInMs, TimeUnit.MILLISECONDS);

        // convert to currency and apply maturity date formula
        exchanger.convert(currentCurrency, HOME_CURRENCY);
        double value = resultSet.getString("Action").equals("Sell") ?
                -(quantity / exchanger.getRate()) : (quantity / exchanger.getRate());
        sum += value * (1 + (diffInDays / 365.0) * Double.parseDouble(riskFreeRate.getText()));

        quantitiesPerCurrency.put(currentCurrency, sum);
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
        top.add(currency);

        DecimalFormat decimalFormat = new DecimalFormat("0.##");
        top.add(new JLabel("Quantity:"));
        amount = new JFormattedTextField(decimalFormat);
        amount.setValue(500);
        amount.setColumns(5);
        top.add(amount);

        decimalFormat = new DecimalFormat("0.######");
        top.add(new JLabel("Spot Price FX / " + HOME_CURRENCY + ":"));
        fxRate = new JFormattedTextField(decimalFormat);
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
                    + "', " + Double.parseDouble(amount.getText()) + ", "
                    + Double.parseDouble(fxRate.getText()) + ")");

            JOptionPane.showMessageDialog(this, "Row Inserted Successfully!");
        } catch (SQLException e)
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
