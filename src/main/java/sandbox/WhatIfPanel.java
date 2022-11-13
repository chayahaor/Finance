package sandbox;

import helpers.DatePanel;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.Objects;

import static main.Main.HOME_CURRENCY;

public class WhatIfPanel extends JPanel
{
    private JFormattedTextField amount;
    private JComboBox<String> buyOrSell;

    private JComboBox<String> currencies;

    public WhatIfPanel(JComboBox<String> currencyComboBox)
    {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setMaximumSize(new Dimension(1000, 30));
        NumberFormatter defaultFormatter = new NumberFormatter(new DecimalFormat("#.##"));
        int numColumns = 5;

        amount = generateTextField(defaultFormatter, numColumns, 100.00, "amount");
        add(amount);

        currencies = new JComboBox<>();
        for (int i = 0; i < currencyComboBox.getItemCount(); i++)
        {
            currencies.addItem(currencyComboBox.getItemAt(i));
        }
        currencies.setSelectedItem(HOME_CURRENCY);
        currencies.setEditable(false);
        currencies.setSize(35, 15);
        add(currencies);

        add(new DatePanel());

        add(generateTextField(defaultFormatter, numColumns, 3.5, "fxRate"));

        add(generateTextField(defaultFormatter, numColumns,4.0, "forwardRate"));

        String[] options = {"Buy", "Sell"};
        buyOrSell = new JComboBox<>(options);
        buyOrSell.setEditable(false);
        add(buyOrSell);

    }

    public JFormattedTextField generateTextField(NumberFormatter defaultFormatter,
                                                 int numColumns,
                                                 double value,
                                                 String name)
    {
        JFormattedTextField textField = new JFormattedTextField(defaultFormatter);
        textField.setColumns(numColumns);
        textField.setValue(value);
        textField.setName(name);
        return textField;
    }

    public double getAmount()
    {
        double val = getInHomeCurrency(Math.abs(Double.parseDouble(amount.getText())));
        val = (Objects.equals(buyOrSell.getSelectedItem(), "Buy") ? val : -val);
        return val;
    }

    private double getInHomeCurrency(double amount)
    {
        if (!Objects.equals(currencies.getSelectedItem(), HOME_CURRENCY))
        {
            // TODO: if not home currency, convert to home currency
            amount = 0;
        }
        return amount;
    }
}
