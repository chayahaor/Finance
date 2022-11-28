package sandbox;

import dagger.DaggerCurrencyExchangeComponent;
import helpers.CurrencyComboBox;
import helpers.DatePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static main.Main.HOME_CURRENCY;

public class Sandbox extends JPanel
{
    private final ArrayList<WhatIfPanel> whatIfs = new ArrayList<>();
    private final JScrollPane scrollPane;
    private final JPanel whatIf;
    private DatePanel specifiedDate;
    private NumberFormat moneyFormat;
    private JFormattedTextField defaultAmount;

    private JFormattedTextField rfr;
    private final CurrencyComboBox currencyComboBox;

    public Sandbox()
    {
        setSize(900, 500);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        currencyComboBox = DaggerCurrencyExchangeComponent
                .create()
                .getCurrencyExchange();
        currencyComboBox.addSymbols();

        addStartingRow();

        addSecondaryStartingRow();

        addMiddleRow();

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

    private void addStartingRow()
    {
        JPanel startingRow = new JPanel();
        startingRow.setMaximumSize(new Dimension(850, 50));

        startingRow.add(new JLabel("Enter the starting value (in " + HOME_CURRENCY + ")     $"));

        moneyFormat = new DecimalFormat("#,##0.00");
        defaultAmount = new JFormattedTextField(moneyFormat);
        defaultAmount.setValue(10000.00);
        defaultAmount.setColumns(7);
        startingRow.add(defaultAmount);

        add(startingRow);
    }

    private void addSecondaryStartingRow()
    {
        JPanel panel = new JPanel();
        panel.setMaximumSize(new Dimension(850, 50));
        panel.add(new JLabel("Enter the " + HOME_CURRENCY + " risk free rate as a percentage"));

        DecimalFormat formatter = new DecimalFormat("#.######");
        rfr = new JFormattedTextField(formatter);
        rfr.setColumns(7);
        rfr.setValue(4);
        panel.add(rfr);

        add(panel);
    }

    private void addMiddleRow()
    {
        JPanel middleRow = new JPanel();
        middleRow.setMaximumSize(new Dimension(850, 50));
        LocalDate today = LocalDate.now();
        String formatted = DateTimeFormatter.ofPattern("MM-dd-yyyy", Locale.ENGLISH).format(today);
        middleRow.add(new JLabel(" *** Play with buying or selling on " + formatted + " ***"));
        add(middleRow);
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

        generateButton("Show NPV in " + HOME_CURRENCY + " today", this::onClickCurrent, calcRow);

        generateButton("Show NPV in " + HOME_CURRENCY + " at specified date", this::onClickFuture, calcRow);

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

    private void onClickReset(ActionEvent actionEvent)
    {
        defaultAmount.setValue(10000.00);
        whatIfs.clear();
        whatIf.removeAll();
        scrollPane.setViewportView(whatIf);
        this.revalidate();
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

    private void onClickCurrent(ActionEvent actionEvent)
    {
        int yesterdayCount = 0;
        double sum = 0.0;
        sum = getSum(sum);
        for (WhatIfPanel possibility : whatIfs)
        {
            DatePanel maturityDate = possibility.getMaturityDate();
            long diff = maturityDate.dateDiffFromToday();
            if (diff >= 0)
            {
                sum += possibility.getQuantity();
            } else
            {
                yesterdayCount++;
            }
        }
        displayResults(yesterdayCount, sum);
    }

    private void onClickFuture(ActionEvent actionEvent)
    {
        JOptionPane.showMessageDialog(this, specifiedDate, "Enter specified date", JOptionPane.PLAIN_MESSAGE);

        while (specifiedDate.dateDiffFromToday() < 0)
        {
            JOptionPane.showMessageDialog(this, specifiedDate, "Specified date already occurred - try again", JOptionPane.INFORMATION_MESSAGE);
        }

        JOptionPane.showMessageDialog(this, specifiedDate.toString());

        int yesterdayCount = 0;
        double sum = 0.0;
        sum = getSum(sum);
        for (WhatIfPanel possibility : whatIfs)
        {
            DatePanel maturityDate = possibility.getMaturityDate();
            long diff = maturityDate.dateDiffFromToday();
            if (diff >= 0)
            {
                // it is a percentage -- so divide by 100
                double riskFreeRate = Double.parseDouble(rfr.getText()) / 100;
                sum += possibility.getForwardQuantity(riskFreeRate, specifiedDate);
            } else
            {
                yesterdayCount++;
            }
        }

        displayResults(yesterdayCount, sum);
    }

    private double getSum(double sum)
    {
        try
        {
            sum = Double.parseDouble(moneyFormat.parse(defaultAmount.getText()).toString());
        } catch (Exception ignored)
        {
        }
        return sum;
    }

    private void displayResults(int yesterdayCount, double sum)
    {
        if (yesterdayCount > 0)
        {
            JOptionPane.showMessageDialog(this,
                    "At least one maturity date happened already -- its value was ignored");
        }
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        JOptionPane.showMessageDialog(this, decimalFormat.format(sum));
    }

}
