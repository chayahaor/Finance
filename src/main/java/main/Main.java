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
    public double initialAmount = 10000;
    private Sandbox sandbox;
    private Finance finance;

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
        } catch (SQLException exception)
        {
            JOptionPane.showMessageDialog(this,
                    "Something went wrong with the SQL connection: " + exception.getMessage());
            System.exit(0);
        }
    }

    public void setUpJTabbedPane() throws SQLException
    {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setForeground(Color.BLACK);

        String dbName = "finance";
        int portNumber = 3306;
        Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:" + portNumber + "/" + dbName, "root", "");

        Statement stmt = connection.createStatement();
        ResultSet resultSet = stmt.executeQuery("Select * from Snapshot");
        if (!resultSet.next()) // if there is no result set
        {
            JFormattedTextField defaultAmount
                    = new JFormattedTextField(new DecimalFormat("####.00"));
            defaultAmount.setValue(initialAmount);
            defaultAmount.setColumns(7);
            JOptionPane.showMessageDialog(this, defaultAmount,
                    "Enter Initial Amount", JOptionPane.PLAIN_MESSAGE);
            initialAmount = Double.parseDouble(defaultAmount.getText());
            LocalDate today = LocalDate.now();
            String formatted = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH).format(today);
            stmt.executeQuery("Call spInitial (" + initialAmount + ", '" + HOME_CURRENCY + "', '" + formatted + "');");
        }

        // add all the tabs to the Main frame's JTabbedPane
        sandbox = new Sandbox();

        finance = new Finance(connection);

        tabbedPane.add("Play in the Sandbox", sandbox);
        tabbedPane.add("Finance Stuff", finance);
        tabbedPane.setPreferredSize(new Dimension(950, 550));

        add(tabbedPane);
    }

    public static void main(String[] args)
    {
        Font font = new Font("Lucida Sans Unicode", Font.PLAIN, 12);

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
        UIManager.put("Button.font", font);
        UIManager.put("Label.font", font);
        UIManager.put("TabbedPane.font", font);
        UIManager.put("TextField.font", font);
        UIManager.put("OptionPane.messageFont", font);
        UIManager.put("TextArea.font", font);
        UIManager.put("ComboBox.font", font);

        // instantiate the Main frame
        Main frame = new Main();
        frame.setVisible(true);


    }
}
