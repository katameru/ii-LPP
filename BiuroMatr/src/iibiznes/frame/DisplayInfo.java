
package iibiznes.frame;

import iibiznes.fields.BoardInfo;
import iibiznes.game.NewGameSettings;
import java.awt.Color;
import java.util.Arrays;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author grzes
 */
public class DisplayInfo
{
    public DisplayInfo(JSONObject json) throws JSONException
    {
        players = json.getInt("players");
        
        positions = new Integer[players];
        for (int i = 0; i < players; ++i)
            positions[i] = 0;
        
        JSONArray tmp = json.getJSONArray("nicks");
        nicks = new String[players];
        for (int i = 0; i < players; ++i)
            nicks[i] = tmp.getString(i);
        
        properties = new Integer[BoardInfo.getFields().length];        
        for (int i = 0; i < properties.length; ++i)
            properties[i] = -1;
        
        tmp = json.getJSONArray("colors");
        colors = new Color[players];       
        for (int i = 0; i < players; ++i)
            colors[i] = BoardInfo.colorForName(tmp.getString(i));
        
        tmp = json.getJSONArray("CS");
        CS = new Integer[players];       
        for (int i = 0; i < players; ++i)
            CS[i] = tmp.getInt(i);
    }
    
    public DisplayInfo(NewGameSettings setts)
    {
        players = setts.players;
        
        positions = new Integer[players];
        for (int i = 0; i < players; ++i)
            positions[i] = 0;
        
        nicks = new String[players];
        for (int i = 0; i < players; ++i)
            nicks[i] = setts.names[i];
        
        properties = new Integer[BoardInfo.getFields().length];        
        for (int i = 0; i < properties.length; ++i)
            properties[i] = -1;
        
        colors = new Color[players];       
        for (int i = 0; i < players; ++i)
            colors[i] = setts.colors[i];
        
        CS = new Integer[players];       
        for (int i = 0; i < players; ++i)
            CS[i] = setts.startCS;
    }
    
    public void update(JSONObject json) throws JSONException
    {
        JSONArray tmp = json.getJSONArray("positions");       
        for (int i = 0; i < players; ++i)
            positions[i] = tmp.getInt(i);        
        
        tmp = json.getJSONArray("properties");     
        for (int i = 0; i < players; ++i)
            properties[i] = tmp.getInt(i);        
        
        tmp = json.getJSONArray("CS");     
        for (int i = 0; i < players; ++i)
            CS[i] = tmp.getInt(i);       
    }
    
    public JSONObject toJSON()
    {
        try {
            JSONObject json = new JSONObject();
            json.put("players", players);
            json.put("positions", Arrays.asList(positions));
            json.put("nicks", Arrays.asList(nicks));
            json.put("properties", Arrays.asList(properties));
            json.put("colors", Arrays.asList(defColors));
            json.put("CS", Arrays.asList(CS));
            return json;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public void findMe(String myNick)
    {
        for (int i = 0; i < nicks.length; ++i)
            if (nicks[i].equals(myNick))
                myNr = i;
    }
    
    public Color myColor()
    {
        return colors[myNr];
    }
    
    public String myNick()
    {
        return nicks[myNr];
    }
    
    public int myPositioin()
    {
        return positions[myNr];
    }
    
    public int myCS()
    {
        return CS[myNr];
    }
    
    public int[] myProperties()
    {
        int count = 0;
        for (int owner: properties)
            if (owner == myNr) count++;
        int[] res = new int[count];
        int next = 0;        
        for (int i = 0; i < properties.length; ++i)
            if (properties[i] == myNr)
                res[next++] = i;
        return res;
    }
    
    /**
     * Number of players in game.
     */
    public final int players;
        
    /**
     * Field with index of player at local machine.
     */
    public int myNr = -1;
    
    /**
     * positions[i] is position on board of player i. If player has already lost
     * then his position is below zero.
     */
    public final Integer[] positions;
    
    /**
     * nicks[i] is nick of ith player.
     */
    public final String[] nicks;
    
    /**
     * properties[i] indicates number of player who ownes field i. If nobody
     * owns that field, then value is below zero.
     */
    public final Integer[] properties;
    
    /**
     * colors[i] is color of ith player
     */
    public final Color[] colors;   
    
    /**
     * CS[i] is CS of ith player.
     */
    public final Integer[] CS;
    
    public final String[] defColors = { "red", "blue", "green", "yellow", "violet"};

}
