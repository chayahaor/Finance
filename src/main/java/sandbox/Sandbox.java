package sandbox;

import api.API;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static main.Main.HOME_CURRENCY;

public class Sandbox extends JPanel
{
    private final ArrayList<WhatIfPanel> whatIfs = new ArrayList<>();
    private final JScrollPane scrollPane;
    private final JPanel whatIfContainer;
    private NumberFormat moneyFormat;
    private JFormattedTextField defaultAmount;
    private JFormattedTextField rfr;
    private final JComboBox<String> currencies;
    private JDateChooser specifiedDate;

    public Sandbox() throws IOException
    {
        setSize(900, 500);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        API api = new API();
        currencies = api.getSymbolResults();

        addStartingRow();

        addSecondaryStartingRow();

        addMiddleRow();
        add(new InstructionsPanel());

        whatIfContainer = new JPanel();
        whatIfContainer.setLayout(new BoxLayout(whatIfContainer, BoxLayout.Y_AXIS));
        whatIfContainer.setMaximumSize(new Dimension(850, 450));

        scrollPane = new JScrollPane(whatIfContainer);
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
        buttonPanel.add(actionRow);

        JPanel calcRow = new JPanel();
        generateButton("Show NPV in " + HOME_CURRENCY + " today", this::onClickCurrent, calcRow);
        generateButton("Show NPV in " + HOME_CURRENCY + " at specified date", this::onClickFuture, calcRow);
        buttonPanel.add(calcRow);

        specifiedDate = new JDateChooser(new Date());
        specifiedDate.setMinSelectableDate(new Date());


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
        whatIfContainer.removeAll();
        scrollPane.setViewportView(whatIfContainer);
        this.revalidate();
    }

    private void onClickMore(ActionEvent actionEvent)
    {
        JPanel row = new JPanel(new BorderLayout());
        row.setMaximumSize(new Dimension(1000, 30));

        WhatIfPanel whatIfPanel = new WhatIfPanel(currencies);
        DeleteButton deleteButton = new DeleteButton(whatIfPanel);
        deleteButton.addActionListener(this::onClickDelete);
        row.add(deleteButton, BorderLayout.EAST);
        row.add(whatIfPanel);
        whatIfs.add(whatIfPanel);
        whatIfContainer.add(row);
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
            whatIfContainer.remove(button.getParent());
            whatIfs.remove((WhatIfPanel) button.getComponentToBeDeleted());
            whatIfContainer.revalidate();
            scrollPane.setViewportView(whatIfContainer);
            scrollPane.revalidate();
        }
    }

    private void onClickCurrent(ActionEvent actionEvent)
    {
        Date today = new Date();
        double sum = 0.0;
        sum = getSum(sum);
        for (WhatIfPanel possibility : whatIfs)
        {

            Date maturityDate = possibility.getMaturityDate();
            long diff = daysBetween(maturityDate, today);
            if (diff >= 0)
            {
                sum += possibility.getQuantity();
            }
        }
        displayResults(sum);
    }

    private void onClickFuture(ActionEvent actionEvent)
    {
        Date today = new Date();

        JOptionPane.showMessageDialog(
                this, specifiedDate,
                "Enter specified date", JOptionPane.PLAIN_MESSAGE);
        Date specifiedDay = specifiedDate.getDate();

        JOptionPane.showMessageDialog(this, specifiedDate.getDate().toString());

        double sum = 0.0;
        sum = getSum(sum);
        for (WhatIfPanel possibility : whatIfs)
        {
            Date maturityDate = possibility.getMaturityDate();
            long diff = daysBetween(today, maturityDate);
            if (diff >= 0)
            {
                // it is a percentage -- so divide by 100
                double riskFreeRate = Double.parseDouble(rfr.getText()) / 100;
                sum += possibility.getForwardQuantity(riskFreeRate, today, specifiedDay);
            }
        }

        displayResults(sum);
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

    private void displayResults(double sum)
    {
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.##");
        JOptionPane.showMessageDialog(this, decimalFormat.format(sum));
    }


    public long daysBetween(Date today, Date other)
    {
        //TODO: Change order
        long diffInMillies = other.getTime() - today.getTime();
        return TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }
}
