package main;


import dagger.DaggerCurrencyExchangeComponent;
import finance.Finance;
import json.CurrencyExchangeServiceFactory;
import json.Symbol;
import sandbox.Sandbox;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import java.awt.*;
import java.util.Map;

@Singleton
public class Main extends JFrame
{
    public static final String HOME_CURRENCY = "USD";
    private Sandbox sandbox;
    private Finance finance;
    private final JComboBox<String> currencyComboBox;
    public static final JComboBox<String> fromCurrency = new JComboBox<>();
    public static final JComboBox<String> toCurrency = new JComboBox<>();


    private Map<String, Symbol> symbolsMap;

    @Inject
    public Main(MainPresenter presenter)
    {
        CurrencyExchangeServiceFactory factory = new CurrencyExchangeServiceFactory();

        setTitle("Finance Project");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());
        setResizable(true);

        currencyComboBox = new JComboBox<>();
        presenter.loadSymbolsChoices();

        setUpJTabbedPane();
    }

    public void setSymbolsChoices(Map<String, Symbol> symbols)
    {
        symbolsMap = symbols;
        String[] symbolsArray = symbols.keySet().toArray(new String[0]);

        currencyComboBox.removeAllItems();
        fromCurrency.removeAllItems();
        toCurrency.removeAllItems();


        for (int i = 0; i < symbolsArray.length; i++)
        {
            //currencyComboBox.addItem(descriptionsArray[i]);
            currencyComboBox.addItem(String.valueOf(symbolsMap.get(symbolsArray[i]).getCode()));
            fromCurrency.addItem(String.valueOf(symbolsMap.get(symbolsArray[i]).getCode()));
            toCurrency.addItem(String.valueOf(symbolsMap.get(symbolsArray[i]).getCode()));
        }

        fromCurrency.setSelectedItem(HOME_CURRENCY);
        fromCurrency.setEditable(false);
        toCurrency.setSelectedItem(HOME_CURRENCY);
        toCurrency.setEditable(false);
    }

    public void setUpJTabbedPane()
    {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setForeground(Color.BLACK);

        // add all the tabs to the Main frame's JTabbedPane
        sandbox = new Sandbox();
        sandbox.setCurrencyComboBox(currencyComboBox);

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
        Main frame =
                DaggerCurrencyExchangeComponent
                        .create()
                        .getCurrencyExchangeFrame();
        frame.setVisible(true);
    }
}
