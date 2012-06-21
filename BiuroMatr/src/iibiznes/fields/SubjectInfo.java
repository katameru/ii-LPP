
package iibiznes.fields;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author grzes
 */
public class SubjectInfo
{
    public SubjectInfo(String name, Color color, int taskCost)
    {
        this.name = name;
        this.color = color;
        this.taskCost = taskCost;
    }
    
    public void addTopic(String topic)
    {
        topics.add(topic);
    }
    
    public int getSize()
    {
        return topics.size();
    }

    public List<String> getTopics()
    {
        return topics;
    }

    public String getName()
    {
        return name;
    }
    
    public static SubjectInfo fromLine(String line) throws WrongLineException
    {        
        String[] tokens = line.split(";", -1 /*empty strings count*/);
        if (tokens.length < 3)
            throw new WrongLineException("Not enough tokens.");
        if (tokens[0].isEmpty())
            throw new WrongLineException("Name may not be empty");
        if (tokens[1].isEmpty())
            throw new WrongLineException("Color may not be empty");
        int cost = 0;
        if (!tokens[2].isEmpty())
            try {
                cost = Integer.parseInt(tokens[2]);
            } catch (NumberFormatException ex) {
                throw new WrongLineException("Cost is not an integer value.");
            }
        return new SubjectInfo(tokens[0],BoardInfo.colorForName(tokens[1]),cost);
    }
    
    public final String name;
    public final Color color;
    public final int taskCost;
    public final List<String> topics = new ArrayList<String>();
}
