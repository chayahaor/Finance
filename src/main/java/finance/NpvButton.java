package finance;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static main.Main.DEFAULT_VALUE;
import static main.Main.HOME_CURRENCY;

public class NpvButton extends JButton
{
    private final Connection connection;
    private final double riskFreeRate;

    public NpvButton(Connection connection, double riskFreeRate)
    {
        this.connection = connection;
        this.riskFreeRate = riskFreeRate;
        this.setText("Get Current NPV in " + HOME_CURRENCY);
        this.addActionListener(this::pullCurrentValue);
    }

    /**
     * Pulls current value from database
     *
     * @param actionEvent - on click Get Current NPV button
     */
    private void pullCurrentValue(ActionEvent actionEvent)
    {
        double currentValue = 0;
        try
        {
            Statement stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery("Call spGetMainDataByCurrency();");
            HashMap<String, Double> quantitiesPerCurrency = new HashMap<>();
            while (resultSet.next())
            {
                calculateRowCurrentValue(resultSet, quantitiesPerCurrency);
            }

            for (String currency : quantitiesPerCurrency.keySet())
            {
                currentValue += quantitiesPerCurrency.get(currency);
            }
        } catch (Exception exception)
        {
            currentValue = DEFAULT_VALUE;
        }

        NumberFormat moneyFormatter = NumberFormat.getCurrencyInstance();
        JOptionPane.showMessageDialog(this, "Current NPV: " + moneyFormatter.format(currentValue));
    }

    /**
     * Update the quantity/currency HashMap with the data from specific row in database
     *
     * @param resultSet             - the current row in the database
     * @param quantitiesPerCurrency - the HashMap being updated with the results
     * @throws SQLException - if SQL Connection fails
     */
    private void calculateRowCurrentValue(ResultSet resultSet,
                                          HashMap<String, Double> quantitiesPerCurrency)
            throws SQLException
    {
        String currentCurrency = resultSet.getString("Currency");
        double sum = quantitiesPerCurrency.get(currentCurrency) == null
                ? 0.0 : quantitiesPerCurrency.get(currentCurrency);

        Date actionDate = resultSet.getTimestamp("ActionDate");
        Date maturityDate = resultSet.getTimestamp("MaturityDate");
        Date specifiedDate = new Date();

        // get difference in days between maturity date and specified date
        // (or between maturity date and action date if maturity date already passed)
        long diffInMs = (maturityDate.getTime() - specifiedDate.getTime() < 0)
                ? maturityDate.getTime() - actionDate.getTime()
                : maturityDate.getTime() - specifiedDate.getTime(); // <-- time until maturity date
        double diffInDays = TimeUnit.DAYS.convert(diffInMs, TimeUnit.MILLISECONDS);

        //convert to currency
        double fxRate = Double.parseDouble(resultSet.getString("ForwardPrice"));
        double quantityFromRow = Double.parseDouble(resultSet.getString("Quantity"));
        double quantityInHomeCurrency = resultSet.getString("Action").equals("Sell")
                ? -(quantityFromRow / fxRate)
                : (quantityFromRow / fxRate);

        // apply maturity formula
        sum += quantityInHomeCurrency / (1 + (diffInDays / 365.0) * riskFreeRate);

        quantitiesPerCurrency.put(currentCurrency, sum);
    }
}
