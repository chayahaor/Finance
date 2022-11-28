package sandbox;

import javax.swing.*;
import java.awt.*;

import static main.Main.HOME_CURRENCY;

public class InstructionsPanel extends JPanel
{
    public InstructionsPanel()
    {
        setLayout(new GridLayout(1, 6));
        setMaximumSize(new Dimension(1000, 100));

        add(new JLabel("Enter quantity", SwingConstants.RIGHT));

        add(new JLabel("Select currency", SwingConstants.RIGHT));

        add(new JLabel("Maturity date", SwingConstants.RIGHT));

        add(new JLabel("Spot Price FX /" + HOME_CURRENCY, SwingConstants.RIGHT));

        add(new JLabel("Buy or Sell", SwingConstants.RIGHT));

        add(new JLabel("    "));
    }
}
