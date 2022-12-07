
package helpers;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * DatePanel displays the months, days of the month, and years in a dropdown
 */
public class DatePanel extends JPanel
{
    private JComboBox<String> month;
    private JComboBox<String> year;
    private int lastDay;
    private JComboBox<String> day;
    private final String[] monthNames = new String[]{"January", "February", "March", "April",
            "May", "June", "July", "August", "September", "October", "November", "December"};

    public DatePanel()
    {
        setLayout(new FlowLayout());

        setSize(35, 15);

        setUpMonth();
        setUpYear();

        month.addActionListener(this::onChange);
        year.addActionListener(this::onChange);

        validateNumberDays();
        setUpDay();

        add(year);
    }

    /**
     * Method to set up the values for the month JComboBox
     */
    private void setUpMonth()
    {
        month = new JComboBox<>(monthNames);
        month.setEditable(false);
        DateFormat format = new SimpleDateFormat("MMMM");
        month.setSelectedItem(format.format(new Date())); // set selected month to current month
        add(month);
    }

    /**
     * Method to set up the values for the year JComboBox
     */
    private void setUpYear()
    {
        year = new JComboBox<>();
        year.setEditable(false);
        for (int i = 1000; i < 6000; i++)
        {
            year.addItem(i + "");
        }
        DateFormat format = new SimpleDateFormat("yyyy");
        year.setSelectedItem(format.format(new Date())); // set selected year to current year
    }

    /**
     * Every time a month or year is changed, the number of days also changes
     * Updates number of days and adds them again to the DatePanel
     *
     * @param actionEvent - the action of changing selected month or year
     */
    private void onChange(ActionEvent actionEvent)
    {
        if (day != null)
        {
            remove(day);
            remove(year);
        }
        validateNumberDays();
        setUpDay();
        add(year);
    }

    /**
     * Set up days for the JComboBox
     */
    private void setUpDay()
    {
        int dayOfMonth;
        boolean alreadySet = (day != null);
        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);

        if (!alreadySet)
        {
            dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        } else
        {
            dayOfMonth = Integer.parseInt(Objects.requireNonNull(day.getSelectedItem()).toString());
        }

        day = new JComboBox<>();
        day.setSize(5, this.getHeight());

        for (int i = 1; i <= lastDay; i++)
        {
            day.addItem(i + "");
        }

        day.setSelectedItem(dayOfMonth > lastDay ? cal.get(Calendar.DAY_OF_MONTH) + "" : dayOfMonth + "");

        add(day);
    }

    /**
     * Method to set up the last day depending on the selected month and year
     */
    public void validateNumberDays()
    {
        // check if the selected year is a leap year
        int selectedYear = Integer.parseInt(Objects.requireNonNull(year.getSelectedItem()).toString());
        boolean leapYear;
        if (selectedYear % 400 == 0)
        {
            leapYear = true;
        } else if (selectedYear % 100 == 0)
        {
            leapYear = false;
        } else leapYear = selectedYear % 4 == 0;

        // 30 days has September, April, June, and November
        // All the rest have 31,
        // Except February, which has 28 - and 29 in a leap year
        switch (Objects.requireNonNull(month.getSelectedItem()).toString())
        {
            case "September":
            case "April":
            case "June":
            case "November":
                lastDay = 30;
                break;
            case "January":
            case "March":
            case "May":
            case "July":
            case "August":
            case "October":
            case "December":
                lastDay = 31;
                break;
            case "February":
                lastDay = leapYear ? 29 : 28;
                break;
        }
    }

    /**
     * Getter for month dropdown
     *
     * @return month JComboBox
     */
    public JComboBox<String> getMonthComboBox()
    {
        return month;
    }


    /**
     * Getter for year dropdown
     *
     * @return year JComboBox
     */
    public JComboBox<String> getYearComboBox()
    {
        return year;
    }

    /**
     * Getter for month dropdown
     *
     * @return month JComboBox
     */
    public JComboBox<String> getDayComboBox()
    {
        return day;
    }

    /**
     * Getter for year value
     *
     * @return the year
     */
    public int getYear()
    {
        return Integer.parseInt(Objects.requireNonNull(getYearComboBox().getSelectedItem()).toString());
    }

    /**
     * Getter for selected month name
     *
     * @return month name
     */

    public String getMonthName()
    {
        return Objects.requireNonNull(getMonthComboBox().getSelectedItem()).toString();
    }

    /**
     * Getter for month number (0 = January, 11 = December)
     *
     * @return month number
     */
    public int getMonthNumber()
    {
        int monthNumber = 0;
        String monthName = getMonthName();
        for (int i = 0; i < monthNames.length; i++)
        {
            if (monthNames[i].equals(monthName))
            {
                monthNumber = i;
                break;
            }
        }
        return monthNumber;
    }

    /**
     * Getter for day of month
     *
     * @return day of month
     */
    public int getDay()
    {
        return Integer.parseInt(Objects.requireNonNull(getDayComboBox().getSelectedItem()).toString());
    }

    /**
     * Generate Date object from the year, month, and day
     *
     * @return generated Date object
     */
    public Date getDate()
    {
        return new GregorianCalendar(getYear(), getMonthNumber(), getDay()).getTime();
    }

    /**
     * Get the number of days between today and Date in DatePanel
     *
     * @return number of days
     */
    public long dateDiffFromToday()
    {
        Date today = new Date();
        Date thisDate = getDate();
        long diffInMs = thisDate.getTime() - today.getTime();
        return TimeUnit.DAYS.convert(diffInMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Get the number of days between specified Date object and Date in DatePanel
     *
     * @param specifiedDate - the specified Date
     * @return number of days
     */
    public long dateDiffFromSpecifiedDate(Date specifiedDate)
    {
        Date thisDate = getDate();
        long diffInMs = thisDate.getTime() - specifiedDate.getTime();
        return TimeUnit.DAYS.convert(diffInMs, TimeUnit.MILLISECONDS);
    }

    /**
     * toString() method - returns the Date in MM-dd-yyyy format
     *
     * @return MM-dd-yyyy format of the Date in the DatePanel
     */
    @Override
    public String toString()
    {
        Date date = getDate();
        Instant instant = date.toInstant();
        LocalDateTime ldt = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        return DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH).format(ldt);
    }
}
