
package iibiznes.fields;

import java.awt.Color;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import javax.imageio.ImageIO;

/**
 *
 * @author grzes
 */
public class BoardInfo
{    
    public static void readConf(String path) throws WrongFileException
    {
        readFields(path + "/fields");
        readTopics(path + "/topics");
        readSubjects(path + "/subjects");
        //readCards(path + "/cards");
        readImages(path);
    }
    
    private static void readFields(String path) throws WrongFileException
    {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(path));
        } catch (FileNotFoundException ex) {
            throw new WrongFileException("Error while reading 'fields': "
                    + "file " + path + " not found.");
        }
        String line = "";
        try {
            line = in.readLine(); //first line is description of columns
        } catch (IOException ex) {
            throw new WrongFileException("IOError while reading 'fields'"
                    + "in line 1.\n" + ex.getMessage());
        }
        ArrayList<FieldInfo> vec = new ArrayList<FieldInfo>();
        for (int i = 2; ; ++i)
        {
            try {
                line = in.readLine();
            } catch (IOException ex) {
                throw new WrongFileException("IOError while reading 'fields'"
                        + "in line " + i + ".\n" + ex.getMessage());
            }
            if (line == null) break;
            try {
                vec.add(FieldInfo.fromLine(line));
            } catch (WrongLineException ex) {
                throw new WrongFileException("Error while reading 'fields'"
                        + "in line " + i + ".\n" + ex.getMessage());
            }
        }
        fields = new FieldInfo[vec.size()];
        for (int i = 0; i < fields.length; ++i)
            fields[i] = vec.get(i);
    }
    
    private static void readSubjects(String path) throws WrongFileException
    {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(path));
        } catch (FileNotFoundException ex) {
            throw new WrongFileException("Error while reading 'subjects': "
                    + "file " + path + " not found.");
        }
        String line = "";
        try {
            line = in.readLine(); //first line is description of columns
        } catch (IOException ex) {
            throw new WrongFileException("IOError while reading 'subjects'"
                    + "in line 1.\n" + ex.getMessage());
        }
        for (int i = 2; ; ++i)
        {
            try {
                line = in.readLine();
            } catch (IOException ex) {
                throw new WrongFileException("IOError while reading 'fields'"
                        + "in line " + i + ".\n" + ex.getMessage());
            }
            if (line == null) break;
            try {
                SubjectInfo si = SubjectInfo.fromLine(line);
                subjects.put(si.name, si);
            } catch (WrongLineException ex) {
                throw new WrongFileException("Error while reading 'subjects'"
                        + "in line " + i + ".\n" + ex.getMessage());
            }
        }
        linkTopicsAndSubjects();
    }
    
    private static void linkTopicsAndSubjects() throws WrongFileException
    {
        for (int i = 0; i < fields.length; ++i)
        {
            if (!fields[i].type.equals("Topic"))
                continue;
            SubjectInfo si = subjects.get(fields[i].subject);
            if (si == null) {
                throw new WrongFileException("Subject of topic \""
                        + fields[i].name + "\" is \"" + fields[i].subject
                        + "\" but it was not found in 'subjects' file.");
            }
            si.addTopic(fields[i].name);
            topicToSub.put(fields[i].name, si);
        }
    }

    private static void readTopics(String path) throws WrongFileException
    {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(path));
        } catch (FileNotFoundException ex) {
            throw new WrongFileException("Error while reading 'topics': "
                    + "file " + path + " not found.");
        }
        String line = "";
        try {
            line = in.readLine(); //first line is description of columns
        } catch (IOException ex) {
            throw new WrongFileException("IOError while reading 'topics'"
                    + "in line 1.\n" + ex.getMessage());
        }
        for (int i = 2; ; ++i)
        {
            try {
                line = in.readLine();
            } catch (IOException ex) {
                throw new WrongFileException("IOError while reading 'topics'"
                        + "in line " + i + ".\n" + ex.getMessage());
            }
            if (line == null) break;
            try {
                topicLine(line);
            } catch (WrongLineException ex) {
                throw new WrongFileException("Error while reading 'topics'"
                        + "in line " + i + ".\n" + ex.getMessage());
            }
        }
    }
    
    private static void topicLine(String line) throws WrongLineException
    {
        String[] tokens = line.split(";");
        if (tokens.length < 7)
            throw new WrongLineException("Not enough tokens.");
        int[] tab = new int[6];
        for (int j = 1; j <= 6; ++j)
        {
            try {
                tab[j-1] = Integer.parseInt(tokens[j]);
            } catch (NumberFormatException ex) {                    
                throw new WrongLineException("Not an integer number.");
            }
        }        
        charges.put(tokens[0], tab);
    }

    private static void readCards(String path)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    private static void readImages(String path) throws WrongFileException
    {
         boardImg = readImage(path, "iibiznes.png");
         dicesImg = readImage(path, "dices.png");
    }
    
    private static Image readImage(String path, String name) throws WrongFileException
    {
        try {
            return ImageIO.read(new File(path + "/" + name));
        } catch (IOException ex) {
            throw new WrongFileException("Error while reading '" + name + "'"
                    + "\n" + ex.getMessage());
        }
    }
        
    public static Color colorForName(String color)
    {
        if (color.equalsIgnoreCase("yellow")) return Color.YELLOW;
        else if (color.equalsIgnoreCase("red")) return Color.RED;
        else if (color.equalsIgnoreCase("blue")) return Color.BLUE;
        else if (color.equalsIgnoreCase("orange")) return Color.ORANGE;
        else if (color.equalsIgnoreCase("green")) return Color.GREEN;
        else if (color.equalsIgnoreCase("violet")) return new Color(128,0,128);
        else if (color.equalsIgnoreCase("brown")) return new Color(128,64,0);
        else if (color.equalsIgnoreCase("black")) return Color.BLACK;
        else if (color.equalsIgnoreCase("white")) return Color.WHITE;
        else return Color.PINK;
    }
    
    
    
    
    public static int[] getCharges(String topic)
    {
        return charges.get(topic);
    }
    
    public static Image getBoardImg()
    {
        return boardImg;
    }

    public static Image getDicesImg()
    {
        return dicesImg;
    }
    
    public static SubjectInfo topicToSubject(String topic)
    {
        return topicToSub.get(topic);
    }

    public static FieldInfo[] getFields()
    {
        return fields;
    }
    
    static Image boardImg, dicesImg;
    static FieldInfo[] fields;
    static Map <String, SubjectInfo> topicToSub = new TreeMap<String, SubjectInfo>();
    static Map <String, SubjectInfo> subjects = new TreeMap<String, SubjectInfo>();
    static Map <String, int[]> charges = new TreeMap<String, int[]>();

}
