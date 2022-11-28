package sandbox;

import helpers.DatePanel;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Objects;

import static main.Main.HOME_CURRENCY;

public class WhatIfPanel extends JPanel
{
    private final JFormattedTextField quantity;
    private final JComboBox<String> buyOrSell;
    private final JComboBox<String> currencies;
    private final DatePanel maturityDate;
    private final JFormattedTextField fxRate;

    public WhatIfPanel(JComboBox<String> currencyComboBox)
    {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setMaximumSize(new Dimension(1000, 30));
        NumberFormatter twoDecimalFormatter = new NumberFormatter(new DecimalFormat("#.##"));
        NumberFormatter sixDecimalFormatter = new NumberFormatter(new DecimalFormat("#.######"));
        int numColumns = 5;

        quantity = generateTextField(twoDecimalFormatter, numColumns, 100.00, "amount");
        add(quantity);

        currencies = new JComboBox<>();
        for (int i = 0; i < currencyComboBox.getItemCount(); i++)
        {
            currencies.addItem(currencyComboBox.getItemAt(i));
        }
        currencies.setSelectedItem(HOME_CURRENCY);
        currencies.setEditable(false);
        currencies.setSize(35, 15);
        add(currencies);

        maturityDate = new DatePanel();

        add(maturityDate);

        fxRate = generateTextField(sixDecimalFormatter, numColumns, 1.0, "fxRate");
        add(fxRate);

        String[] options = {"Buy", "Sell"};
        buyOrSell = new JComboBox<>(options);
        buyOrSell.setEditable(false);
        add(buyOrSell);
    }

    public DatePanel getMaturityDate()
    {
        return this.maturityDate;
    }

    public JFormattedTextField generateTextField(NumberFormatter formatter, int numColumns, double value, String name)
    {
        JFormattedTextField textField = new JFormattedTextField(formatter);
        textField.setColumns(numColumns);
        textField.setValue(value);
        textField.setName(name);
        return textField;
    }

    public double getQuantity()
    {
        double val = getInHomeCurrency(Math.abs(Double.parseDouble(quantity.getText())));
        val = (Objects.equals(buyOrSell.getSelectedItem(), "Buy") ? val : -val);
        return val;
    }

    private double getInHomeCurrency(double quantity)
    {
        if (!Objects.equals(currencies.getSelectedItem(), HOME_CURRENCY))
        {
            // if not home currency, convert to home currency
            quantity = quantity / Double.parseDouble(fxRate.getText());
        }
        return quantity;
    }

    public double getForwardAmount(double rfr, Date specifiedDate)
    {
        // maturity date - specified date
        long diff = maturityDate.dateDiffFromSpecifiedDate(specifiedDate);

        double val = getQuantity();

        // if diff is negative, specified date is later than maturity date -- you already have full amount
        // if diff is zero, specified date equals maturity date -- you have the full amount at specified date
        // if diff is positive, specified date is earlier than maturity date -- linear accretion
        // note that neither date can be before buying/selling date -- bad data is rejected before reaching this point
        double amount = (diff < 0)
                ? val * (1 + rfr)
                : val * (1 + ((((double) diff) / 365) * rfr));

        System.out.println(amount);
        return amount;
    }
}
