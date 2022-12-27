package sandbox;

import javax.swing.*;
import java.awt.*;

public class DeleteButton extends JButton
{
    private final JComponent componentToBeDeleted;

    public DeleteButton(JComponent componentToBeDeleted)
    {
        this.componentToBeDeleted = componentToBeDeleted;
        setText("DELETE ENTRY");
        setForeground(Color.RED);
    }

    public JComponent getComponentToBeDeleted()
    {
        return componentToBeDeleted;
    }
}
