
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
    
    public String chargeDesc()
    {
        if (type.equalsIgnoreCase("Topic"))
            return topicChargeDesc();
        else if (type.equalsIgnoreCase("Other"))
        {
            if (subject.equalsIgnoreCase("Kurs"))
                return courseChargeDesc();
            else return halfDutyChargeDesc();
        }
        else return "Field free of charge";
    }
    
    private String topicChargeDesc()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Temat bez zadań domowych: ")
                .append(BoardInfo.getCharges(name)[0])
                .append("\n").append("(Gdy gracz posiada wszystkie tematy z"
                + " danego przedmiotu opłata jest liczona podwójnie)\n")
                .append("Z jednym zadaniem: ")
                .append(BoardInfo.getCharges(name)[1])
                .append("\n").append("Z dwoma zadaniami: ")
                .append(BoardInfo.getCharges(name)[2])
                .append("\n").append("Z trzema zadaniami: ")
                .append(BoardInfo.getCharges(name)[3])
                .append("\n").append("Z czterema zadaniami: ")
                .append(BoardInfo.getCharges(name)[4])
                .append("\n").append("Z projektem: ")
                .append(BoardInfo.getCharges(name)[5]);
        return sb.toString();
    }

    private String courseChargeDesc()
    {
        return  "Gracz stający na polu kula dwiema kostkami.\n"
                + "Opłata wynosi dziesięciokrotność sumy oczek.\n"
                + "Gdy właściciel pola posiada zarówno Kurs Javy i Kurs C++,"
                + "to opłata jest liczona podwójnie.";     
    }

    private String halfDutyChargeDesc()
    {        
        StringBuilder sb = new StringBuilder();
        sb.append("W przypadku gdy gracz posiada:\n")
          .append("  - 1 półobowiązek -- 100CS\n")
          .append("  - 2 półobowiązki -- 200CS\n")
          .append("  - 3 półobowiązki -- 300CS\n")
          .append("  - 4 półobowiązki -- 400CS\n");
        return sb.toString();
    }
    
    public final String name;
    public final String type;
    public final String subject;
    public final int price;
    public final int midX;
    public final int midY;

}
