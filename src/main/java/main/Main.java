package main;


import finance.Finance;
import sandbox.Sandbox;

import javax.swing.*;
import java.awt.*;
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
        // set up frame
        setTitle("Finance Project");
        setSize(1000, 600);
        setMinimumSize(new Dimension(1000, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());
        setResizable(true);

        // set up both tabs
        setUpJTabbedPane();
    }

    /**
     * Add tabs to the Main Frame
     */
    public void setUpJTabbedPane()
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

            // add finance tab to the Main frame's JTabbedPane if connection is successful
            Finance finance = new Finance(connection);
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

    /**
     * Create connection to finance database, insert initial row into database
     * @return SQL Connection
     * @throws SQLException - if SQL connection fails
     */
    private Connection createConnection() throws SQLException
    {
        // generate the SQL connection through localhost
        String dbName = "finance";
        int portNumber = 3306;
        Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:" + portNumber + "/" + dbName, "root", "");

        // get the initial quantity in the database
        Statement stmt = connection.createStatement();
        ResultSet resultSet = stmt.executeQuery("Call spGetInitialDate();");

        if (!resultSet.next()) // if there is no result set (i.e. no initial value in database)
        {
            // prompt the user for initial quantity
            JFormattedTextField defaultAmount
                    = new JFormattedTextField(new DecimalFormat("###0.00"));
            defaultAmount.setValue(DEFAULT_VALUE);
            defaultAmount.setColumns(7);
            JOptionPane.showMessageDialog(this, defaultAmount,
                    "Enter Initial Amount", JOptionPane.PLAIN_MESSAGE);
            double initialAmount = Double.parseDouble(defaultAmount.getText());

            // insert initial quantity into database
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

    /**
     * Main method, instantiate the frame and set up Nimbus LAF
     * @param args - the program arguments
     */
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
