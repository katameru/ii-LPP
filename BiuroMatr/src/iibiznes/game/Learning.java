
package iibiznes.game;

import iibiznes.fields.FieldInfo;

/**
 *
 * @author grzes
 */
public class Learning extends Field
{

    public Learning(FieldInfo fieldInfo, Game game, int loss)
    {
        super(fieldInfo, game);
        this.loss = loss;
    }

    public int getLoss()
    {
        return loss;
    }

    @Override
    public void arrive(Player player)
    {
        player.addCS(-loss);
        game.getGameIO().loss(player, "Zużywasz ChcenieSię na naukę, bo niedługo "
                + fieldInfo.name, loss);
    }

    @Override
    public String getDescription()
    {
        return "Stając na tym polu tracisz " + loss + " punktów ChceniaSię.";
    }
    
    int loss;
}
