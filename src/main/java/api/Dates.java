package api;

import org.jdatepicker.JDatePicker;
import org.jdatepicker.constraints.DateSelectionConstraint;
import org.jdatepicker.constraints.WeekdayConstraint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
    }


    public static void main(String[] args) {
        Dates dates = new Dates();
        dates.setVisible(true);
    }
}
