package sandbox;

import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static main.Main.HOME_CURRENCY;

public class WhatIfPanel extends JPanel
{
    private final JComboBox<String> buyOrSell;
    private final JFormattedTextField quantity;
    private final JComboBox<String> currencies;
    private final JDateChooser maturityDate;
    private final JFormattedTextField forwardPrice;

    public WhatIfPanel(JComboBox<String> currencyComboBox)
    {
        setLayout(new GridLayout(1, 6));
        setMaximumSize(new Dimension(1000, 30));

        buyOrSell = new JComboBox<>(new String[]{"Buy", "Sell"});
        buyOrSell.setEditable(false);
        add(buyOrSell);

        int numColumns = 5;
        NumberFormatter twoDecimalFormatter = new NumberFormatter(new DecimalFormat("#.##"));
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

        maturityDate = new JDateChooser(new Date());
        maturityDate.setMinSelectableDate(new Date());
        add(maturityDate);

        NumberFormatter sixDecimalFormatter = new NumberFormatter(new DecimalFormat("#.######"));
        forwardPrice = generateTextField(sixDecimalFormatter, numColumns, 1.0);
        add(forwardPrice);
    }

    public Date getMaturityDate()
    {
        return this.maturityDate.getDate();
    }

    public JFormattedTextField generateTextField(
            NumberFormatter formatter,
            int numColumns,
            double value)
    {
        JFormattedTextField textField = new JFormattedTextField(formatter);
        textField.setColumns(numColumns);
        textField.setValue(value);
        return textField;
    }

    public double getQuantity(double riskFreeRate, Date actionDate, Date specifiedDate)
    {
        // if (maturity date - specified date) is negative
        // specified date is later than maturity date -- you already have full amount
        // else -- use (maturity date - specified date) for linear accretion
        long diffInDays = daysBetween(getMaturityDate(), specifiedDate) < 0
                ? daysBetween(getMaturityDate(), actionDate)
                : daysBetween(getMaturityDate(), specifiedDate); // <-- time until maturity

        // get quantity (pos if buying, neg if selling) in home currency
        double quantity = Math.abs(Double.parseDouble(this.quantity.getText()));
        quantity = (Objects.equals(buyOrSell.getSelectedItem(), "Buy") ? quantity : -quantity);
        double quantityInHomeCurrency = quantity / Double.parseDouble(forwardPrice.getText());

        // apply formula given risk-free rate in home currency and forward price
        return (quantityInHomeCurrency + 0.0) / (1 + (diffInDays / 365.0) * riskFreeRate);
    }

    public long daysBetween(Date thisDate, Date otherDate)
    {
        long diffInMs = thisDate.getTime() - otherDate.getTime();
        return TimeUnit.DAYS.convert(diffInMs, TimeUnit.MILLISECONDS);
    }
}
