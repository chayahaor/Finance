package sandbox;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.DecimalFormat;

public class WhatIfPanel extends JPanel
{
    public WhatIfPanel()
    {
        setMaximumSize(new Dimension(850, 300));
        //setLayout(new GridLayout(1, 5));
        JFormattedTextField amount = new JFormattedTextField();
        NumberFormatter defaultFormatter = new NumberFormatter(new DecimalFormat("#.##"));
        DefaultFormatterFactory valueFactory = new DefaultFormatterFactory(defaultFormatter);

        amount.setFormatterFactory(valueFactory);
        amount.setSize(new Dimension(5, 15));
        add(amount);

        JComboBox<String> currencies = new JComboBox<>();
        currencies.setEditable(false);
        currencies.setSize(35, 15);
        add(currencies);

        JFormattedTextField fxRate = new JFormattedTextField();
        fxRate.setFormatterFactory(valueFactory);
        fxRate.setSize(new Dimension(5, 15));
        add(fxRate);

        JFormattedTextField forwardRate = new JFormattedTextField();
        forwardRate.setFormatterFactory(valueFactory);
        forwardRate.setSize(new Dimension(5, 15));
        add(forwardRate);

        add(new DatePanel());
    }
}
