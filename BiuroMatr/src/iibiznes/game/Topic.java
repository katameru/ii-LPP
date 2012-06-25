
package iibiznes.game;

import iibiznes.fields.BoardInfo;
import iibiznes.fields.FieldInfo;
import iibiznes.fields.SubjectInfo;
import java.awt.Color;

/**
 *
 * @author grzes
 */
public class Topic extends Buyable
{
    public Topic(FieldInfo fieldInfo, Game game)
    {
        super(fieldInfo, game);
    }


    @Override
    public void setOwner(Player owner)
    {
        this.owner = owner;
        tasks = 0;
    }
    
    @Override
    public int getCharge()
    {
        int charge = BoardInfo.getCharges(fieldInfo.name)[tasks];
        if (tasks == 0 && owner.hasAll(mySubject()))
            return 2*charge;
        else return charge;
    }
    
    public SubjectInfo mySubject()
    {
        return BoardInfo.topicToSubject(fieldInfo.name);
    }

    public int getTasks()
    {
        return tasks;
    }
    
    @Override
    public String getDescription()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Temat bez zadań domowych: ")
                .append(BoardInfo.getCharges(fieldInfo.name)[0])
                .append("\n").append("(Gdy gracz posiada wszystkie tematy z"
                + " danego przedmiotu opłata jest liczona podwójnie)\n")
                .append("Z jednym zadaniem: ")
                .append(BoardInfo.getCharges(fieldInfo.name)[1])
                .append("\n").append("Z dwoma zadaniami: ")
                .append(BoardInfo.getCharges(fieldInfo.name)[2])
                .append("\n").append("Z trzema zadaniami: ")
                .append(BoardInfo.getCharges(fieldInfo.name)[3])
                .append("\n").append("Z czterema zadaniami: ")
                .append(BoardInfo.getCharges(fieldInfo.name)[4])
                .append("\n").append("Z projektem: ")
                .append(BoardInfo.getCharges(fieldInfo.name)[5]);
        return sb.toString();
    }
    
    @Override
    public Color getColor()
    {
        return mySubject().color;
    }

    int tasks = 0;

}
