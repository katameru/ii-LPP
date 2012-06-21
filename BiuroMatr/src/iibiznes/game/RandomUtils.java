
package iibiznes.game;

import java.util.Random;

/**
 *
 * @author grzes
 */
public class RandomUtils
{
    {
        rand = new Random(System.currentTimeMillis());
    }
    
    public int rollADice()
    {
        return 1 + rand.nextInt(6);
    }
    
    public Pair rollTheDices()
    {
        return new Pair(rollADice(), rollADice());
    }
    
    Random rand;    
}
