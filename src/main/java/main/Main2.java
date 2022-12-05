package main;

import finance.Finance2;
import sandbox.Sandbox2;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Main2 extends JFrame {
    public static final String HOME_CURRENCY = "USD";
    public double initialAmount = 10000;
    private Sandbox2 sandbox2;
    private Finance2 finance;

    public Main2() throws IOException {
        setTitle("Finance Project");
        setSize(1000, 600);
        setMinimumSize(new Dimension(1000, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());
        setResizable(true);
        setUpJTabbedPane();
    }

    public void setUpJTabbedPane() throws IOException {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setForeground(Color.BLACK);
        sandbox2 = new Sandbox2();
        tabbedPane.add("Play in the Sandbox", sandbox2);

        try
        {
            // create database connection
            Connection connection = createConnection();

            // add finance tab to the Main frame's JTabbedPane if Connection is successful
            finance = new Finance2(connection);
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

    private Connection createConnection() throws SQLException {
        String dbName = "finance";
        int portNumber = 3306;
        Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:" + portNumber + "/" + dbName, "root", "");

        Statement stmt = connection.createStatement();
        ResultSet resultSet = stmt.executeQuery("Select * from maindata");
        if (!resultSet.next()) // if there is no result set
        {
            //TODO: possibly delete and replace with hardcoded in 10,000
            JFormattedTextField defaultAmount
                    = new JFormattedTextField(new DecimalFormat("###0.00"));
            defaultAmount.setValue(initialAmount);
            defaultAmount.setColumns(7);
            JOptionPane.showMessageDialog(this, defaultAmount,
                    "Enter Initial Amount", JOptionPane.PLAIN_MESSAGE);
            initialAmount = Double.parseDouble(defaultAmount.getText());
            LocalDate today = LocalDate.now();
            String formatted = DateTimeFormatter
                    .ofPattern("yyyy-MM-dd", Locale.ENGLISH)
                    .format(today);
            stmt.executeQuery("Call spInitial (" + initialAmount + ", '" + HOME_CURRENCY + "', '" + formatted + "');");
        }
        return connection;
    }

    public static void main(String[] args) throws IOException {
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
        Main2 frame = new Main2();
        frame.setVisible(true);
    }
}
