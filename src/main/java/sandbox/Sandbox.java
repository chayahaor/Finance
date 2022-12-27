package sandbox;

import api.API;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static main.Main.DEFAULT_VALUE;
import static main.Main.HOME_CURRENCY;

public class Sandbox extends JPanel
{
    private JComboBox<String> currencies = new JComboBox<>();
    private final ArrayList<WhatIfPanel> whatIfs = new ArrayList<>();
    private final JPanel whatIfContainer;
    private final JScrollPane scrollPane;
    private NumberFormat moneyFormat;
    private JFormattedTextField defaultAmount;
    private JFormattedTextField rfr;
    private JDateChooser specifiedDate;

    public Sandbox()
    {
        setSize(900, 500);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        API api = new API();
        try
        {
            currencies = api.getSymbolResults();
        } catch (Exception exception)
        {
            JOptionPane.showMessageDialog(this,
                    "Something went wrong getting the currencies: "
                            + exception.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        addStartingValueRow();
        addRiskFreeRateRow();
        addInformativeRow();
        addInstructionsPanelRow();

        whatIfContainer = new JPanel();
        whatIfContainer.setLayout(new BoxLayout(whatIfContainer, BoxLayout.Y_AXIS));
        whatIfContainer.setMaximumSize(new Dimension(850, 450));

        scrollPane = new JScrollPane(whatIfContainer);
        scrollPane.setPreferredSize(new Dimension(850, 0));
        scrollPane.setMaximumSize(new Dimension(850, 450));
        add(scrollPane);

        setUpButtonPanel();
    }

    /**
     * Add JPanel containing starting value to the tab
     */
    private void addStartingValueRow()
    {
        JPanel startingRow = new JPanel();
        startingRow.setMaximumSize(new Dimension(850, 50));

        startingRow.add(new JLabel("Enter the starting value (in " + HOME_CURRENCY + ")     $"));

        moneyFormat = new DecimalFormat("#,##0.00");
        defaultAmount = new JFormattedTextField(moneyFormat);
        defaultAmount.setValue(DEFAULT_VALUE);
        defaultAmount.setColumns(7);
        startingRow.add(defaultAmount);

        add(startingRow);
    }

    /**
     * Add JPanel containing risk-free rate to the tab
     */
    private void addRiskFreeRateRow()
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

    /**
     * Add JPanel informing user that buying and selling is only allowed for today
     */
    private void addInformativeRow()
    {
        JPanel middleRow = new JPanel();
        middleRow.setMaximumSize(new Dimension(850, 50));
        LocalDate today = LocalDate.now();
        String formatted = DateTimeFormatter.ofPattern("MM-dd-yyyy", Locale.ENGLISH).format(today);
        middleRow.add(new JLabel(" *** Play with buying or selling on " + formatted + " ***"));
        add(middleRow);
    }

    /**
     * Add JPanel containing the instructions to the tab
     */
    private void addInstructionsPanelRow()
    {
        JPanel instructionPanel = new JPanel();
        instructionPanel.setLayout(new GridLayout(1, 6));
        instructionPanel.setMaximumSize(new Dimension(850, 100));

        instructionPanel.add(new JLabel("Buy or Sell", SwingConstants.CENTER));
        instructionPanel.add(new JLabel("Enter quantity", SwingConstants.CENTER));
        instructionPanel.add(new JLabel("Select currency", SwingConstants.CENTER));
        instructionPanel.add(new JLabel("Maturity date", SwingConstants.CENTER));
        instructionPanel.add(new JLabel("Forward Price FX /" + HOME_CURRENCY,
                SwingConstants.CENTER));
        instructionPanel.add(new JLabel("    "));

        add(instructionPanel);
    }

    /**
     * Set up JPanel containing all the Sandbox buttons to the tab
     */
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
        generateButton("Show NPV in " + HOME_CURRENCY + " today",
                this::onClickCurrent, calcRow);
        generateButton("Show NPV in " + HOME_CURRENCY + " at specified date",
                this::onClickFuture, calcRow);
        buttonPanel.add(calcRow);

        specifiedDate = new JDateChooser(new Date());
        specifiedDate.setMinSelectableDate(new Date());

        add(buttonPanel);
    }

    /**
     * Generate a sandbox button given parameters
     * @param text - the text of the JButton
     * @param listener - the action listener of the JButton
     * @param panel - the JPanel to add the JButton to
     */
    private void generateButton(String text, ActionListener listener, JPanel panel)
    {
        JButton button = new JButton();
        button.setText(text);
        button.addActionListener(listener);
        panel.add(button);
    }

    /**
     * Resets the Sandbox
     * @param actionEvent - when clicking on reset button
     */
    private void onClickReset(ActionEvent actionEvent)
    {
        defaultAmount.setValue(DEFAULT_VALUE);
        whatIfs.clear();
        whatIfContainer.removeAll();
        scrollPane.setViewportView(whatIfContainer);
        this.revalidate();
    }

    /**
     * Adds another WhatIfPanel to the SandBox
     * @param actionEvent - on clicking the add more button
     */
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

    /**
     * Deletes a specific row from the Sandbox
     * @param actionEvent - when clicking the DeleteButton associated with that row
     */
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

    /**
     * Shows the NPV for today
     * @param actionEvent - on clicking Show NPV Today button
     */
    private void onClickCurrent(ActionEvent actionEvent)
    {
        Date today = new Date();

        calculate(today, today);
    }

    /**
     * Prompts user for a specific date and shows the NPV for that date
     * @param actionEvent - on clicking Show NPV for Specified Date button
     */
    private void onClickFuture(ActionEvent actionEvent)
    {
        Date today = new Date();

        JOptionPane.showMessageDialog(
                this, specifiedDate,
                "Enter specified date", JOptionPane.PLAIN_MESSAGE);
        Date specifiedDay = specifiedDate.getDate();

        calculate(today, specifiedDay);
    }

    /**
     * Calculate the total NPV between today and a specified date
     * @param today - today's date
     * @param specifiedDay - the specified date
     */
    private void calculate(Date today, Date specifiedDay)
    {
        double sum = getInitial();
        for (WhatIfPanel possibility : whatIfs)
        {
            // it is a percentage -- so divide by 100
            double riskFreeRate = Double.parseDouble(rfr.getText()) / 100;
            sum += possibility.getQuantity(riskFreeRate, today, specifiedDay);
        }
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.##");
        JOptionPane.showMessageDialog(this, decimalFormat.format(sum));
    }

    /**
     * Get the initial amount user specified in GUI
     * @return initial amount (or default if something goes wrong)
     */
    private double getInitial()
    {
        double sum;
        try
        {
            sum = Double.parseDouble(moneyFormat.parse(defaultAmount.getText()).toString());
        } catch (Exception exception)
        {
            sum = DEFAULT_VALUE;
        }
        return sum;
    }

}
