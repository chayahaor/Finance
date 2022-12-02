package api;

import org.jdatepicker.JDatePicker;
import org.jdatepicker.constraints.DateSelectionConstraint;
import org.jdatepicker.constraints.WeekdayConstraint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Dates extends JFrame {

    private JDatePicker datePicker;

    public Dates() {
        setTitle("Date Picker");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        datePicker = new JDatePicker();
        DateSelectionConstraint weekdayConstraint = new WeekdayConstraint();
        datePicker.addDateSelectionConstraint(weekdayConstraint);
        add(datePicker);
        Button button = new Button("Read Date");
        add(button);
        datePicker.addActionListener(this::onChange);
    }

    private void onChange(ActionEvent event) {
        Calendar selectedValue = (Calendar) datePicker.getModel().getValue();
        Date selectedDate = selectedValue.getTime();
        System.out.println(selectedDate);

        LocalDate localDate = LocalDate.now();
        ZoneId defaultZoneId = ZoneId.systemDefault();
        Date date = Date.from(localDate.atStartOfDay(defaultZoneId).toInstant());
        long diffInMillies = date.getTime() - selectedDate.getTime();
        System.out.println(TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS));
        //returns negative if in future
        //returns positive if in past
    }


    public static void main(String[] args) {
        Dates dates = new Dates();
        dates.setVisible(true);
    }
}
