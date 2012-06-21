
package iibiznes.game;

import iibiznes.fields.FieldInfo;

/**
 *
 * @author grzes
 */
class ToPrison extends Field
{

    public ToPrison(FieldInfo fieldInfo, Game game)
    {
        super(fieldInfo, game);
    }

    @Override
    public void arrive(Player player)
    {
        player.setInPrison(2);
        player.setPosition(prisonField);
        game.getGameIO().goToPrison(player);
    }

    @Override
    public String getDescription()
    {
        return "Jeśli staniesz na tym polu będziesz musiał spędzić 2 tury w"
                + "ryjcu.";
    }
    
    static int prisonField;    
}
