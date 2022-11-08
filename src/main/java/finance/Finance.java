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
    private JComboBox<String> initialCurrency;
    private JComboBox<String> toCurrency;
    private JFormattedTextField amount;
    private JFormattedTextField fxRate;
    private DatePanel maturityDate;
    private JButton doAction;

    private JComboBox<String> currencyComboBox;

    public Finance()
    {
        currentValue = 10000;

        setSize(900, 500);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(new PerformActionPanel());

        //not sure if I should put this in a class "upper panel"
        //I think the variables need to stay in this class
        //If they can move, we should move this to another class
        /*JPanel frame = new JPanel();
        frame.setLayout(new GridLayout(1, 5));
        frame.setMaximumSize(new Dimension(850, 100));

        currentValueDisplay = new JTextArea(String.valueOf(getCurrentValue()));
        frame.add(currentValueDisplay);*/
    }

    private class PerformActionPanel extends JPanel
    {
        public PerformActionPanel()
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

            currencyComboBox = new JComboBox<>();

            initialCurrency = new JComboBox<>();
            for (int i = 0; i < currencyComboBox.getItemCount(); i++)
            {
                initialCurrency.addItem(currencyComboBox.getItemAt(i));
            }
            initialCurrency.setSelectedItem(HOME_CURRENCY);
            initialCurrency.setEditable(false);
            initialCurrency.setSize(35, 15);
            performAction.add(initialCurrency);

            toCurrency = new JComboBox<>();
            for (int i = 0; i < currencyComboBox.getItemCount(); i++)
            {
                toCurrency.addItem(currencyComboBox.getItemAt(i));
            }
            toCurrency.setSelectedItem(HOME_CURRENCY);
            toCurrency.setEditable(false);
            toCurrency.setSize(35, 15);
            performAction.add(toCurrency);

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

    public void setCurrencyComboBox(JComboBox<String> currencyComboBox)
    {
        this.currencyComboBox = currencyComboBox;
    }

}
