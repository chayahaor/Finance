package sandbox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class DatePanel extends JPanel
{
    /**
     * DatePanel displays the months and years in a dropdown
     */
    private JComboBox<String> month;
    private JComboBox<String> year;

    private int lastDay;
    private JComboBox<String> day;

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
        month = new JComboBox<>(new String[]{"January", "February", "March", "April",
                "May", "June", "July", "August", "September", "October", "November", "December"});
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

    private void setUpDay()
    {
        day = new JComboBox<>();
        day.setSize(5, this.getHeight());

        for (int i = 1; i <= lastDay; i++)
        {
            day.addItem(i + "");
        }
        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        day.setSelectedItem(dayOfMonth + "");
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
    public JComboBox<String> getMonth()
    {
        return month;
    }

    /**
     * Getter for year dropdown
     *
     * @return year JComboBox
     */
    public JComboBox<String> getYear()
    {
        return year;
    }

    /**
     * Getter for month dropdown
     *
     * @return month JComboBox
     */
    public JComboBox<String> getDay()
    {
        return day;
    }
}
