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

    /**
     * Get the associated JComponent to be deleted
     * @return the JComponent
     */
    public JComponent getComponentToBeDeleted()
    {
        return componentToBeDeleted;
    }
}
