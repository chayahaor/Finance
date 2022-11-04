package main;

import dagger.DaggerCurrencyExchangeComponent;
import finance.Finance;
import json.CurrencyExchangeServiceFactory;
import json.Symbol;
import sandbox.Sandbox;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class Main extends JFrame
{
    private Sandbox sandbox;

    private Finance finance;
    private JComboBox<String> currencyComboBox;

    @Inject
    public Main(MainPresenter presenter)
    {
        setTitle("Finance Project");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());
        setResizable(true);

        CurrencyExchangeServiceFactory factory = new CurrencyExchangeServiceFactory();
        currencyComboBox = new JComboBox<>();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setForeground(Color.BLACK);

        // add all the tabs to the Main frame's JTabbedPane
        sandbox = new Sandbox();
        sandbox.setCurrencyComboBox(currencyComboBox);

        System.out.println(currencyComboBox.getItemCount());
        finance = new Finance();
        tabbedPane.add("Play in the Sandbox", sandbox);
        tabbedPane.add("Do Actual Finance Stuff", finance);
        tabbedPane.setPreferredSize(new Dimension(950, 550));

        add(tabbedPane);

        presenter.loadSymbolsChoices();

    }

    public void setSymbolsChoices(Map<String, Symbol> symbols)
    {
        String[] symbolsArray = symbols.keySet().toArray(new String[0]);
        String[] descriptionsArray = new String[symbolsArray.length];
        for (int i = 0; i < symbolsArray.length; i++)
        {
            descriptionsArray[i] = symbols.get(symbolsArray[i]).getDescription();
        }
        currencyComboBox.removeAllItems();

        for (int i = 0; i < descriptionsArray.length; i++)
        {
            currencyComboBox.addItem(descriptionsArray[i]);
        }
    }

    public static void main(String[] args)
    {
        // instantiate the Main frame
        Main frame =
                DaggerCurrencyExchangeComponent
                        .create()
                        .getCurrencyExchangeFrame();
        frame.setVisible(true);
    }
}
