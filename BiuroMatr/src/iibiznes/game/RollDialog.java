
package iibiznes.game;

import iibiznes.fields.BoardInfo;
import javax.swing.ImageIcon;

/**
 *
 * @author grzes
 */
public class RollDialog extends javax.swing.JDialog
{
    private RollDialog(java.awt.Frame parent, boolean modal)
    {
        super(parent, modal);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        initComponents();
        myInit();
    }
    
    public static Pair rollTwoDices(java.awt.Frame parent, String desc)
    {
        dialog = new RollDialog(parent, true);
        dialog.lblDesc.setText("<html> " + desc);
        dialog.setVisible(true);
        return value;
    }
    
    public static int rollOneDice(java.awt.Frame parent, String desc)
    {
        dialog = new RollDialog(parent, true);
        dialog.lblDesc.setText("<html> " + desc);
        dialog.oneDice = true;
        dialog.dice2.setVisible(false);
        return value.x;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonRoll = new javax.swing.JButton();
        buttonOK = new javax.swing.JButton();
        lblDesc = new javax.swing.JLabel();
        dice1 = new javax.swing.JTextField();
        dice2 = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        buttonRoll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRollActionPerformed(evt);
            }
        });

        buttonOK.setText("OK");
        buttonOK.setEnabled(false);
        buttonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonOKActionPerformed(evt);
            }
        });

        lblDesc.setText("<html>To jest jakiś napis. To jest jakiś napis. To jest jakiś napis. To jest jakiś napis. ");

        dice1.setEditable(false);

        dice2.setEditable(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(dice1, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dice2, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblDesc, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(buttonOK, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(buttonRoll, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblDesc, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                    .addComponent(buttonRoll, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE))
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonOK)
                    .addComponent(dice1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dice2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonOKActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_buttonOKActionPerformed
    {//GEN-HEADEREND:event_buttonOKActionPerformed
        dispose();
    }//GEN-LAST:event_buttonOKActionPerformed

    private void buttonRollActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_buttonRollActionPerformed
    {//GEN-HEADEREND:event_buttonRollActionPerformed
        value = (new RandomUtils()).rollTheDices();
        dialog.dice1.setText("" + value.x);
        dialog.dice2.setText("" + value.y);
        buttonRoll.setEnabled(false);
        buttonOK.setEnabled(true);
    }//GEN-LAST:event_buttonRollActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonOK;
    private javax.swing.JButton buttonRoll;
    private javax.swing.JTextField dice1;
    private javax.swing.JTextField dice2;
    private javax.swing.JLabel lblDesc;
    // End of variables declaration//GEN-END:variables

    private void myInit()
    {
        buttonRoll.setIcon(new ImageIcon(BoardInfo.getDicesImg()));
    }
    
    boolean oneDice = false;
    static Pair value;
    static RollDialog dialog;
}
