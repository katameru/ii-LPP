
package iibiznes.game;

import iibiznes.fields.FieldInfo;
import iibiznes.fields.SubjectInfo;
import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author grzes
 */
public class Player
{
    public Player(String name, Color color, int CS)
    {
        this.name = name;
        this.color = color;
        this.CS = CS;
        id = nextId++;
        pcs = new PropertyChangeSupport(this);
    }
    
    public boolean hasAll(SubjectInfo subject)
    {
        List<String> topics = subject.getTopics();
        for (String topic: topics)
        {
            if (!hasTopic(topic))
                return false;
        }
        return true;
    }
    
    public boolean hasTopic(String topic)
    {
        for (Field field: properties)
        {
            if (field.getFieldInfo().name.equals(topic))
                return true;
        }
        return false;
    }
    
    public int halfDuties()
    {
        int count = 0;
        for (Field field: properties)
        {
            FieldInfo fi = field.getFieldInfo();
            if (fi.type.equals("Other") && fi.subject.equals("Półobowiązek"))
                count++;
        }
        return count;
    }
    
    public int coursers()
    {
        int count = 0;
        for (Field field: properties)
        {
            FieldInfo fi = field.getFieldInfo();
            if (fi.type.equals("Other") && fi.subject.equals("Kurs"))
                count++;
        }
        return count;
    }

    public int getCS()
    {
        return CS;
    }
    
    public void addCS(int nominal)
    {
        CS += nominal;
        pcs.firePropertyChange("CS", CS-nominal, CS);
    }

    public Color getColor()
    {
        return color;
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public int getPosition()
    {
        return position;
    }

    public void setPosition(int position)
    {
        pcs.firePropertyChange("Position", this.position, position);
        this.position = position;
    }    

    public ArrayList<Buyable> getProperties()
    {
        return properties;
    }
    
    public void addProperty(Buyable b)
    {
        properties.add(b);
        pcs.firePropertyChange("NewProperty", null, null);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        pcs.addPropertyChangeListener(listener);
    }

    public int getInPrison()
    {
        return inPrison;
    }

    public void setInPrison(int inPrison)
    {
        pcs.firePropertyChange("InPrison", this.inPrison, inPrison);
        this.inPrison = inPrison;
    }
    
    
    public final String name;
    public final Color color;
    public final int id;
    boolean inGame = true;
    private int inPrison;
    private int CS;
    private int position = 0;
    private PropertyChangeSupport pcs;
    private ArrayList<Buyable> properties = new ArrayList<Buyable>();
    private static int nextId = 0;
}
