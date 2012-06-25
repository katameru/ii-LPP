
package iibiznes.game.cards;

import iibiznes.game.Game;
import iibiznes.game.Player;

/**
 *
 * @author grzes
 */
public class Loss extends Card
{
    public Loss(int loss, String desc)
    {
        this.loss = loss;
        this.desc = desc;
    }

    @Override
    public void work(Player pl, Game game)
    {
        pl.addCS(-loss);
        String str = desc + " Tracisz " + loss + " motywacji.";
        game.getGameIO().card(pl, str);
    }
    
    int loss;
    String desc;
}
