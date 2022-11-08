package finance;

import javax.swing.*;
import java.awt.*;

public class PerformActionPanel extends JPanel
{
    public PerformActionPanel()
    {
        setLayout(new GridLayout(1, 5));
        setMaximumSize(new Dimension(850, 100));

        JLabel amountInstruction = new JLabel();
        amountInstruction.setText("Enter the amount");
        add(amountInstruction);
    }
}
