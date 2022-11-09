package finance;

import sandbox.DatePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;

import static main.Main.HOME_CURRENCY;

public class Finance extends JPanel
{
    private double currentValue;
    private int columnLength = 5;

    private JComboBox<String> action;
    private JFormattedTextField amount;
    private JFormattedTextField fxRate;
    private DatePanel maturityDate;
    private JButton doAction;
    private JPanel perform;

    private JComboBox<String> currencyComboBoxFrom;
    private JComboBox<String> currencyComboBoxTo;


    public Finance()
    {
        this.currencyComboBoxFrom = new JComboBox<>();
        this.currencyComboBoxTo = new JComboBox<>();
        currentValue = 10000;

        setSize(900, 500);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    private class PerformActionPanel extends JPanel
    {
        public PerformActionPanel(JComboBox<String> from, JComboBox<String> to)
        {
            JPanel displayAmount = new JPanel();
            displayAmount.setMaximumSize(new Dimension(850, 100));

            JLabel amountInstruction = new JLabel();
            amountInstruction.setText("Current Amount: ");
            displayAmount.add(amountInstruction);

            NumberFormat moneyFormatter = NumberFormat.getCurrencyInstance();

            JLabel currentValueDisplay = new JLabel();
            currentValueDisplay.setText(moneyFormatter.format(currentValue));
            displayAmount.add(currentValueDisplay);

            add(displayAmount);


            JPanel performAction = new JPanel();
            performAction.setMaximumSize(new Dimension(850, 100));

            action = new JComboBox<>(new String[]{"Buy Spot", "Buy Forward", "Sell Short", "Sell Long", "Cover Short Position"});
            action.setEditable(false);
            performAction.add(action);



            performAction.add(from);
            performAction.add(to);

            amount = new JFormattedTextField();
            amount.setValue(500);
            amount.setColumns(columnLength);
            performAction.add(amount);

            fxRate = new JFormattedTextField();
            fxRate.setValue(3.5);
            fxRate.setColumns(columnLength);
            performAction.add(fxRate);

            maturityDate = new DatePanel();
            performAction.add(maturityDate);

            doAction = new JButton();
            doAction.setText("Perform Action");
            doAction.addActionListener(this::onClick);
            performAction.add(doAction);

            add(performAction);
        }

        private void onClick(ActionEvent actionEvent) {
            //add to the database
        }
    }

    private double getCurrentValue()
    {
        return currentValue;
    }

    public void setCurrencyComboBoxFrom(JComboBox<String> financeComboBox)
    {
        this.currencyComboBoxFrom = financeComboBox;
    }

    public void setCurrencyComboBoxTo(JComboBox<String> financeComboBox)
    {
        this.currencyComboBoxTo = financeComboBox;
    }

    public void addPerformActionPanel(){
        add(new PerformActionPanel(currencyComboBoxFrom, currencyComboBoxTo));
    }
}
