
package iibiznes.game;

import iibiznes.fields.FieldInfo;
import java.awt.Color;

/**
 *
 * @author grzes
 */
public abstract class Buyable extends Field
{
    public Buyable(FieldInfo fieldInfo, Game game)
    {
        super(fieldInfo, game);
        price = fieldInfo.price;
    }
    
    public int getPrice()
    {
        return price;
    }
    
    public abstract int getCharge();
    
    public Player getOwner()
    {
        return owner;
    }

    public void setOwner(Player owner)
    {
        this.owner = owner;
    }
    
    @Override
    public void arrive(Player player)
    {
        GameIO gameIO = game.getGameIO();
        if (owner == null)
        {
            gameIO.maybeBuy(player, this);
        }
        else if (owner == player)
        {
            gameIO.myField(player);
        }
        else
        {
            int charge = getCharge();
            gameIO.charge(player, owner, charge);
            player.addCS( -charge );
            owner.addCS( charge );
        }
    }

    public abstract Color getColor();
    
    protected final int price;
    protected Player owner;
}
