package sandbox;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.DecimalFormat;

import static main.Main.HOME_CURRENCY;

public class WhatIfPanel extends JPanel
{
    public WhatIfPanel(JComboBox<String> currencyComboBox)
    {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setMaximumSize(new Dimension(1000, 30));
        NumberFormatter defaultFormatter = new NumberFormatter(new DecimalFormat("#.##"));
        int numColumns = 5;

        JFormattedTextField amount = generateTextField(defaultFormatter, numColumns, 100.00);
        add(amount);

        JComboBox<String> currencies = new JComboBox<>();
        for (int i = 0; i < currencyComboBox.getItemCount(); i++)
        {
            currencies.addItem(currencyComboBox.getItemAt(i));
        }
        currencies.setSelectedItem(HOME_CURRENCY);
        currencies.setEditable(false);
        currencies.setSize(35, 15);
        add(currencies);

        add(new DatePanel());

        JFormattedTextField fxRate = generateTextField(defaultFormatter, numColumns, 3.5);
        add(fxRate);

        JFormattedTextField forwardRate = generateTextField(defaultFormatter, numColumns,4.0);
        add(forwardRate);

        JComboBox<String> buyOrSell = new JComboBox<>();
        buyOrSell.addItem("Buy");
        buyOrSell.addItem("Sell");
        add(buyOrSell);

    }

    public JFormattedTextField generateTextField(NumberFormatter defaultFormatter, int numColumns, double value)
    {
        JFormattedTextField textField = new JFormattedTextField(defaultFormatter);
        textField.setColumns(numColumns);
        textField.setValue(value);
        return textField;
    }
}
