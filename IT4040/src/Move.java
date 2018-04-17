
public class Move {
	public int row;
	public int col;
	
	public Move() {
		this.row = -1;
		this.col = -1;
	}
	
	public Move(int row, int col) {
		this.row = row;
		this.col = col;
	}

	@Override
	public String toString() {
		return row + " - " + col;
	}
}
