
package iibiznes.game.cards;

import iibiznes.game.Buyable;
import iibiznes.game.Game;
import iibiznes.game.Player;
import iibiznes.game.Topic;

/**
 *
 * @author grzes
 */
public class Tasks extends Card
{
    public Tasks(int absolute, int percents, boolean projects)
    {
        this.percents = percents;
        this.absolute = absolute;
        this.projects = projects;
    }

    @Override
    public void work(Player pl, Game game)
    {
        if (projects)
        {
            if (absolute > 0) caseAbsPro(pl, game);
            else casePerPro(pl, game);
        }
        else
        {
            if (absolute > 0) caseAbsTask(pl, game);
            else casePerTask(pl, game);            
        }
    }
    
    private void casePerTask(Player pl, Game game)
    {
        int sum = 0;
        for (Buyable b: pl.getProperties())
        {
            if (b instanceof Topic)
            {
                Topic t = (Topic) b;
                if (t.getTasks() < 5)
                    sum += t.getTasks() * t.mySubject().taskCost;
            }
        }
        int res = sum*percents/100;
        pl.addCS(-res);
        String desc = "Musisz uaktualnić Twoje zadania. Każde uaktualnie "
                + "kosztuje Cię " + percents + "% pierwotnej wartości.";
        String loss = " Łączna strata: " + res + " CS.";
        game.getGameIO().card(pl, desc + loss);        
    }
    
    private void caseAbsTask(Player pl, Game game)
    {
        int sum = 0;
        for (Buyable b: pl.getProperties())
        {
            if (b instanceof Topic)
            {
                Topic t = (Topic) b;
                if (t.getTasks() < 5)
                    sum += t.getTasks() * absolute;
            }
        }
        pl.addCS(-sum);
        String desc = "Musisz uaktualnić Twoje zadania. Każde uaktualnie "
                + "kosztuje Cię " + absolute + " CS";
        String loss = " Łączna strata: " + sum + " CS.";
        game.getGameIO().card(pl, desc + loss);        
    }
    
    private void casePerPro(Player pl, Game game)
    {
        int sum = 0;
        for (Buyable b: pl.getProperties())
        {
            if (b instanceof Topic)
            {
                Topic t = (Topic) b;
                if (t.getTasks() == 5)
                    sum += t.mySubject().taskCost;
            }
        }
        int res = sum*percents/100;
        pl.addCS(-res);
        String desc = "Musisz uaktualnić Twoje projekty. Każde uaktualnie "
                + "kosztuje Cię " + percents + "% pierwotnej wartości.";
        String loss = " Łączna strata: " + res + " CS.";
        game.getGameIO().card(pl, desc + loss);        
    }
    
    private void caseAbsPro(Player pl, Game game)
    {
        int sum = 0;
        for (Buyable b: pl.getProperties())
        {
            if (b instanceof Topic)
            {
                Topic t = (Topic) b;
                if (t.getTasks() == 5)
                    sum += absolute;
            }
        }
        pl.addCS(-sum);
        String desc = "Musisz uaktualnić Twoje projekty. Każde uaktualnie "
                + "kosztuje Cię " + absolute + " CS";
        String loss = " Łączna strata: " + sum + " CS.";
        game.getGameIO().card(pl, desc + loss);        
    }
    
    int percents;
    int absolute;
    boolean projects;
}
