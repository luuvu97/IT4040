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
//		ValueMove result = this.minimax(4, mySeed);
		ValueMove result = this.minimax(4, this.mySeed, Integer.MIN_VALUE, Integer.MAX_VALUE);
		return result.move;
	}

	public ValueMove minimax(int depth, Seed player) {
		// Generate possible next moves in a List<Move>
		this.showBoardInfo(player);
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
		Seed opp = (player == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
		List<Move> candidateMoves = this.getCandidateMoves1(player);

		// mySeed is maximizing; while OppSeed is minizing
		int score;
		int bestRow = -1;
		int bestCol = -1;
		if (candidateMoves.isEmpty() || depth == 0) {
//			score = this.evaluate(player);
			score = this.evaluate();
			return new ValueMove(score, bestRow, bestCol);
		} else {
			for (Move move : candidateMoves) {
				board[move.row][move.col] = player;
				if (player == this.mySeed) { // mySeed computer - maximizing
					score = minimax(depth - 1, opp, alpha, beta).score;
					if (score > alpha) {
						alpha = score;
						bestRow = move.row;
						bestCol = move.col;
					}
				} else { // oppSeed - minizing
					score = minimax(depth - 1, opp, alpha, beta).score;
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
