
package iibiznes.game;

import iibiznes.fields.FieldInfo;
import java.awt.Color;
import java.util.Random;

/**
 *
 * @author grzes
 */
public class Course extends Buyable
{

    public Course(FieldInfo fieldInfo, Game game)
    {
        super(fieldInfo, game);
    }
    
    @Override
    public int getCharge()
    {
        //For simplicity i resign from rolling the dices to get a price.
        //Instead it will be randomized without user participation.
        /*String desc = "Opłata za pole " + fieldInfo.name + " zależna jest od"
                + "liczby wyrzuconych oczek. Kulnij kostkami.";
        Pair p = game.getGameIO().takeARoll(desc);*/
        int x = 1 + r.nextInt(6), y = 1 + r.nextInt(6);
        return 10 * owner.coursers() * (x + y);
    }

    @Override
    public String getDescription()
    {
        return  "Gracz stający na polu kula dwiema kostkami.\n"
                + "Opłata wynosi dziesięciokrotność sumy oczek.\n"
                + "Gdy właściciel pola posiada zarówno Kurs Javy i Kurs C++,"
                + "to opłata jest liczona podwójnie.";     
    }

    @Override
    public Color getColor()
    {
        return new Color(63,127,127);
    }
    
    static Random r = new Random(System.currentTimeMillis());
}
