package guipanels;

import edu.memphis.ccrg.lida.framework.gui.panels.GuiPanelImpl;
import java.awt.Graphics;
import javax.swing.JPanel;

public class ActionPanel extends GuiPanelImpl {
  private String actionStatus;
  
  public ActionPanel()
  {
     actionStatus = "";
  }
  
  public void paintComponent( Graphics g )
  {
	    super.paintComponent( g );
	    int pixel=0;
	    
	    for (pixel=0 ; pixel <= getHeight() ; pixel += 10){
	        g.drawLine(0, pixel, pixel, getHeight());
	        }

	    for (pixel=getHeight() ; pixel >=0 ; pixel -= 10){
	        g.drawLine(0, pixel, getHeight() - pixel, 0);
	    }
	    
	    for (pixel=0 ; pixel <= getHeight() ; pixel +=10){
	        g.drawLine(getWidth(), pixel, getWidth() - pixel, getHeight());
	    }
	    
	    for (pixel=getHeight() ; pixel >=0 ; pixel -= 10){
	        g.drawLine(getWidth(), pixel, getWidth() - (getHeight() - pixel), 0);
	    }
	}
  
  @Override
    public void refresh() 
    {
      repaint();
    }
}
