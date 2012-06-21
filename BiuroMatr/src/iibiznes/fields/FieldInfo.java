
package iibiznes.fields;

/**
 *
 * @author grzes
 */
public class FieldInfo
{
    public FieldInfo(String name, String type, String subject,
                     int price, int midX, int midY)
    {
        this.name = name;
        this.type = type;
        this.subject = subject;
        this.price = price;
        this.midX = midX;
        this.midY = midY;
    }
    
    public static FieldInfo fromLine(String line) throws WrongLineException
    {
        String[] tokens = line.split(";", -1 /*empty strings count*/);
        if (tokens.length < 6)
            throw new WrongLineException("Not enough tokens.");
        if (tokens[0].isEmpty())
            throw new WrongLineException("Name may not be empty");
        if (tokens[1].isEmpty())
            throw new WrongLineException("Type may not be empty");
        int price = 0;
        if (!tokens[3].isEmpty())
            try {
                price = Integer.parseInt(tokens[3]);
            } catch (NumberFormatException ex) {
                throw new WrongLineException("Cost is not an integer value.");
            }
        int x = 0, y = 0;
        try {
            x = Integer.parseInt(tokens[4]);
            y = Integer.parseInt(tokens[5]);
        } catch (NumberFormatException ex) {
            throw new WrongLineException("Coordinate is not an integer value.");
        }
        return new FieldInfo(tokens[0],tokens[1],tokens[2],price, x, y);
    }
    
    public final String name;
    public final String type;
    public final String subject;
    public final int price;
    public final int midX;
    public final int midY;
}
