
package iibiznes.game.cards;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author grzes
 */
public class CardStack
{
    public CardStack(ArrayList<Card> cards)
    {
        this.cards = cards;
    }
    
    public void shuffle()
    {
        int n = cards.size();
        for (int i = 0; i < cards.size()-1; ++i)
        {
            int j = rand.nextInt(n-i);
            swap(i,j);
        }
    }
    
    private void swap(int i, int j)
    {
        Card pom = cards.get(i);
        cards.set(i, cards.get(j));
        cards.set(j, pom);
    }
        
    public Card next()
    {
        Card card = cards.get(pos);
        pos = (pos + 1) % cards.size();
        return card;
    }

    public void setCards(ArrayList<Card> cards)
    {
        this.cards = cards;
        pos = 0;
    }
    
    ArrayList<Card> cards;
    int pos;
    Random rand = new Random(System.currentTimeMillis());
}
