package sandbox;

import javax.swing.*;
import java.awt.*;

public class InstructionsPanel extends JPanel
{
    public InstructionsPanel()
    {
        setLayout(new GridLayout(1, 6));
        setMaximumSize(new Dimension(1000, 100));

        JLabel amountInstruction = new JLabel();
        amountInstruction.setText("                Enter amount");
        amountInstruction.setHorizontalTextPosition(SwingConstants.RIGHT);
        add(amountInstruction);

        JLabel currencyInstruction = new JLabel();
        currencyInstruction.setText("      Select currency");
        add(currencyInstruction);

        JLabel maturityDateInstruction = new JLabel();
        maturityDateInstruction.setText("Enter maturity date");
        add(maturityDateInstruction);

        JLabel fxRateInstruction = new JLabel();
        fxRateInstruction.setText("             Enter FX Rate   ");
        add(fxRateInstruction);

        JLabel forwardRateInstruction = new JLabel();
        forwardRateInstruction.setText("          Enter Forward Rate");
        add(forwardRateInstruction);

        JLabel buyOrSellInstruction = new JLabel();
        buyOrSellInstruction.setText("         Buy or Sell");
        add(buyOrSellInstruction);
    }
}
