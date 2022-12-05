package sandbox;

import javax.swing.*;
import java.awt.*;


public class InstructionsPanel extends JPanel {
    private static final String HOME_CURRENCY = "USD";

    public InstructionsPanel() {
        setLayout(new GridLayout(1, 6));
        setMaximumSize(new Dimension(850, 100));

        add(new JLabel("Buy or Sell", SwingConstants.CENTER));

        add(new JLabel("Enter quantity", SwingConstants.CENTER));

        add(new JLabel("Select currency", SwingConstants.CENTER));

        add(new JLabel("Maturity date", SwingConstants.CENTER));

        add(new JLabel("Spot Price FX /" + HOME_CURRENCY, SwingConstants.CENTER));

        add(new JLabel("    "));
    }
}
