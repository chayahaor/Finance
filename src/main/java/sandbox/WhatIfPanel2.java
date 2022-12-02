package sandbox;

import org.jdatepicker.JDatePicker;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class WhatIfPanel2 extends JPanel {
    private static final String HOME_CURRENCY = "USD";

    private final JFormattedTextField quantity;
    private final JComboBox<String> buyOrSell;
    private final JComboBox<String> currencies;
    private final JDatePicker maturityDate;
    private final JFormattedTextField spotPrice;

    public WhatIfPanel2(JComboBox<String> currencyComboBox) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setMaximumSize(new Dimension(1000, 30));

        NumberFormatter twoDecimalFormatter = new NumberFormatter(new DecimalFormat("#.##"));
        int numColumns = 5;

        quantity = generateTextField(twoDecimalFormatter, numColumns, 100.00);
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
        ZoneId defaultZoneId = ZoneId.systemDefault();
        Date today = Date.from(LocalDate.now().atStartOfDay(defaultZoneId).toInstant());

        maturityDate = new JDatePicker(today);

        add(maturityDate);

        NumberFormatter sixDecimalFormatter = new NumberFormatter(new DecimalFormat("#.######"));
        spotPrice = generateTextField(sixDecimalFormatter, numColumns, 1.0);
        add(spotPrice);

        buyOrSell = new JComboBox<>(new String[]{"Buy", "Sell"});
        buyOrSell.setEditable(false);
        add(buyOrSell);
    }

    public Date getMaturityDate() {
        return (Date) maturityDate.getModel().getValue();
    }

    public JFormattedTextField generateTextField(
            NumberFormatter formatter,
            int numColumns,
            double value) {
        JFormattedTextField textField = new JFormattedTextField(formatter);
        textField.setColumns(numColumns);
        textField.setValue(value);
        return textField;
    }

    public double getQuantity() {
        double val = getInHomeCurrency(Math.abs(Double.parseDouble(quantity.getText())));
        val = (Objects.equals(buyOrSell.getSelectedItem(), "Buy") ? val : -val);
        return val;
    }

    private double getInHomeCurrency(double quantity) {
        if (!Objects.equals(currencies.getSelectedItem(), HOME_CURRENCY))
        {
            // if not home currency, convert to home currency
            quantity = quantity / Double.parseDouble(spotPrice.getText());
        }
        return quantity;
    }

    public double getForwardQuantity(double riskFreeRate, Date today, Date specifiedDate) {
        // if (maturity date - specified date) is negative
        // specified date is later than maturity date -- you already have full amount
        // else -- use (specified date - today) for linear accretion
        // note that neither date can be before today (buying/selling date)
        // -- bad data is rejected before reaching this point
        long diff = daysBetween(getMaturityDate(), specifiedDate);

        long differance = diff < 0
                ? daysBetween(getMaturityDate(), today)
                : daysBetween(specifiedDate, today);
        double quantity = getQuantity();
        return quantity * (1 + (differance / 365.0) * riskFreeRate);
    }

    public long daysBetween(Date today, Date other) {
        long diffInMillies = other.getTime() - today.getTime();
        return TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS); //TODO: change order?
    }
}
