
package iibiznes.frame;

import iibiznes.fields.BoardInfo;
import iibiznes.fields.FieldInfo;
import iibiznes.fields.SubjectInfo;
import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

/**
 *
 * @author grzes
 */
public class PlayerPanel extends JPanel
{
    PlayerPanel(DisplayInfo di)
    {
        this.di = di;
        initComponents();
        myInit();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblPlayer = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        CS = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        listProp = new javax.swing.JList();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        lblPos = new javax.swing.JLabel();

        lblPlayer.setFont(new java.awt.Font("Cantarell", 1, 18));
        lblPlayer.setText("Player");

        jLabel1.setLabelFor(CS);
        jLabel1.setText("Motywacja:");

        CS.setEditable(false);
        CS.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        CS.setText("300");

        listProp.setModel(model);
        jScrollPane1.setViewportView(listProp);

        jScrollPane2.setViewportView(jScrollPane1);

        jLabel2.setText("Własności");

        jLabel3.setLabelFor(lblPos);
        jLabel3.setText("Na polu:");

        lblPos.setBackground(java.awt.Color.white);
        lblPos.setText("1. Start");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(CS, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addComponent(lblPos, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)))
            .addComponent(lblPlayer, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblPlayer, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(CS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(lblPos, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void myInit()
    {
        setBorder(BorderFactory.createLineBorder(di.myColor()));
        lblPlayer.setText(di.myNick());
        lblPlayer.setForeground(di.myColor());
        CS.setText("" + di.myCS());
        adjustList();
    }
    
    private void adjustList()
    {
        listProp.setCellRenderer( new ListCellRenderer()
        {
            @Override
            public Component getListCellRendererComponent(
                    JList list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus)
            {
                int i = (Integer) value;
                FieldInfo fi = BoardInfo.getFields()[i];
                JLabel lbl = new JLabel(fi.name);
                SubjectInfo si = BoardInfo.topicToSubject(fi.name);
                lbl.setForeground( si == null ? Color.BLACK : si.color );
                return lbl;
            }
        } );
    }
    
    public void update()
    {
        /******* UPDATE PROPERITES ***************************/
        model.removeAllElements();
        for (int i: di.myProperties())
        {
            model.addElement(i);
        }
                
        /******* UPDATE CS ***************************/
        CS.setText(di.myCS() + "");
            
        /******* UPDATE POSITION ***************************/
        try {
            int pos = di.myPositioin();
            FieldInfo fi = BoardInfo.getFields()[pos];
            lblPos.setText((pos+1) + ". " + fi.name);
        } catch (Exception ex) {
            lblPos.setText("???");                
        }
        
        repaint();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField CS;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblPlayer;
    private javax.swing.JLabel lblPos;
    private javax.swing.JList listProp;
    // End of variables declaration//GEN-END:variables

    DisplayInfo di;
    DefaultListModel model = new DefaultListModel();
}
