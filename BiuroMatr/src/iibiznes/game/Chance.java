
package iibiznes.game;

import iibiznes.fields.FieldInfo;
import iibiznes.game.cards.Card;
import iibiznes.game.cards.CardStack;

/**
 *
 * @author grzes
 */
public class Chance extends Field
{
    public Chance(FieldInfo fieldInfo, Game game, int color)
    {
        super(fieldInfo, game);
        this.color = color;
    }
    
    @Override
    public String getDescription()
    {
        return "Stając na tym polu losujesz kartę z szansą.";
    }
    
    @Override
    public void arrive(Player player)
    {
        Card card;
        if (color == RED) card = redStack.next();
        else card = blueStack.next();
        card.work(player, game);
    }
    
    public static void setStacks(CardStack redStack, CardStack blueStack)
    {
        Chance.redStack = redStack;
        Chance.blueStack = blueStack;
    }
    
    public static final int RED = 0, BLUE = 1;
    int color;
    static CardStack redStack;
    static CardStack blueStack;
}
