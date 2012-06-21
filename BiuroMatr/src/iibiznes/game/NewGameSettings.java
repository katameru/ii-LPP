
package iibiznes.game;

import java.awt.Color;

/**
 *
 * @author grzes
 */
public class NewGameSettings
{
    public NewGameSettings(int players, String[] names, Color[] colors, int startCS, int premium)
    {
        this.players = players;
        this.names = names;
        this.colors = colors;
        this.startCS = startCS;
        this.premium = premium;
    }
    
    public final int players;
    public final String[] names;
    public final Color[] colors;
    public final int startCS;
    public final int premium;
}
