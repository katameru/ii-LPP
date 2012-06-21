
package iibiznes.game.cards;

import iibiznes.game.Game;
import iibiznes.game.Player;

/**
 *
 * @author grzes
 */
public class Move extends Card
{
    public Move(int steps)
    {
        this.steps = steps;
    }

    @Override
    public void work(Player pl, Game game)
    {
        String str;
        if (steps >= 0)
            str = "Posuwasz się o " + steps + " pola. Jeżeli przechodzisz przez"
                    + " Start otrzymujesz premię.";
        else str = "Cofasz się o " + (-steps) + " pola";        
        game.getGameIO().card(pl, str);
        
        int pos = pl.getPosition();
        int l = game.getFields().length;
        int newPos = pos + steps;
        if (newPos < 0) newPos += l;
        if (newPos >= l)
        {
            newPos -= l;
            pl.addCS(game.getPremium());
        }
        pl.setPosition(newPos);
        game.getFields()[newPos].arrive(pl);        
    }
    
    int steps;
}
