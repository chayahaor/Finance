package sandbox;

import helpers.DatePanel;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static main.Main.HOME_CURRENCY;

public class WhatIfPanel extends JPanel
{
    private final JFormattedTextField amount;
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

        amount = generateTextField(twoDecimalFormatter, numColumns, 100.00, "amount");
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

        maturityDate = new DatePanel();
        add(maturityDate);

        fxRate = generateTextField(sixDecimalFormatter, numColumns, 1.0, "fxRate");
        add(fxRate);

        add(generateTextField(twoDecimalFormatter, numColumns, 4.0, "forwardRate"));

        String[] options = {"Buy", "Sell"};
        buyOrSell = new JComboBox<>(options);
        buyOrSell.setEditable(false);
        add(buyOrSell);

    }

    public JFormattedTextField generateTextField(NumberFormatter formatter, int numColumns, double value, String name)
    {
        JFormattedTextField textField = new JFormattedTextField(formatter);
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
            // if not home currency, convert to home currency
            amount = Double.parseDouble(fxRate.getText()) * amount;
        }
        return amount;
    }

    public double getForwardAmount(Date specifiedDate)
    {
        int selectedYear = maturityDate.getYear();
        int selectedMonth = maturityDate.getMonthNumber();
        int selectedDay = maturityDate.getDay();

        Date maturity = new GregorianCalendar(selectedYear, selectedMonth, selectedDay).getTime();
        long diffInMs = maturity.getTime() - specifiedDate.getTime();
        if (diffInMs > 0)
        {
            long diff = TimeUnit.DAYS.convert(diffInMs, TimeUnit.MILLISECONDS);
            System.out.println(diff);
        }
        return 0.0;
    }
}
