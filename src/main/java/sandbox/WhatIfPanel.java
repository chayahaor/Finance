package sandbox;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.DecimalFormat;

public class WhatIfPanel extends JPanel
{
    private static int numColumns = 5;

    public WhatIfPanel()
    {
        setMaximumSize(new Dimension(850, 300));

        NumberFormatter defaultFormatter = new NumberFormatter(new DecimalFormat("#.##"));

        JFormattedTextField amount = new JFormattedTextField(defaultFormatter);
        amount.setValue(100.00);
        amount.setColumns(numColumns);
        add(amount);

        JComboBox<String> currencies = new JComboBox<>();
        currencies.setEditable(false);
        currencies.setSize(35, 15);
        add(currencies);

        JFormattedTextField fxRate = new JFormattedTextField(defaultFormatter);
        fxRate.setValue(3.5);
        fxRate.setColumns(numColumns);
        add(fxRate);

        JFormattedTextField forwardRate = new JFormattedTextField(defaultFormatter);
        forwardRate.setColumns(numColumns);
        forwardRate.setValue(4.0);
        add(forwardRate);

        add(new DatePanel());
    }
}
