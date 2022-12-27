package finance;

import api.API;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
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

    public JFreeChart getChart() throws SQLException, IOException
    {
        updatePnL();
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Profit and Loss",
                "Date",
                "Profit/Loss",
                createDataset(),
                PlotOrientation.VERTICAL,
                true, true, false);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(100, 100));

        return chart;
    }

    private void updatePnL() throws SQLException, IOException
    {
        Statement getRecentPnL = connection.createStatement();
        ResultSet mostRecentDateSet = getRecentPnL.executeQuery(
                "Call spGetMostRecentDate();");

        Date mostRecent;
        if (mostRecentDateSet.next())
        {
            mostRecent = mostRecentDateSet.getTimestamp("Date");
        }
        else {
            ResultSet initialDateSet = getRecentPnL.executeQuery("Call spGetInitial();");
            if (initialDateSet.next())
            {
                mostRecent = initialDateSet.getTimestamp("ActionDate");
            }
            else {
                mostRecent = new Date();
            }
        }

        Date today = new Date();
        for (Date dayLookingAt = mostRecent;
             dayLookingAt.before(today);
             dayLookingAt = new Date(dayLookingAt.getTime() + (1000 * 60 * 60 * 24)))
        {
            Statement stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery("Call spGetMainDataByCurrency();");
            double totalPnL = 0;
            while (resultSet.next())
            {
                double pnl;
                Date transactionDate = resultSet.getTimestamp("ActionDate");
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

    private XYDataset createDataset() throws SQLException
    {
        XYSeries pnlData = new XYSeries("Profit and Loss");
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
