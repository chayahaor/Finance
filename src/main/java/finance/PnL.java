package finance;

import api.API;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static main.Main.HOME_CURRENCY;

public class PnL
{
    private final Connection connection;
    private final API api;
    private final double riskFreeRate;

    public PnL(Connection connection, double riskFreeRate)
    {
        api = new API();
        this.connection = connection;
        this.riskFreeRate = riskFreeRate;
    }

    /**
     * Updates and gets the PnL chart
     *
     * @return the PnL chart
     * @throws SQLException - if SQL Connection fails
     * @throws IOException  - if connection to API fails
     */
    public JFreeChart getChart() throws SQLException, IOException
    {
        updatePnL();
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Net Present Value",
                "Date",
                "Profit/Loss",
                createDataset());

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(100, 100));

        return chart;
    }

    /**
     * Update PnL table in database
     *
     * @throws SQLException - if SQL Connection fails
     * @throws IOException  - if connection to API fails
     */
    private void updatePnL() throws SQLException, IOException
    {
        Statement getRecentPnL = connection.createStatement();
        ResultSet mostRecentDateSet = getRecentPnL.executeQuery(
                "Call spGetMostRecentDate();");

        Date mostRecent;
        if (mostRecentDateSet.next())
        {
            mostRecent = mostRecentDateSet.getTimestamp("Date");
        } else
        {
            ResultSet initialDateSet = getRecentPnL.executeQuery("Call spGetInitialDate();");
            if (initialDateSet.next())
            {
                mostRecent = initialDateSet.getTimestamp("ActionDate");
            } else
            {
                mostRecent = new Date();
            }
        }

        //we already have most recent in DB. Start looking for the day after
        mostRecent = new Date(mostRecent.getTime() + (1000*60*60*24));

        Date today = new Date();
        Date yesterday = new Date(today.getTime() - (1000 * 60 * 60 * 24));
        for (Date dayLookingAt = mostRecent;
             dayLookingAt.before(yesterday);
             dayLookingAt = new Date(dayLookingAt.getTime() + (1000 * 60 * 60 * 24)))
        {
            Statement stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery("Call spGetMainDataByCurrency();");
            double totalPnL = 0;
            while (resultSet.next())
            {
                double pnl;
                Date transactionDate = resultSet.getTimestamp("ActionDate");
                String action = resultSet.getString("Action");
                String currency = resultSet.getString("Currency");
                Date maturityDate = resultSet.getTimestamp("MaturityDate");
                double quantity = resultSet.getDouble("Quantity");
                double forwardPrice = resultSet.getDouble("ForwardPrice");
                if (transactionDate.equals(dayLookingAt))
                {
                    String day = dayLookingAt.toString();
                    pnl = forwardPrice - Double.parseDouble(api.convert(currency, HOME_CURRENCY, day));
                } else
                {
                    String day = new Date(dayLookingAt.getTime()
                                          - (1000 * 60 * 60 * 24)).toString();
                    pnl = Double.parseDouble(api.convert(currency, HOME_CURRENCY, day));
                }
                pnl = action.equals("Sell") ? -pnl : pnl;
                double transactionPnL = pnl * quantity;
                if (!maturityDate.before(dayLookingAt))
                {
                    //if not mature, adjust for time
                    long diffInMs = maturityDate.getTime() - dayLookingAt.getTime();
                    double diffInDays = TimeUnit.DAYS.convert(diffInMs, TimeUnit.MILLISECONDS);

                    double time = diffInDays / 365.0;
                    transactionPnL = transactionPnL / (1 + time * riskFreeRate);
                }
                totalPnL += transactionPnL;
            }
            ZoneId defaultZoneId = ZoneId.systemDefault();
            String formattedDate = DateTimeFormatter
                    .ofPattern("yyyy-MM-dd", Locale.ENGLISH)
                    .format(dayLookingAt.toInstant().atZone(defaultZoneId).toLocalDate());

            Statement stmtInsert = connection.createStatement();
            stmtInsert.executeQuery("Call spInsertIntoPnL("
                                    + "'" + formattedDate
                                    + "', " + totalPnL + ");");
        }

    }

    /**
     * Create XY dataset to be used to populate PnL Chart based on PnL table in database
     *
     * @return the XYDataset
     * @throws SQLException - if SQL Connection fails
     */
    private XYDataset createDataset() throws SQLException
    {
        XYSeries pnlData = new XYSeries("Net Present Value");
        Statement stmt = connection.createStatement();
        ResultSet resultSet = stmt.executeQuery("Call spGetPnL();");
        while (resultSet.next())
        {
            pnlData.add(resultSet.getTimestamp("Date").getTime(),
                    resultSet.getDouble("PNL"));
        }

        final XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(pnlData);
        return dataset;
    }

}
