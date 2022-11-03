package sandbox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

public class Sandbox extends JPanel
{
    private ArrayList<WhatIfPanel> whatIfs = new ArrayList<>();
    private JPanel whatIf;

    public Sandbox()
    {
        setSize(900, 500);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JScrollPane scrollPane;
        whatIf = new JPanel();
        whatIf.setLayout(new BoxLayout(whatIf, BoxLayout.Y_AXIS));
        whatIf.setMaximumSize(new Dimension(850, 300));

        scrollPane = new JScrollPane(whatIf);
        scrollPane.setSize(new Dimension(850, 300));
        add(scrollPane);

        JButton btnAddMore = new JButton();
        btnAddMore.setText("Add another what if row");
        btnAddMore.addActionListener(this::onClick);
        add(btnAddMore);
    }

    private void onClick(ActionEvent actionEvent)
    {
        WhatIfPanel whatIfPanel = new WhatIfPanel();
        whatIf.add(whatIfPanel);
        whatIfs.add(whatIfPanel);
        this.validate();
    }

}
