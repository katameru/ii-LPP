
package iibiznes.game;

import iibiznes.fields.FieldInfo;
import java.awt.Color;

/**
 *
 * @author grzes
 */
public class HalfDuty extends Buyable
{

    public HalfDuty(FieldInfo fieldInfo, Game game)
    {
        super(fieldInfo, game);
    }
    
    @Override
    public int getCharge()
    {
        return 100*owner.halfDuties();
    }

    @Override
    public String getDescription()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("W przypadku gdy gracz posiada:\n")
          .append("  - 1 półobowiązek -- 100CS\n")
          .append("  - 2 półobowiązki -- 200CS\n")
          .append("  - 3 półobowiązki -- 300CS\n")
          .append("  - 4 półobowiązki -- 400CS\n");
        return sb.toString();
    }

    @Override
    public Color getColor()
    {
        return Color.DARK_GRAY;
    }
}
