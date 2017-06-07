/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package guipanels;

import edu.memphis.ccrg.lida.framework.ModuleName;
import edu.memphis.ccrg.lida.framework.gui.panels.GuiPanelImpl;
import java.util.logging.Level;
import java.util.logging.Logger;
import modules.Environment;

/**
 *
 * @author ftanada
 */
public class LidaActionPanel extends GuiPanelImpl 
{
    private static final Logger logger = Logger.getLogger(LidaActionPanel.class.getCanonicalName());

    /**
     * Creates new form LidaActionPanel
     */
    public LidaActionPanel() {
        initComponents();
        jTextArea1.setEditable(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        jLabel1.setText("DemoLida by Fabio Tanada");

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(120, 120, 120)
                        .addComponent(jLabel1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(44, 44, 44)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(71, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(54, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables
    private final static String newLine = "\n";
    private Environment environment;

    public void setText(String sInput)
    {
      jTextArea1.append(sInput+newLine);
    }
    
    @Override
    public void initPanel(String[] param) 
    {
        environment = (Environment) agent.getSubmodule(ModuleName.Environment);
        if (environment != null) 
        {
            refresh();
        }  else 
        {
            logger.log(Level.WARNING,
                    "Unable to parse module {1} Panel not initialized.",
                    new Object[]{0L, param[0]});
        }
    }
    
    @Override
    public void refresh() 
    {
       String sAux;
       
       if (this.environment != null)
       {
           sAux = this.environment.getLastAction();
           if (sAux != null)
           {
               sAux = System.currentTimeMillis() + " " + sAux;
               setText(sAux);
               this.environment.resetLastAction();
           }
       }
    }

}
