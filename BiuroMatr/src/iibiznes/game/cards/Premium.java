
package iibiznes.game.cards;

import iibiznes.game.Game;
import iibiznes.game.Player;

/**
 *
 * @author grzes
 */
public class Premium extends Card
{
    public Premium(int premium, String desc)
    {
        this.premium = premium;
        this.desc = desc;
    }

    @Override
    public void work(Player pl, Game game)
    {
        pl.addCS(premium);
        String str = desc + " Przybywa Ci " + premium + " CS.";
        game.getGameIO().card(pl, str);
    }
    
    int premium;
    String desc;
}
