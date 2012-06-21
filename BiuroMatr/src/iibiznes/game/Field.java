
package iibiznes.game;

import iibiznes.fields.FieldInfo;

/**
 *
 * @author grzes
 */
public abstract class Field
{
    public Field(FieldInfo fieldInfo, Game game)
    {
        this.fieldInfo = fieldInfo;
        this.game = game;
    }
    
    abstract public void arrive(Player player);

    public FieldInfo getFieldInfo()
    {
        return fieldInfo;
    }
    
    abstract public String getDescription();
    
    protected final FieldInfo fieldInfo;
    protected final Game game;
}
