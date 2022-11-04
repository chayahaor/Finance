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
        amount.setColumns(numColumns);
        add(amount);

        JComboBox<String> currencies = new JComboBox<>();
        currencies.setEditable(false);
        currencies.setSize(35, 15);
        add(currencies);

        JFormattedTextField fxRate = new JFormattedTextField(defaultFormatter);
        fxRate.setColumns(numColumns);
        add(fxRate);

        JFormattedTextField forwardRate = new JFormattedTextField(defaultFormatter);
        forwardRate.setColumns(numColumns);
        add(forwardRate);

        add(new DatePanel());
    }
}
