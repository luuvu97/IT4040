import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class AIPlayerMiniMax extends AIPlayer {

	public AIPlayerMiniMax(GameMain main) {
		super(main);
	}

	@Override
	public Move move() {
		List<Move> moves = new ArrayList<Move>();

		this.getDefAtkScore(this.mySeed);
		 for (int i = 0; i < this.ROWS; i++) {
		 for (int j = 0; j < this.COLS; j++) {
		 System.out.print(this.defAtkScore[i][j] + "\t");
		 }
		 System.out.println();
		 }
		 System.out.println();
		 System.out.println();
		
		ValueMove[] valMove = new ValueMove[this.ROWS * this.COLS];
		for(int row = 0; row < this.ROWS; row++) {
			for(int col = 0; col < this.COLS; col++) {
				if(this.board[row][col] == Seed.EMPTY) {
					valMove[row * this.COLS + col] = new ValueMove(this.defAtkScore[row][col], row, col);
				}else {
					valMove[row * this.COLS + col] = new ValueMove(-1, row, col);
				}
			}
		}
		
		//Sort array decending
		Arrays.sort(valMove, new Comparator<ValueMove>() {
			@Override
			public int compare(ValueMove o1, ValueMove o2) {
				//Sort array decending
				return Integer.compare(o2.score, o1.score);			}
		});
				
		
//		for(ValueMove vm : valMove) {
//			System.out.println(vm.score);
//		}

		int max = Integer.MIN_VALUE;
		for(int i = 0; i < valMove.length - 1; i++) {
			if(valMove[i].score == valMove[i + 1].score) {
				moves.add(new Move(valMove[i].move.row, valMove[i].move.col));
			}else {
				moves.add(new Move(valMove[i].move.row, valMove[i].move.col));
				if(moves.size() > 4) {
					break;
				}
			}
		}
		
		for(Move m : moves) {
			System.out.println(m.row + " - " + m.col + " : " + this.defAtkScore[m.row][m.col]);
		}
		System.out.println("\n\n");
		 
		 
//		ValueMove result = this.minimax(4, mySeed);
		ValueMove result = this.minimax(MAX_DEPTH, this.mySeed, Integer.MIN_VALUE, Integer.MAX_VALUE);
		return result.move;
	}

	public ValueMove minimax(int depth, Seed player) {
		// Generate possible next moves in a List<Move>
		List<Move> candidateMoves = this.getCandidateMoves(player);

		// mySeed is maximizing; while OppSeed is minizing
		int bestScore = (player == this.mySeed) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
		int currentScore;
		int bestRow = -1;
		int bestCol = -1;
		if (candidateMoves.isEmpty() || depth == 0) {
			bestScore = this.evaluate(player);
			System.out.println("EMPTY");
		} else {
			for (Move move : candidateMoves) {
				board[move.row][move.col] = player;
				if (player == this.mySeed) { // mySeed computer - maximizing
					currentScore = minimax(depth - 1, oppSeed).score;
					if (currentScore > bestScore) {
						bestScore = currentScore;
						bestRow = move.row;
						bestCol = move.col;
					}
				} else { // oppSeed - minizing
					currentScore = minimax(depth - 1, mySeed).score;
					if (currentScore < bestScore) {
						bestScore = currentScore;
						bestRow = move.row;
						bestCol = move.col;
					}
				}
				// undo
				board[move.row][move.col] = Seed.EMPTY;
			}
		}
		return new ValueMove(bestScore, bestRow, bestCol);
	}

	public ValueMove minimax(int depth, Seed player, int alpha, int beta) {
		// Generate possible next moves in a List<Move>
		List<Move> candidateMoves = this.getCandidateMoves(player);

		// mySeed is maximizing; while OppSeed is minizing
		int score = 0;
		int bestRow = -1;
		int bestCol = -1;
		if (candidateMoves.isEmpty()) {
			//den luong player nhung doi thu da win => khong co nuoc choi =>  hasWon or Equal
			score = this.evaluate(player);
			if(player == this.mySeed) {	//player has won
				score -= depth * 100000;
			}else {	//computer has won
				score += depth * 100000;
			}
			return new ValueMove(score, bestRow, bestCol);
		}
		if (depth == 0) {
			score = this.evaluate(player);
			return new ValueMove(score, bestRow, bestCol);
		} else {
			for (Move move : candidateMoves) {
				board[move.row][move.col] = player;
				if (player == this.mySeed) { // mySeed computer - maximizing
					score = minimax(depth - 1, oppSeed, alpha, beta).score;
					if (score > alpha) {
						alpha = score;
						bestRow = move.row;
						bestCol = move.col;
					}
				} else { // oppSeed - minizing
					score = minimax(depth - 1, mySeed, alpha, beta).score;
					if (score < beta) {
						beta = score;
						bestRow = move.row;
						bestCol = move.col;
					}
				}
				// undo
				board[move.row][move.col] = Seed.EMPTY;
				if(alpha >= beta) {
					break;
				}
			}
		}
		return new ValueMove((player == this.mySeed) ? alpha : beta, bestRow, bestCol);
	}
}
