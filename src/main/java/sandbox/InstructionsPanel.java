package sandbox;

import javax.swing.*;
import java.awt.*;

public class InstructionsPanel extends JPanel
{
    public InstructionsPanel()
    {
        setLayout(new GridLayout(1, 7));
        setMaximumSize(new Dimension(1000, 100));

        add(new JLabel("Enter amount", SwingConstants.RIGHT));

        add(new JLabel("Select currency", SwingConstants.RIGHT));

        add(new JLabel("Enter maturity date", SwingConstants.RIGHT));

        add(new JLabel("Enter FX Rate", SwingConstants.RIGHT));

        add(new JLabel("Enter Forward Rate", SwingConstants.RIGHT));

        add(new JLabel("Buy or Sell", SwingConstants.CENTER));

        add(new JLabel("    "));
    }
}
