
public class ValueMove {
	public int score;
	public Move move;
	
	public ValueMove(int score, int row, int col) {
		this.move = new Move(row, col);
		this.score = score;
	}
}
