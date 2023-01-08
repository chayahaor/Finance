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

public class NpvGraph
{
    private final Connection connection;
    private final API api;
    private final double riskFreeRate;

    public NpvGraph(Connection connection, double riskFreeRate)
    {
        api = new API();
        this.connection = connection;
        this.riskFreeRate = riskFreeRate;
    }

    /**
     * Updates and gets the Npv chart
     *
     * @return the Npv chart
     * @throws SQLException - if SQL Connection fails
     * @throws IOException  - if connection to API fails
     */
    public JFreeChart getChart() throws SQLException, IOException
    {
        updateNpv();
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Net Present Value Over Time",
                "Date",
                "Net Present Value",
                createDataset());

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(100, 100));

        return chart;
    }

    /**
     * Update Npv table in database
     *
     * @throws SQLException - if SQL Connection fails
     * @throws IOException  - if connection to API fails
     */
    private void updateNpv() throws SQLException, IOException
    {
        Statement getRecentNpv = connection.createStatement();
        ResultSet mostRecentDateSet = getRecentNpv.executeQuery(
                "Call spGetMostRecentDate();");

        Date mostRecent;
        if (mostRecentDateSet.next())
        {
            mostRecent = mostRecentDateSet.getTimestamp("Date");
        } else
        {
            ResultSet initialDateSet = getRecentNpv.executeQuery("Call spGetInitialDate();");
            if (initialDateSet.next())
            {
                mostRecent = initialDateSet.getTimestamp("ActionDate");
            } else
            {
                mostRecent = new Date();
            }
        }

        //we already have most recent in DB. Start looking for the day after
        mostRecent = new Date(mostRecent.getTime() + (1000 * 60 * 60 * 24));

        Date today = new Date();
        Date yesterday = new Date(today.getTime() - (1000 * 60 * 60 * 24));
        for (Date dayLookingAt = mostRecent;
             dayLookingAt.before(yesterday);
             dayLookingAt = new Date(dayLookingAt.getTime() + (1000 * 60 * 60 * 24)))
        {
            Statement stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery("Call spGetMainDataByCurrency();");
            double totalNpv = 0;
            while (resultSet.next())
            {
                double npv;
                Date transactionDate = resultSet.getTimestamp("ActionDate");
                String action = resultSet.getString("Action");
                String currency = resultSet.getString("Currency");
                Date maturityDate = resultSet.getTimestamp("MaturityDate");
                double quantity = resultSet.getDouble("Quantity");
                double forwardPrice = resultSet.getDouble("ForwardPrice");
                if (transactionDate.equals(dayLookingAt))
                {
                    String day = dayLookingAt.toString();
                    npv = forwardPrice
                          - Double.parseDouble(api.convert(currency, HOME_CURRENCY, day));
                } else
                {
                    String day = new Date(dayLookingAt.getTime()
                                          - (1000 * 60 * 60 * 24)).toString();
                    npv = Double.parseDouble(api.convert(currency, HOME_CURRENCY, day));
                }
                npv = action.equals("Sell") ? -npv : npv;
                double transactionNpv = npv * quantity;
                if (!maturityDate.before(dayLookingAt))
                {
                    //if not mature, adjust for time
                    long diffInMs = maturityDate.getTime() - dayLookingAt.getTime();
                    double diffInDays = TimeUnit.DAYS.convert(diffInMs, TimeUnit.MILLISECONDS);

                    double time = diffInDays / 365.0;
                    transactionNpv = transactionNpv / (1 + time * riskFreeRate);
                }
                totalNpv += transactionNpv;
            }
            ZoneId defaultZoneId = ZoneId.systemDefault();
            String formattedDate = DateTimeFormatter
                    .ofPattern("yyyy-MM-dd", Locale.ENGLISH)
                    .format(dayLookingAt.toInstant().atZone(defaultZoneId).toLocalDate());

            Statement stmtInsert = connection.createStatement();
            stmtInsert.executeQuery("Call spInsertIntoNpv("
                                    + "'" + formattedDate
                                    + "', " + totalNpv + ");");
        }

    }

    /**
     * Create XY dataset to be used to populate Npv Chart based on Npv table in database
     *
     * @return the XYDataset
     * @throws SQLException - if SQL Connection fails
     */
    private XYDataset createDataset() throws SQLException
    {
        XYSeries netPresentValueData = new XYSeries("Net Present Value");
        Statement stmt = connection.createStatement();
        ResultSet resultSet = stmt.executeQuery("Call spGetNpv();");
        while (resultSet.next())
        {
            netPresentValueData.add(resultSet.getTimestamp("Date").getTime(),
                    resultSet.getDouble("NPV"));
        }

        final XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(netPresentValueData);
        return dataset;
    }

}
