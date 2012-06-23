
package iibiznes.game;

import iibiznes.fields.BoardInfo;
import java.awt.Color;
import java.util.Arrays;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    
    public NewGameSettings(JSONObject json) throws JSONException
    {
        players = json.getInt("players");
        
        JSONArray tmp = json.getJSONArray("names");
        names = new String[players];
        for (int i = 0; i < players; ++i)
            names[i] = tmp.getString(i);
        
        tmp = json.getJSONArray("colors");
        colors = new Color[players];       
        for (int i = 0; i < players; ++i)
            colors[i] = BoardInfo.colorForName(tmp.getString(i));
        
        startCS = json.getInt("startCS");
        premium = json.getInt("premium");
    }
    
    public JSONObject toJSON()
    {
        try {
            JSONObject json = new JSONObject();
            json.put("players", players);
            json.put("names", Arrays.asList(names));
            json.put("colors", Arrays.asList(defColors));
            json.put("startCS", startCS);
            json.put("premium", premium);
            return json;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public final String[] defColors = { "red", "blue", "green", "yellow", "violet"};
    public final int players;
    public final String[] names;
    public final Color[] colors;
    public final int startCS;
    public final int premium;
}
