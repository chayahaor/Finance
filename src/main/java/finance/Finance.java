package finance;

import main.Main;
import org.jfree.chart.ChartPanel;
import sandbox.DatePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;

public class Finance extends JPanel {
    private double currentValue;
    private JLabel userValue;
    private JComboBox<String> action;
    private JFormattedTextField amount;
    private JFormattedTextField fxRate;
    private DatePanel maturityDate;
    private JButton doAction;


    private final JComboBox<String> currencyComboBoxFrom;
    private final JComboBox<String> currencyComboBoxTo;

    public Finance() {
        this.currencyComboBoxFrom = Main.fromCurrency;
        this.currencyComboBoxTo = Main.toCurrency;
        this.currentValue = pullCurrentValue();
        setSize(900, 500);
        setLayout(new FlowLayout());
        add(addActionComponents());
        add(addGraph());
    }

    private double pullCurrentValue() {
        //TODO: replace with DB call
        return 10000;
    }

    private JPanel addActionComponents() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        NumberFormat moneyFormatter = NumberFormat.getCurrencyInstance();
        panel.add(new JLabel("Currently Have: "));
        userValue = new JLabel(moneyFormatter.format(currentValue));
        panel.add(userValue);
        action = new JComboBox<>(new String[]{
                "Buy Spot",
                "Buy Forward",
                "Sell Short",
                "Sell Long",
                "Cover Short Position"});
        panel.add(action);
        panel.add(currencyComboBoxFrom);
        panel.add(currencyComboBoxTo);

        amount = new JFormattedTextField();
        amount.setValue(500);
        amount.setColumns(5);
        panel.add(amount);

        fxRate = new JFormattedTextField();
        fxRate.setValue(3.5);
        fxRate.setColumns(5);
        panel.add(fxRate);

        maturityDate = new DatePanel();
        panel.add(maturityDate);

        doAction = new JButton();
        doAction.setText("Perform Action");
        doAction.addActionListener(this::onClick);
        panel.add(doAction);
        return panel;
    }

    private void onClick(ActionEvent event) {
        //TODO: Store values in DB
    }

    public JPanel addGraph() {
        PnL profitLoss = new PnL();
        JPanel graphPanel = new JPanel();
        graphPanel.setLayout(new BorderLayout());
        ChartPanel chartPanel = new ChartPanel(profitLoss.getChart());
        graphPanel.add(chartPanel, BorderLayout.CENTER);
        return graphPanel;
    }
}
