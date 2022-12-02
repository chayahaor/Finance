package sandbox;

import api.API;
import org.jdatepicker.JDatePicker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class Sandbox2 extends JPanel {
    private static final String HOME_CURRENCY = "USD";
    private final ArrayList<WhatIfPanel2> whatIfs = new ArrayList<>();
    private final JScrollPane scrollPane;
    private final WhatIfPanel2 whatIf;
    private NumberFormat moneyFormat;
    private JFormattedTextField defaultAmount;
    private JFormattedTextField rfr;
    private final ArrayList<String> currencyList;
    private final JComboBox<String> currencies;
    private JDatePicker specifiedDate;

    public Sandbox2() throws IOException {
        setSize(900, 500);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        API api = new API();
        currencyList = api.getSymbolResults();
        currencies = new JComboBox<>();
        for (String curr : currencyList)
        {
            currencies.addItem(curr);
        }
        addStartingRow();

        addSecondaryStartingRow();

        addMiddleRow();
        add(new InstructionsPanel());

        //TODO: fix formatting of first what if row.
        // It is not included in any calculations and looks weird
        whatIf = new WhatIfPanel2(currencies);
        whatIf.setLayout(new BoxLayout(whatIf, BoxLayout.Y_AXIS));
        whatIf.setMaximumSize(new Dimension(850, 450));

        scrollPane = new JScrollPane(whatIf);
        scrollPane.setPreferredSize(new Dimension(850, 0));
        scrollPane.setMaximumSize(new Dimension(850, 450));
        add(scrollPane);

        setUpButtonPanel();

        specifiedDate = new JDatePicker();
    }

    private void addStartingRow() {
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

    private void addSecondaryStartingRow() {
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

    private void addMiddleRow() {
        JPanel middleRow = new JPanel();
        middleRow.setMaximumSize(new Dimension(850, 50));
        LocalDate today = LocalDate.now();
        String formatted = DateTimeFormatter.ofPattern("MM-dd-yyyy", Locale.ENGLISH).format(today);
        middleRow.add(new JLabel(" *** Play with buying or selling on " + formatted + " ***"));
        add(middleRow);
    }

    private void setUpButtonPanel() {
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

        add(buttonPanel);
    }

    private void generateButton(String text, ActionListener listener, JPanel panel) {
        JButton button = new JButton();
        button.setText(text);
        button.addActionListener(listener);
        panel.add(button);
    }

    private void onClickReset(ActionEvent actionEvent) {
        defaultAmount.setValue(10000.00);
        whatIfs.clear();
        whatIf.removeAll();
        scrollPane.setViewportView(whatIf);
        this.revalidate();
    }

    private void onClickMore(ActionEvent actionEvent) {
        JPanel row = new JPanel(new BorderLayout());
        row.setMaximumSize(new Dimension(1000, 30));

        WhatIfPanel2 whatIfPanel = new WhatIfPanel2(currencies);
        DeleteButton deleteButton = new DeleteButton(whatIfPanel);
        deleteButton.addActionListener(this::onClickDelete);
        row.add(deleteButton, BorderLayout.EAST);
        row.add(whatIfPanel);
        whatIfs.add(whatIfPanel);
        whatIf.add(row);
        this.revalidate();
    }

    private void onClickDelete(ActionEvent actionEvent) {
        DeleteButton button = (DeleteButton) actionEvent.getSource();
        int option = JOptionPane.showConfirmDialog(scrollPane,
                "You are about to delete an entry. Are you sure? ",
                "Warning!", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION)
        {
            whatIf.remove(button.getParent());
            whatIfs.remove((WhatIfPanel2) button.getComponentToBeDeleted());
            whatIf.revalidate();
            scrollPane.setViewportView(whatIf);
            scrollPane.revalidate();
        }
    }

    private void onClickCurrent(ActionEvent actionEvent) {
        ZoneId defaultZoneId = ZoneId.systemDefault();
        Date today = Date.from(LocalDate.now().atStartOfDay(defaultZoneId).toInstant());

        int yesterdayCount = 0;
        double sum = 0.0;
        //TODO: add starting sum
        for (WhatIfPanel2 possibility : whatIfs)
        {

            Date maturityDate = possibility.getMaturityDate();
            long diff = daysBetween(maturityDate, today);
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

    private void onClickFuture(ActionEvent actionEvent) {
        ZoneId defaultZoneId = ZoneId.systemDefault();
        Date today = Date.from(LocalDate.now().atStartOfDay(defaultZoneId).toInstant());

        JOptionPane.showMessageDialog(this, specifiedDate, "Enter specified date", JOptionPane.PLAIN_MESSAGE);
        Calendar selectedValue = (Calendar) specifiedDate.getModel().getValue();
        Date specifiedDay = selectedValue.getTime();

        while (daysBetween(specifiedDay, today) > 0)
        {
            JOptionPane.showMessageDialog(this, specifiedDate, "Specified date already occurred - try again", JOptionPane.INFORMATION_MESSAGE);
            selectedValue = (Calendar) specifiedDate.getModel().getValue();
            specifiedDay = selectedValue.getTime();
        }

        JOptionPane.showMessageDialog(this, specifiedDate.toString());

        int yesterdayCount = 0;
        double sum = 0.0;
        //TODO: add starting sum
        for (WhatIfPanel2 possibility : whatIfs)
        {
            Date maturityDate = possibility.getMaturityDate();
            long diff = daysBetween(today, maturityDate);
            if (diff >= 0)
            {
                // it is a percentage -- so divide by 100
                double riskFreeRate = Double.parseDouble(rfr.getText()) / 100;
                sum += possibility.getForwardQuantity(riskFreeRate, today, specifiedDay);
            } else
            {
                yesterdayCount++;
            }
        }

        displayResults(yesterdayCount, sum);
    }


    private void displayResults(int yesterdayCount, double sum) {
        if (yesterdayCount > 0)
        {
            JOptionPane.showMessageDialog(this,
                    "At least one maturity date happened already -- its value was ignored");
            //TODO: Why was it ignored?
        }
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        JOptionPane.showMessageDialog(this, decimalFormat.format(sum));
    }


    public long daysBetween(Date today, Date other) {
        long diffInMillies = other.getTime() - today.getTime();
        return TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS); //TODO: change order?
    }
}
