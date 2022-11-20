package sandbox;

import javax.swing.*;
import java.awt.*;

import static main.Main.HOME_CURRENCY;

public class InstructionsPanel extends JPanel
{
    public InstructionsPanel()
    {
        setLayout(new GridLayout(1, 7));
        setMaximumSize(new Dimension(1000, 100));

        add(new JLabel("Enter amount", SwingConstants.RIGHT));

        add(new JLabel("Select currency", SwingConstants.RIGHT));

        add(new JLabel("Maturity date", SwingConstants.RIGHT));

        add(new JLabel("  FX Rate /USD", SwingConstants.RIGHT));

        add(new JLabel("Forward Rate", SwingConstants.RIGHT));

        add(new JLabel("Buy or Sell", SwingConstants.CENTER));

        add(new JLabel("    "));
    }
}
