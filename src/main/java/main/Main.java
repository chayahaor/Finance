package main;


import finance.Finance;
import sandbox.Sandbox;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Main extends JFrame
{
    public static final String HOME_CURRENCY = "USD";
    public static final double DEFAULT_VALUE = 10000;

    public Main()
    {
        setTitle("Finance Project");
        setSize(1000, 600);
        setMinimumSize(new Dimension(1000, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());
        setResizable(true);
        try
        {
            setUpJTabbedPane();
        } catch (Exception exception)
        {
            JOptionPane.showMessageDialog(this,
                    "Something went wrong: " + exception.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void setUpJTabbedPane() throws IOException
    {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setForeground(Color.BLACK);

        // add sandbox tab to the Main frame's JTabbedPane always
        Sandbox sandbox = new Sandbox();
        tabbedPane.add("Play in the Sandbox", sandbox);

        try
        {
            // create database connection
            Connection connection = createConnection();

            double riskFreeRate = getRiskFreeRate();

            // add finance tab to the Main frame's JTabbedPane if Connection is successful
            Finance finance = new Finance(connection, riskFreeRate);
            tabbedPane.add("Finance Stuff", finance);
        } catch (SQLException exception)
        {
            // otherwise, notify user that something went wrong and only have a Sandbox tab
            JOptionPane.showMessageDialog(this,
                    "Something went wrong with the SQL connection: " + exception.getMessage());
        }
        // add the JTabbedPane to Main frame
        tabbedPane.setPreferredSize(new Dimension(950, 550));
        add(tabbedPane);
    }

    private Connection createConnection() throws SQLException
    {
        String dbName = "finance";
        int portNumber = 3306;
        Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:" + portNumber + "/" + dbName, "root", "");

        Statement stmt = connection.createStatement();
        ResultSet resultSet = stmt.executeQuery("Select * from maindata");
        if (!resultSet.next()) // if there is no result set
        {
            JFormattedTextField defaultAmount
                    = new JFormattedTextField(new DecimalFormat("###0.00"));
            defaultAmount.setValue(DEFAULT_VALUE);
            defaultAmount.setColumns(7);
            JOptionPane.showMessageDialog(this, defaultAmount,
                    "Enter Initial Amount", JOptionPane.PLAIN_MESSAGE);
            double initialAmount = Double.parseDouble(defaultAmount.getText());
            LocalDate today = LocalDate.now();
            String formatted = DateTimeFormatter
                    .ofPattern("yyyy-MM-dd", Locale.ENGLISH)
                    .format(today);
            stmt.executeQuery("Call spInitial ("
                              + initialAmount + ", '"
                              + HOME_CURRENCY + "', '"
                              + formatted + "');");
        }
        return connection;
    }

    private double getRiskFreeRate()
    {
        JFormattedTextField rfrValue
                = new JFormattedTextField(new DecimalFormat("0.######"));
        rfrValue.setValue(4.0);
        rfrValue.setColumns(7);
        JOptionPane.showMessageDialog(this, rfrValue,
                "Enter the risk free rate today as a percentage in " + HOME_CURRENCY
                , JOptionPane.PLAIN_MESSAGE);

        double rfr = Double.parseDouble(rfrValue.getText());

        // risk-free rate is a percentage
        return rfr / 100.0;
    }

    public static void main(String[] args)
    {
        // update the UIManager to use the Nimbus Look and Feel
        try
        {
            UIManager.LookAndFeelInfo[] lookAndFeels = UIManager.getInstalledLookAndFeels();
            for (UIManager.LookAndFeelInfo info : lookAndFeels)
            {
                if ("Nimbus".equals(info.getName()))
                {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored)
        {
        }

        // change the font of the program
        Font font = new Font("Lucida Sans Unicode", Font.PLAIN, 12);
        UIManager.put("Button.font", font);
        UIManager.put("Label.font", font);
        UIManager.put("TabbedPane.font", font);
        UIManager.put("TextField.font", font);
        UIManager.put("OptionPane.messageFont", font);
        UIManager.put("TextArea.font", font);
        UIManager.put("ComboBox.font", font);

        // instantiate the Main frame
        try
        {
            Main frame = new Main();
            frame.setVisible(true);
        } catch (Exception exception)
        {
            // Something went wrong, show error message and exit
            JOptionPane.showMessageDialog(new JPanel(),
                    "Unfortunately something went wrong instantiating the program.",
                    "Error!", JOptionPane.ERROR_MESSAGE);
        }
    }
}
