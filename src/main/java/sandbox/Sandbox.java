package sandbox;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

import static main.Main.HOME_CURRENCY;

public class Sandbox extends JPanel
{
    private final ArrayList<WhatIfPanel> whatIfs = new ArrayList<>();

    private final JScrollPane scrollPane;
    private final JPanel whatIf;

    private JButton btnAddMore;

    private DatePanel specifiedDate;

    private final JFormattedTextField defaultAmount;
    private JComboBox<String> currencyComboBox;

    public Sandbox()
    {
        setSize(900, 500);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel startingRow = new JPanel();
        startingRow.setMaximumSize(new Dimension(850, 50));

        startingRow.add(new JLabel("Enter the starting value (in " + HOME_CURRENCY + ")"));

        NumberFormatter defaultFormatter = new NumberFormatter(new DecimalFormat("#.##"));
        int numColumns = 5;
        defaultAmount = new JFormattedTextField(defaultFormatter);
        defaultAmount.setValue(10000.00);
        defaultAmount.setColumns(numColumns);
        startingRow.add(defaultAmount);

        add(startingRow);

        add(new InstructionsPanel());

        currencyComboBox = new JComboBox<>();

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

        btnAddMore = setButton("Add another what if row", this::onClickMore, actionRow);

        setButton("Reset the Sandbox", this::onClickReset, actionRow);

        JPanel calcRow = new JPanel();

        setButton("Show amount in " + HOME_CURRENCY + " today", this::onClickCurrent, calcRow);

        setButton("Show amount in " + HOME_CURRENCY + " at specified maturity date", this::onClickFuture, calcRow);

        specifiedDate = new DatePanel();

        buttonPanel.add(actionRow);
        buttonPanel.add(calcRow);

        add(buttonPanel);
    }

    private JButton setButton(String text, ActionListener listener, JPanel panel)
    {
        JButton button = new JButton();
        button.setText(text);
        button.addActionListener(listener);
        panel.add(button);
        return button;
    }

    private void onClickMore(ActionEvent actionEvent)
    {
        WhatIfPanel whatIfPanel = new WhatIfPanel(currencyComboBox);
        whatIf.add(whatIfPanel);
        whatIfs.add(whatIfPanel);
        this.revalidate();
    }

    private void onClickReset(ActionEvent actionEvent)
    {
        btnAddMore.setEnabled(true);
        defaultAmount.setValue(10000.00);
        whatIfs.clear();
        whatIf.removeAll();
        scrollPane.setViewportView(whatIf);
        this.revalidate();
    }

    private void onClickCurrent(ActionEvent actionEvent)
    {
        btnAddMore.setEnabled(false);

        JOptionPane.showMessageDialog(this, "Result goes here.");
    }

    private void onClickFuture(ActionEvent actionEvent)
    {
        btnAddMore.setEnabled(false);

        JOptionPane.showMessageDialog(this, specifiedDate, "Enter specified maturity date", JOptionPane.PLAIN_MESSAGE);

        int selectedYear = Integer.parseInt(Objects.requireNonNull(specifiedDate.getYear().getSelectedItem()).toString());
        int selectedDay = Integer.parseInt(Objects.requireNonNull(specifiedDate.getDay().getSelectedItem()).toString());
        String selectedMonth = Objects.requireNonNull(specifiedDate.getMonth().getSelectedItem()).toString();

        JOptionPane.showMessageDialog(this, selectedMonth + "/" + selectedDay + "/" + selectedYear);
    }

    public void setCurrencyComboBox(JComboBox<String> currencyComboBox)
    {
        this.currencyComboBox = currencyComboBox;
    }
}
