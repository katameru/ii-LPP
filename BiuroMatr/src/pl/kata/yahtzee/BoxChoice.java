package pl.kata.yahtzee;

public enum BoxChoice {
	ONES, TWOS, THREES, FOURS, FIVES, SIXES, THREE_OF_A_KIND, FOUR_OF_A_KIND, FULL_HOUSE, SMALL_STRAIGHT, LARGE_STRAIGHT, YAHTZEE, CHANCE;
	
	public boolean isUpper() {
		BoxChoice upperSection[] = {ONES, TWOS, THREES, FOURS, FIVES, SIXES};
		for(BoxChoice c : upperSection) {
			if(this.equals(c)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isLower() {
		return !isUpper();
	}
}

 