package sandbox;

import javax.swing.*;
import java.awt.*;

public class InstructionsPanel extends JPanel
{
    public InstructionsPanel()
    {
        setLayout(new GridLayout(1, 5));
        setMaximumSize(new Dimension(850, 100));

        JLabel amountInstruction = new JLabel();
        amountInstruction.setText("Enter the amount");
        add(amountInstruction);

        JLabel currencyInstruction = new JLabel();
        currencyInstruction.setText("Select the currency");
        add(currencyInstruction);

        JLabel fxRateInstruction = new JLabel();
        fxRateInstruction.setText("Enter the FX Rate");
        add(fxRateInstruction);

        JLabel forwardRateInstruction = new JLabel();
        forwardRateInstruction.setText("Enter the Forward Rate");
        add(forwardRateInstruction);

        JLabel maturityDateInstruction = new JLabel();
        maturityDateInstruction.setText("Enter the maturity date");
        add(maturityDateInstruction);

    }
}
