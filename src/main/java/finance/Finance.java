package finance;

import dagger.DaggerCurrencyExchangeComponent;
import helpers.*;
import org.jfree.chart.ChartPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.text.NumberFormat;

import static main.Main.HOME_CURRENCY;

public class Finance extends JPanel {
    private double currentValue;
    private JLabel userValue;
    private Connection connection;
    private JComboBox<String> action;
    private JFormattedTextField amount;
    private JFormattedTextField fxRate;
    private DatePanel maturityDate;
    private JButton doAction;
    private CurrencyComboBox fromCurrency;
    private CurrencyComboBox toCurrency;

    public Finance(Connection connection) {
        this.currentValue = pullCurrentValue();
        this.connection = connection;
        setSize(900, 500);
        setLayout(new BorderLayout());
        add(doFinancePanel(), BorderLayout.NORTH);
        add(addGraph());
    }

    private double pullCurrentValue() {
        //TODO: replace with DB call
        return 10000;
    }

    private JPanel doFinancePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setSize(new Dimension(500, 200));
        panel.add(addCurrentValue());
        panel.add(addActionComponents());
        return panel;
    }

    private JPanel addCurrentValue() {
        JPanel panel = new JPanel();
        NumberFormat moneyFormatter = NumberFormat.getCurrencyInstance();
        panel.add(new JLabel("Currently Have: "));
        userValue = new JLabel(moneyFormatter.format(currentValue));
        panel.add(userValue);
        return panel;
    }

    private JPanel addActionComponents() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        action = new JComboBox<>(new String[]{
                "Buy Spot",
                "Buy Forward",
                "Sell Short",
                "Sell Long",
                "Cover Short Position"});
        panel.add(action);

        toCurrency = DaggerCurrencyExchangeComponent
                .create()
                .getCurrencyExchange();
        fromCurrency = DaggerCurrencyExchangeComponent
                .create()
                .getCurrencyExchange();

        panel.add(toCurrency);
        panel.add(fromCurrency);

        toCurrency.addSymbols();
        fromCurrency.addSymbols();

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
