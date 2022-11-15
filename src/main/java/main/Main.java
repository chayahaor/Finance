package main;

import dagger.DaggerCurrencyExchangeComponent;
import finance.Finance;
import helpers.CurrencyComboBox;
import sandbox.Sandbox;

import javax.swing.*;
import java.awt.*;

public class Main extends JFrame
{
    public static final String HOME_CURRENCY = "USD";
    private Sandbox sandbox;
    private Finance finance;
    public CurrencyComboBox fromCurrency;
    public CurrencyComboBox toCurrency;

    public Main()
    {
        setTitle("Finance Project");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());
        setResizable(true);

        setUpCurrencyComboBox();

        setUpJTabbedPane();
    }

    private void setUpCurrencyComboBox()
    {
        toCurrency = DaggerCurrencyExchangeComponent
                .create()
                .getCurrencyExchange();
        fromCurrency = DaggerCurrencyExchangeComponent
                .create()
                .getCurrencyExchange();
    }

    public void setUpJTabbedPane()
    {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setForeground(Color.BLACK);

        // add all the tabs to the Main frame's JTabbedPane
        sandbox = new Sandbox();

        finance = new Finance();

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
