package finance;

import sandbox.DatePanel;

import javax.swing.*;

public class Finance extends JPanel
{
    private JTextArea currentValue;
    private JComboBox<String> action;
    private JComboBox<String> initialCurrency;
    private JComboBox<String> toCurrency;
    private JFormattedTextField amount;
    private JFormattedTextField fxRate;
    private DatePanel maturityDate;
    private JButton doAction;

    public Finance()
    {
        setSize(900, 500);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));


    }
}
