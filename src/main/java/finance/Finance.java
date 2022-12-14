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
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static main.Main.HOME_CURRENCY;

public class Finance extends JPanel
{
    private final API api;
    private final Connection connection;
    private final double riskFreeRate;
    private JComboBox<String> action;
    private JComboBox<String> currencyCombobox;
    private JFormattedTextField quantity;
    private JFormattedTextField fxRate;
    private JDateChooser maturityDate;

    public Finance(Connection connection)
    {
        api = new API();
        this.connection = connection;
        this.riskFreeRate = getRiskFreeRate(connection);
        setSize(900, 500);
        setLayout(new BorderLayout());
        try
        {
            add(doFinancePanel(), BorderLayout.NORTH);
            add(addGraph());
        } catch (Exception exception)
        {
            JOptionPane.showMessageDialog(this,
                    "Something went wrong: " + exception.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Prompt user for the risk-free rate in home currency if not already stored in database
     *
     * @param connection - the SQL Connection
     * @return - the risk-free rate
     */
    private double getRiskFreeRate(Connection connection)
    {
        double defaultRiskFreeRate = 4.0;
        double retVal;

        try
        {
            Statement stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery("Call spGetRiskFreeRate();");
            if (resultSet.next())
            {
                retVal = resultSet.getDouble("RiskFreeRate");
            } else
            {
                // prompt the user for the risk-free rate in home currency
                JFormattedTextField rfrValue
                        = new JFormattedTextField(new DecimalFormat("0.######"));
                rfrValue.setValue(defaultRiskFreeRate);
                rfrValue.setColumns(7);

                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

                JLabel promptText = new JLabel(
                        "Enter the risk-free rate as a percentage "
                        + "to be used throughout the program:");
                panel.add(promptText);
                panel.add(rfrValue);

                JOptionPane.showMessageDialog(this, panel,
                        "Risk-Free Rate in " + HOME_CURRENCY, JOptionPane.PLAIN_MESSAGE);

                double rfr = Double.parseDouble(rfrValue.getText());

                // risk-free rate is a percentage
                retVal = rfr / 100.0;

                // insert value into database
                stmt.executeQuery("Call spSetRiskFreeRate(" + retVal + ");");
            }
        } catch (SQLException exception)
        {
            retVal = defaultRiskFreeRate;
        }

        return retVal;
    }

    /**
     * Adds JPanel with NpvButton and database action components
     *
     * @return JPanel being added to the tab
     * @throws IOException - if connection to API fails
     */
    private JPanel doFinancePanel() throws IOException
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setSize(new Dimension(500, 300));
        panel.add(new NpvButton(connection, riskFreeRate));
        panel.add(addActionComponents());
        return panel;
    }

    /**
     * Adds JPanel with database action components
     *
     * @return JPanel being added to the tab
     * @throws IOException - if connection to API fails
     */
    private JPanel addActionComponents() throws IOException
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel top = new JPanel();

        top.add(new JLabel("Action:"));
        action = new JComboBox<>(new String[]{"Buy", "Sell"});
        top.add(action);

        top.add(new JLabel("Currency:"));
        currencyCombobox = api.getSymbolResults();
        top.add(currencyCombobox);

        DecimalFormat decimalFormat = new DecimalFormat("0.##");
        top.add(new JLabel("Quantity:"));
        quantity = new JFormattedTextField(decimalFormat);
        quantity.setValue(500);
        quantity.setColumns(5);
        top.add(quantity);

        decimalFormat = new DecimalFormat("0.######");
        top.add(new JLabel("Forward Price FX / " + HOME_CURRENCY + ":"));
        fxRate = new JFormattedTextField(decimalFormat);
        fxRate.setValue(3.5);
        fxRate.setColumns(5);
        top.add(fxRate);

        JPanel bottom = new JPanel();
        bottom.add(new JLabel("Maturity Date:"));
        maturityDate = new JDateChooser(new Date());
        maturityDate.setMinSelectableDate(new Date());
        maturityDate.setPreferredSize(new Dimension(200, 35));
        bottom.add(maturityDate);

        JButton doAction = new JButton();
        doAction.setText("Perform Action");
        doAction.addActionListener(this::onClick);
        bottom.add(doAction);

        panel.add(top);
        panel.add(bottom);
        return panel;
    }

    /**
     * Insert current GUI values into the database
     *
     * @param event - on click Perform Action button
     */
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
                              + "', " + Math.abs(Double.parseDouble(quantity.getText())) + ", "
                              + Double.parseDouble(fxRate.getText()) + ")");

            JOptionPane.showMessageDialog(this, "Row Inserted Successfully!");
        } catch (SQLException exception)
        {
            JOptionPane.showMessageDialog(this,
                    "Something went wrong inserting the row: " + exception);
        }
    }

    /**
     * Add NPV graph to the tab
     *
     * @return JPanel containing the graph to be added to the tab
     * @throws SQLException - in case SQL Connection fails
     * @throws IOException  - in case connection to API fails
     */
    public JPanel addGraph() throws SQLException, IOException
    {
        NpvGraph netPresentValue = new NpvGraph(connection, riskFreeRate);
        JPanel graphPanel = new JPanel();
        graphPanel.setLayout(new BorderLayout());
        ChartPanel chartPanel = new ChartPanel(netPresentValue.getChart());
        graphPanel.add(chartPanel, BorderLayout.CENTER);
        return graphPanel;
    }
}