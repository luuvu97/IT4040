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
		this.countSoNhanhDuyet = 0;
		this.countTongSoNut = 0;
		this.countSoLanCatTia = 0;
		long lStartTime = System.currentTimeMillis();
		ValueMove result = this.minimax(this.MAX_DEPTH, this.mySeed, Integer.MIN_VALUE, Integer.MAX_VALUE);
		long lEndTime = System.currentTimeMillis();
		this.time = lEndTime - lStartTime;
		this.main.dboGame.addInfo(this.countSoUngCuVien, this.countTongSoNut, this.countSoNhanhDuyet, this.time);
		System.out.println("Time: " + this.time + " ms");
		System.out.println("Tong so nhanh :" + this.countTongSoNut);
		System.out.println("Tong so nhanh thuc te phai duyet: " + this.countSoNhanhDuyet);
		System.out.println("So lan cat nhanh: " + this.countSoLanCatTia);
		System.out.println("\n------");
		this.showBoardInfo(this.mySeed);
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
		this.countTongSoNut += candidateMoves.size();
		// mySeed is maximizing; while OppSeed is minizing
		int score;
		int bestRow = -1;
		int bestCol = -1;
		
		if(depth == this.MAX_DEPTH) {
			this.countSoUngCuVien = candidateMoves.size();
		}
		
		if(candidateMoves.size() == 1 && depth == this.MAX_DEPTH) {
			this.countSoNhanhDuyet++;
			return new ValueMove(0, candidateMoves.get(0).row, candidateMoves.get(0).col);
		}
		if (candidateMoves.isEmpty() || depth == 0) {
			score = this.evaluate();
//			if(this.main.whoWon() == this.mySeed) {
//				score += this.scoreMetricPlayer[this.scoreMetricPlayer.length - 1] * depth;
//			}
//			if(this.main.whoWon() == this.mySeed) {
//				score -= this.scoreMetricPlayer[this.scoreMetricPlayer.length - 1] * depth;
//			}
			return new ValueMove(score, bestRow, bestCol);
		} else {
			for (Move move : candidateMoves) {
				this.countSoNhanhDuyet++;
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
						if(depth == this.MAX_DEPTH - 1) {
							System.out.println("Eval cross move: " + bestRow + " - " + bestCol);
							this.main.nextMove = new Move(bestRow, bestCol);
						}
					}
				}
				// undo
				board[move.row][move.col] = Seed.EMPTY;
				if(alpha >= beta) {
					this.countSoLanCatTia++;
					break;
				}
			}
		}
		return new ValueMove((player == this.mySeed) ? alpha : beta, bestRow, bestCol);
	}
}
