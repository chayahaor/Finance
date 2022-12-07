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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static main.Main.HOME_CURRENCY;

public class Finance extends JPanel
{
    private JFormattedTextField riskFreeRate;
    private Connection connection;
    private JComboBox<String> action;
    private final String[] actions = new String[]{"Buy", "Sell"};
    private JFormattedTextField amount;
    private JFormattedTextField fxRate;
    private JDateChooser maturityDate;
    private JButton doAction;
    private API api;

    private JComboBox<String> currencyCombobox;


    public Finance(Connection connection) throws IOException
    {
        api = new API();
        this.connection = connection;
        setSize(900, 500);
        setLayout(new BorderLayout());
        add(doFinancePanel(), BorderLayout.NORTH);
        add(addGraph());
    }

    private JPanel doFinancePanel() throws IOException
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
        panel.add(new JLabel("Risk Free Rate of " + HOME_CURRENCY + " as a percentage:"));
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

    private void calculateRowCurrentValue(ResultSet resultSet,
                                          HashMap<String, Double> quantitiesPerCurrency)
            throws SQLException, IOException
    {
        String currentCurrency = resultSet.getString("Currency");
        double sum = quantitiesPerCurrency.get(currentCurrency) == null
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

        //convert to currency and apply maturity date formula
        double rate = api.convert(currentCurrency, HOME_CURRENCY);
        double value = resultSet.getString("Action").equals("Sell")
                ? -(quantity / rate)
                : (quantity / rate);

        // risk-free rate is a percentage
        double rfr = Double.parseDouble(riskFreeRate.getText()) / 100;
        sum += value * (1 + (diffInDays / 365.0) * rfr);

        quantitiesPerCurrency.put(currentCurrency, sum);
    }


    private JPanel addActionComponents() throws IOException
    {
        currencyCombobox = api.getSymbolResults();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel top = new JPanel();
        JPanel bottom = new JPanel();

        top.add(new JLabel("Action:"));
        action = new JComboBox<>(actions);
        top.add(action);

        top.add(new JLabel("Currency:"));
        top.add(currencyCombobox);

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
        maturityDate = new JDateChooser(new Date());
        maturityDate.setMinSelectableDate(new Date());
        maturityDate.setPreferredSize(new Dimension(200, 35));
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
        int actionId = (Objects.equals(action.getSelectedItem(), "Buy") ? 1 : 2);

        try
        {
            LocalDate today = LocalDate.now();
            String formatted = DateTimeFormatter
                    .ofPattern("yyyy-MM-dd", Locale.ENGLISH)
                    .format(today);

            ZoneId defaultZoneId = ZoneId.systemDefault();
            String maturityFormatted = DateTimeFormatter
                    .ofPattern("yyyy-MM-dd", Locale.ENGLISH)
                    .format(maturityDate.getDate().toInstant().atZone(defaultZoneId).toLocalDate());

            Statement stmt = connection.createStatement();
            stmt.executeQuery("Call spInsertMainData ("
                    + "'" + formatted + "', " + actionId + ", '"
                    + currencyCombobox.getSelectedItem() + "', '"
                    + maturityFormatted
                    + "', " + Double.parseDouble(amount.getText()) + ", "
                    + Double.parseDouble(fxRate.getText()) + ")");

            JOptionPane.showMessageDialog(this, "Row Inserted Successfully!");
        } catch (SQLException exception)
        {
            JOptionPane.showMessageDialog(this, "Something went wrong inserting the row: " + exception);
        }

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