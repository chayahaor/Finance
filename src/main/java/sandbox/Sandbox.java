package sandbox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Objects;

import static main.Main.HOME_CURRENCY;

public class Sandbox extends JPanel
{
    private ArrayList<WhatIfPanel> whatIfs = new ArrayList<>();

    private JScrollPane scrollPane;
    private JPanel whatIf;

    private JPanel buttonPanel;
    private JButton btnAddMore;

    private DatePanel specifiedDate;

    private JComboBox<String> currencyComboBox;

    public Sandbox()
    {
        setSize(900, 500);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(new InstructionsPanel());

        // appears this is unnecessary
         currencyComboBox = new JComboBox<>();

        whatIf = new JPanel();
        whatIf.setLayout(new BoxLayout(whatIf, BoxLayout.Y_AXIS));
        whatIf.setMaximumSize(new Dimension(850, 450));

        scrollPane = new JScrollPane(whatIf);
        scrollPane.setPreferredSize(new Dimension(850, 0));
        scrollPane.setMaximumSize(new Dimension(850, 450));
        add(scrollPane);

        buttonPanel = new JPanel();
        buttonPanel.setMaximumSize(new Dimension(850, 100));

        btnAddMore = new JButton();
        btnAddMore.setText("Add another what if row");
        btnAddMore.addActionListener(this::onClick);
        buttonPanel.add(btnAddMore);

        JButton btnResetSandbox = new JButton();
        btnResetSandbox.setText("Reset the Sandbox");
        btnResetSandbox.addActionListener(this::onClickReset);
        buttonPanel.add(btnResetSandbox);

        JButton btnShowCurrentResults = new JButton();
        btnShowCurrentResults.setText("Show amount in " + HOME_CURRENCY + " today");
        btnShowCurrentResults.addActionListener(this::onClickCurrent);
        buttonPanel.add(btnShowCurrentResults);

        JPanel futureRow = new JPanel();
        JButton btnShowFutureResults = new JButton();
        btnShowFutureResults.setText("Show amount in " + HOME_CURRENCY + " at specified maturity date");
        btnShowFutureResults.addActionListener(this::onClickFuture);
        futureRow.add(btnShowFutureResults);

        specifiedDate = new DatePanel();
        futureRow.add(specifiedDate);

        buttonPanel.add(futureRow);

        add(buttonPanel);
    }

    private void onClickReset(ActionEvent actionEvent)
    {
        btnAddMore.setEnabled(true);
        whatIfs.clear();
        whatIf.removeAll();
        scrollPane.setViewportView(whatIf);
    }

    private void onClickFuture(ActionEvent actionEvent)
    {
        btnAddMore.setEnabled(false);

        int selectedYear = Integer.parseInt(Objects.requireNonNull(specifiedDate.getYear().getSelectedItem()).toString());
        int selectedDay = Integer.parseInt(Objects.requireNonNull(specifiedDate.getDay().getSelectedItem()).toString());
        String selectedMonth = Objects.requireNonNull(specifiedDate.getMonth().getSelectedItem()).toString();


        JOptionPane.showMessageDialog(this, selectedMonth + "/" + selectedDay + "/" + selectedYear);
    }

    private void onClickCurrent(ActionEvent actionEvent)
    {
        btnAddMore.setEnabled(false);

       JOptionPane.showMessageDialog(this, "Result goes here.");

    }

    private void onClick(ActionEvent actionEvent)
    {
        WhatIfPanel whatIfPanel = new WhatIfPanel(currencyComboBox);
        whatIf.add(whatIfPanel);
        whatIfs.add(whatIfPanel);
        this.revalidate();
    }

    public void setCurrencyComboBox(JComboBox<String> currencyComboBox)
    {
        this.currencyComboBox = currencyComboBox;
    }
}
