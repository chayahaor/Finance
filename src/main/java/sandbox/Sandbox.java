package sandbox;

import dagger.DaggerCurrencyExchangeComponent;
import helpers.CurrencyComboBox;
import helpers.DatePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import static main.Main.HOME_CURRENCY;

public class Sandbox extends JPanel
{
    private final ArrayList<WhatIfPanel> whatIfs = new ArrayList<>();
    private final JScrollPane scrollPane;
    private final JPanel whatIf;
    private DatePanel specifiedDate;
    private NumberFormat moneyFormat;
    private final JFormattedTextField defaultAmount;
    private final CurrencyComboBox currencyComboBox;

    public Sandbox()
    {
        setSize(900, 500);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        currencyComboBox = DaggerCurrencyExchangeComponent
                .create()
                .getCurrencyExchange();
        currencyComboBox.addSymbols();

        JPanel startingRow = new JPanel();
        startingRow.setMaximumSize(new Dimension(850, 50));

        startingRow.add(new JLabel("Enter the starting value (in " + HOME_CURRENCY + ")"));

        moneyFormat = NumberFormat.getCurrencyInstance();
        int numColumns = 7;
        defaultAmount = new JFormattedTextField(moneyFormat);
        defaultAmount.setValue(10000.00);
        defaultAmount.setColumns(numColumns);
        startingRow.add(defaultAmount);

        add(startingRow);

        add(new InstructionsPanel());

        whatIf = new JPanel();
        whatIf.setLayout(new BoxLayout(whatIf, BoxLayout.Y_AXIS));
        whatIf.setMaximumSize(new Dimension(850, 450));

        scrollPane = new JScrollPane(whatIf);
        scrollPane.setPreferredSize(new Dimension(850, 0));
        scrollPane.setMaximumSize(new Dimension(850, 450));
        add(scrollPane);

        setUpButtonPanel();

    }

    private void setUpButtonPanel()
    {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setMaximumSize(new Dimension(850, 100));
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        JPanel actionRow = new JPanel();

        generateButton("Add another what if row", this::onClickMore, actionRow);

        generateButton("Reset the Sandbox", this::onClickReset, actionRow);

        JPanel calcRow = new JPanel();

        generateButton("Show amount in " + HOME_CURRENCY + " today", this::onClickCurrent, calcRow);

        generateButton("Show amount in " + HOME_CURRENCY + " at specified maturity date", this::onClickFuture, calcRow);

        specifiedDate = new DatePanel();

        buttonPanel.add(actionRow);
        buttonPanel.add(calcRow);

        add(buttonPanel);
    }

    private void generateButton(String text, ActionListener listener, JPanel panel)
    {
        JButton button = new JButton();
        button.setText(text);
        button.addActionListener(listener);
        panel.add(button);
    }

    private void onClickMore(ActionEvent actionEvent)
    {
        JPanel row = new JPanel(new BorderLayout());
        row.setMaximumSize(new Dimension(1000, 30));

        WhatIfPanel whatIfPanel = new WhatIfPanel(currencyComboBox);
        DeleteButton deleteButton = new DeleteButton(whatIfPanel);
        deleteButton.addActionListener(this::onClickDelete);
        row.add(deleteButton, BorderLayout.EAST);
        row.add(whatIfPanel);
        whatIfs.add(whatIfPanel);
        whatIf.add(row);
        this.revalidate();
    }

    private void onClickReset(ActionEvent actionEvent)
    {
        defaultAmount.setValue(10000.00);
        whatIfs.clear();
        whatIf.removeAll();
        scrollPane.setViewportView(whatIf);
        this.revalidate();
    }

    private void onClickCurrent(ActionEvent actionEvent)
    {
        Date today = new Date();
        int yesterdayCount = 0;
        double sum = 0.0;
        try
        {
            sum = Double.parseDouble(moneyFormat.parse(defaultAmount.getText()).toString());
        } catch (Exception ignored)
        {
        }
        for (WhatIfPanel possibility : whatIfs)
        {
            long diff = getDateDiffDiff(today, possibility);
            if (diff >= 0)
            {
                sum += possibility.getAmount();
            } else
            {
                yesterdayCount++;
            }
        }
        if (yesterdayCount > 0)
        {
            JOptionPane.showMessageDialog(this,
                    "At least one maturity date happened already -- its value was ignored");
        }
        JOptionPane.showMessageDialog(this, sum);
    }

    private void onClickFuture(ActionEvent actionEvent)
    {
        JOptionPane.showMessageDialog(this, specifiedDate, "Enter specified maturity date", JOptionPane.PLAIN_MESSAGE);

        int selectedYear = specifiedDate.getYear();
        int selectedMonth = specifiedDate.getMonthNumber();
        int selectedDay = specifiedDate.getDay();
        JOptionPane.showMessageDialog(this, (selectedMonth + 1) + "/" + selectedDay + "/" + selectedYear);

        Date today = new Date();
        Date specified = new GregorianCalendar(selectedYear, selectedMonth, selectedDay).getTime();

        int yesterdayCount = 0;
        double sum = 0.0;
        try
        {
            sum = Double.parseDouble(moneyFormat.parse(defaultAmount.getText()).toString());
        } catch (Exception ignored)
        {
        }
        for (WhatIfPanel possibility : whatIfs)
        {
            long diff = getDateDiffDiff(today, possibility);
            if (diff >= 0)
            {
                sum += possibility.getForwardAmount(specified);
            } else
            {
                yesterdayCount++;
            }
        }

        if (yesterdayCount > 0)
        {
            JOptionPane.showMessageDialog(this,
                    "At least one maturity date happened already -- its value was ignored");
        }
        JOptionPane.showMessageDialog(this, sum);
    }

    private static long getDateDiffDiff(Date today, WhatIfPanel possibility)
    {
        DatePanel maturityDate = possibility.getMaturityDate();
        Date maturity = new GregorianCalendar(maturityDate.getYear(),
                maturityDate.getMonthNumber(), maturityDate.getDay()).getTime();
        long diffInMs = maturity.getTime() - today.getTime();
        return TimeUnit.DAYS.convert(diffInMs, TimeUnit.MILLISECONDS);
    }

    private void onClickDelete(ActionEvent actionEvent)
    {
        DeleteButton button = (DeleteButton) actionEvent.getSource();
        int option = JOptionPane.showConfirmDialog(scrollPane,
                "You are about to delete an entry. Are you sure? ",
                "Warning!", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION)
        {
            whatIf.remove(button.getParent());
            whatIfs.remove((WhatIfPanel) button.getComponentToBeDeleted());
            whatIf.revalidate();
            scrollPane.setViewportView(whatIf);
            scrollPane.revalidate();
        }
    }
}
