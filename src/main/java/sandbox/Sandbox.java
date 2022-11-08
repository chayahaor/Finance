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

    private DatePanel specifiedDate;

    private final JFormattedTextField defaultAmount;
    private JComboBox<String> currencyComboBox;

    private class DeleteButton extends JButton
    {
        private final WhatIfPanel panelToBeDeleted;
        public DeleteButton(WhatIfPanel panelToBeDeleted)
        {
            this.panelToBeDeleted = panelToBeDeleted;

            setText("DELETE ENTRY");
            setForeground(Color.RED);
            addActionListener(this::onClickDelete);
        }

        private void onClickDelete(ActionEvent actionEvent)
        {
            int option = JOptionPane.showConfirmDialog(scrollPane,
                    "You are about to delete an entry. Are you sure? ",
                    "Warning!", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION)
            {
                panelToBeDeleted.setVisible(false);
                this.setVisible(false);
                whatIf.remove(panelToBeDeleted);
                whatIf.remove(this);
                whatIfs.remove(panelToBeDeleted);
                whatIf.revalidate();
            }
        }

    }

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

    private JButton generateButton(String text, ActionListener listener, JPanel panel)
    {
        JButton button = new JButton();
        button.setText(text);
        button.addActionListener(listener);
        panel.add(button);
        return button;
    }

    private void onClickMore(ActionEvent actionEvent)
    {
        JPanel row = new JPanel(new BorderLayout());
        row.setMaximumSize(new Dimension(1000, 30));

        WhatIfPanel whatIfPanel = new WhatIfPanel(currencyComboBox);
        row.add(new DeleteButton(whatIfPanel), BorderLayout.EAST);
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
        JOptionPane.showMessageDialog(this, "Result goes here.");
    }

    private void onClickFuture(ActionEvent actionEvent)
    {
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
