
package iibiznes.game;

import iibiznes.fields.FieldInfo;

/**
 *
 * @author grzes
 */
public class Empty extends Field
{

    public Empty(FieldInfo fieldInfo, Game game)
    {
        super(fieldInfo, game);
    }

    @Override
    public void arrive(Player player)
    {
    }

    @Override
    public String getDescription()
    {
        return "Bezpieczne pole.";
    }    
}
