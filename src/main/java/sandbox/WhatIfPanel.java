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
    private final JFormattedTextField spotPrice;

    public WhatIfPanel(JComboBox<String> currencyComboBox)
    {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setMaximumSize(new Dimension(1000, 30));

        NumberFormatter twoDecimalFormatter = new NumberFormatter(new DecimalFormat("#.##"));
        NumberFormatter sixDecimalFormatter = new NumberFormatter(new DecimalFormat("#.######"));
        int numColumns = 5;

        quantity = generateTextField(twoDecimalFormatter, numColumns, 100.00);
        add(quantity);

        currencies = new JComboBox<>();
        for (int i = 0; i < currencyComboBox.getItemCount(); i++)
        {
            currencies.addItem(currencyComboBox.getItemAt(i));
        }
        currencies.setEditable(false);
        currencies.setSize(35, 15);
        add(currencies);

        maturityDate = new DatePanel();
        add(maturityDate);

        spotPrice = generateTextField(sixDecimalFormatter, numColumns, 1.0);
        add(spotPrice);

        buyOrSell = new JComboBox<>(new String[]{"Buy", "Sell"});
        buyOrSell.setEditable(false);
        add(buyOrSell);
    }

    public DatePanel getMaturityDate()
    {
        return this.maturityDate;
    }

    public JFormattedTextField generateTextField(NumberFormatter formatter, int numColumns, double value)
    {
        JFormattedTextField textField = new JFormattedTextField(formatter);
        textField.setColumns(numColumns);
        textField.setValue(value);
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
            quantity = quantity / Double.parseDouble(spotPrice.getText());
        }
        return quantity;
    }

    public double getForwardQuantity(double riskFreeRate, DatePanel specifiedDate)
    {
        Date specified = specifiedDate.getDate();

        // if (maturity date - specified date) is negative
        // specified date is later than maturity date -- you already have full amount
        // else -- use (specified date - today) for linear accretion
        // note that neither date can be before today (buying/selling date)
        // -- bad data is rejected before reaching this point
        long diff = maturityDate.dateDiffFromSpecifiedDate(specified) < 0
                ? maturityDate.dateDiffFromToday()
                : specifiedDate.dateDiffFromToday();

        double quantity = getQuantity();

        return quantity * (1 + (diff / 365.0) * riskFreeRate);
    }
}
