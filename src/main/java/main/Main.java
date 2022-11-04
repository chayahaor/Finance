package main;

import dagger.DaggerCurrencyExchangeComponent;
import json.CurrencyExchangeServiceFactory;
import json.Symbol;
import sandbox.Sandbox;
import finance.Finance;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class Main extends JFrame
{
    private final MainPresenter presenter;
    private Map<String, Symbol> symbolsMap;
    private String[] symbolsArray;
    private String[] descriptionsArray;

    private JComboBox<String> fromComboBox;
    private JComboBox<String> toComboBox;

    @Inject
    public Main(MainPresenter presenter) {
        this.presenter = presenter;

        setTitle("Finance Project");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());
        setResizable(true);

        CurrencyExchangeServiceFactory factory = new CurrencyExchangeServiceFactory();
        fromComboBox = new JComboBox<>();
        toComboBox = new JComboBox<>();
        presenter.loadSymbolsChoices();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setForeground(Color.BLACK);

        // add all the tabs to the Main frame's JTabbedPane
        tabbedPane.add("Play in the Sandbox", new Sandbox(fromComboBox));
        tabbedPane.add("Do Actual Finance Stuff", new Finance());
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
        } catch (Exception ignored) {}

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
        frame.setVisible(true);
    }

    public void setSymbolsChoices(Map<String, Symbol> symbols)
    {
        symbolsMap = symbols;
        symbolsArray = symbolsMap.keySet().toArray(new String[0]);
        descriptionsArray = new String[symbolsArray.length];
        for (int i = 0; i < symbolsArray.length; i++) {
            descriptionsArray[i] = symbolsMap.get(symbolsArray[i]).getDescription();
        }
        fromComboBox.removeAllItems();
        toComboBox.removeAllItems();

        for (int i = 0; i < descriptionsArray.length; i++) {
            fromComboBox.addItem(descriptionsArray[i]);
            toComboBox.addItem(descriptionsArray[i]);
        }
    }
}
