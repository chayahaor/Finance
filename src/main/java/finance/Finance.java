package finance;

import api.API;
import com.toedter.calendar.JDateChooser;
import org.jfree.chart.ChartPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.sql.Connection;
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

    public Finance(Connection connection, double riskFreeRate)
    {
        api = new API();
        this.connection = connection;
        this.riskFreeRate = riskFreeRate;
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

    private JPanel doFinancePanel() throws IOException
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setSize(new Dimension(500, 300));
        panel.add(new NpvButton(connection, riskFreeRate));
        panel.add(addActionComponents());
        return panel;
    }

    private JPanel addActionComponents() throws IOException
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel top = new JPanel();
        JPanel bottom = new JPanel();

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

    public JPanel addGraph() throws SQLException, IOException
    {
        PnL profitLoss = new PnL(connection, riskFreeRate);
        JPanel graphPanel = new JPanel();
        graphPanel.setLayout(new BorderLayout());
        ChartPanel chartPanel = new ChartPanel(profitLoss.getChart());
        graphPanel.add(chartPanel, BorderLayout.CENTER);
        return graphPanel;
    }
}