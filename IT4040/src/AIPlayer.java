import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

//Abstract superclass for all AI players with different strategies
//To construct an AI player:
//	1. construct an instace (of its subclass) with the game Board
//	2. Call setSeed() to set the computer's seed
//	3. Call move() which returns the next move in an int[2] array of () {row, col}
//
//the implementation subclassees need to override method move()
//they shall not modify Cell[][], no side effect expected
//Assume that next move is available, not game-over yet

public abstract class AIPlayer {
	protected int ROWS = GameMain.ROWS;
	protected int COLS = GameMain.COLS;
	protected final int MAX_DEPTH = 6;
	public static int count = 0;

	public int defScore[] = { 0, 1, 10, 100, 1000 };
	public int atkScore[] = { 0, 4, 40, 400, 4000 };

	protected int[][] defAtkScore;

	protected Seed[][] board;
	protected Seed mySeed;
	protected Seed oppSeed;
	protected GameMain main;

	public String[] pattern = new String[] { "-xx-", "-xxxo", "oxxx-", "-xxx-", "-x-xx-", "-xx-x-", "-xxxxo", "oxxxx-",
			"-xxxx-", "xxxxx" };

	public final int[] scoreMetricPlayer = new int[] { 20, 30, 30, 50, 50, 50, 100, 100, 1000, 5000 };
	public final int[] scoreMetricOpponent = new int[] { 20, 30, 30, 50, 50, 50, 100, 100, 1000, 5000 };

	public AIPlayer(GameMain main) {
		this.board = main.board;
		this.mySeed = Seed.NOUGHT;
		this.oppSeed = Seed.CROSS;
		this.main = main;
	}

	public String parsePattern(int index, Seed thePlayer) {
		String str = this.pattern[index].replace("-", " ");
		if (thePlayer == Seed.NOUGHT) {
			str = str.replace("o", "t");
			str = str.replace("x", "o");
			str = str.replace("t", "x");
		}
		return str;
	}

	public void getDefAtkScore(Seed thePlayer) {
		this.defAtkScore = new int[this.ROWS][this.COLS];
		int numCross = 0, numNought = 0;

		// for each row
		for (int row = 0; row < this.ROWS; row++) {
			for (int col = 0; col < this.COLS - 4; col++) {
				numNought = 0;
				numCross = 0;
				for (int i = 0; i < 5; i++) {
					if (this.board[row][col + i] == Seed.NOUGHT) {
						numNought++;
					}
					if (this.board[row][col + i] == Seed.CROSS) {
						numCross++;
					}
				}
				for (int i = 0; i < 5; i++) {
					if (this.board[row][col + i] == Seed.EMPTY) {
						if (numCross == 0 && numNought != 0) {
							if (thePlayer == this.mySeed) {
								//luot may choi. co numNought O => cong
								this.defAtkScore[row][col + i] += this.atkScore[numNought];
							} else {
								//luot nguoi choi. co numNought O => thu
								this.defAtkScore[row][col + i] += this.defScore[numNought];
							}
						} else if (numCross != 0 && numNought == 0) { // computer = 0
							if (thePlayer == this.mySeed) {
								//luot may choi. co numCross X => thu
								this.defAtkScore[row][col + i] += this.defScore[numCross];
							} else {
								//luot nguoi choi. co Cross X => cong
								this.defAtkScore[row][col + i] += this.atkScore[numCross];
							}
						}
					}
				}
			}
		}

		// for each col
		for (int col = 0; col < this.COLS; col++) {
			for (int row = 0; row < this.ROWS - 4; row++) {
				numNought = 0;
				numCross = 0;
				for (int i = 0; i < 5; i++) {
					if (this.board[row + i][col] == Seed.NOUGHT) {
						numNought++;
					}
					if (this.board[row + i][col] == Seed.CROSS) {
						numCross++;
					}
				}
				for (int i = 0; i < 5; i++) {
					if (this.board[row + i][col] == Seed.EMPTY) {
						if (numCross == 0 && numNought != 0) {
							if (thePlayer == this.mySeed) {
								//luot may choi. co numNought O => cong
								this.defAtkScore[row + i][col] += this.atkScore[numNought];
							} else {
								//luot nguoi choi. co numNought O => thu
								this.defAtkScore[row + i][col] += this.defScore[numNought];
							}
						} else if (numCross != 0 && numNought == 0) { // computer = 0
							if (thePlayer == this.mySeed) {
								//luot may choi. co numCross X => thu
								this.defAtkScore[row + i][col] += this.defScore[numCross];
							} else {
								//luot nguoi choi. co Cross X => cong
								this.defAtkScore[row + i][col] += this.atkScore[numCross];
							}
						}
					}
				}
			}

		}
		// for each Diagonal (left -> right)
		// co cac duong cheo (trai -> phai) bat dau tu 0-0; 0-1; ...; 0-(COLS-5) && 0-0,
		// 1-0, 2-0,..., (ROWS-5)-0

		for (int row = 0; row < this.ROWS - 4; row++) {
			for (int col = 0; col < this.COLS - 4; col++) {
				numNought = 0;
				numCross = 0;
				for (int i = 0; i < 5; i++) {
					if (this.board[row + i][col + i] == Seed.NOUGHT) {
						numNought++;
					}
					if (this.board[row + i][col + i] == Seed.CROSS) {
						numCross++;
					}

				}
				for (int i = 0; i < 5; i++) {
					if (this.board[row + i][col + i] == Seed.EMPTY) {
						if (numCross == 0 && numNought != 0) {
							if (thePlayer == this.mySeed) {
								//luot may choi. co numNought O => cong
								this.defAtkScore[row + i][col + i] += this.atkScore[numNought];
							} else {
								//luot nguoi choi. co numNought O => thu
								this.defAtkScore[row + i][col + i] += this.defScore[numNought];
							}
						} else if (numCross != 0 && numNought == 0) { // computer = 0
							if (thePlayer == this.mySeed) {
								//luot may choi. co numCross X => thu
								this.defAtkScore[row + i][col + i] += this.defScore[numCross];
							} else {
								//luot nguoi choi. co Cross X => cong
								this.defAtkScore[row + i][col + i] += this.atkScore[numCross];
							}
						}
					}
				}
			}
		}

		// for each opponent Diagonal (right -> left)
		for (int row = 0; row < this.ROWS - 4; row++) {
			for (int col = 4; col < this.COLS; col++) {
				numNought = 0;
				numCross = 0;
				for (int i = 0; i < 5; i++) {
					if (this.board[row + i][col - i] == Seed.NOUGHT) {
						numNought++;
					}
					if (this.board[row + i][col - i] == Seed.CROSS) {
						numCross++;
					}
				}
				for (int i = 0; i < 5; i++) {
					if (this.board[row + i][col - i] == Seed.EMPTY) {
						if (numCross == 0 && numNought != 0) {
							if (thePlayer == this.mySeed) {
								//luot may choi. co numNought O => cong
								this.defAtkScore[row + i][col - i] += this.atkScore[numNought];
							} else {
								//luot nguoi choi. co numNought O => thu
								this.defAtkScore[row + i][col - i] += this.defScore[numNought];
							}
						} else if (numCross != 0 && numNought == 0) { // computer = 0
							if (thePlayer == this.mySeed) {
								//luot may choi. co numCross X => thu
								this.defAtkScore[row + i][col - i] += this.defScore[numCross];
							} else {
								//luot nguoi choi. co Cross X => cong
								this.defAtkScore[row + i][col - i] += this.atkScore[numCross];
							}
						}
					}
				}
			}
		}
	}

	public List<Move> getCandidateMoves(Seed thePlayer) {
		List<Move> moves = new ArrayList<Move>();
		if (this.main.hasWon(mySeed, board) || this.main.hasWon(oppSeed, board)) {
			return moves;
		}
		this.getDefAtkScore(thePlayer);

		ValueMove[] valMove = new ValueMove[this.ROWS * this.COLS];
		for (int row = 0; row < this.ROWS; row++) {
			for (int col = 0; col < this.COLS; col++) {
				valMove[row * this.COLS + col] = new ValueMove(this.defAtkScore[row][col], row, col);
			}
		}

		// Sort array decending
		Arrays.sort(valMove, new Comparator<ValueMove>() {
			@Override
			public int compare(ValueMove o1, ValueMove o2) {
				// Sort array decending
				return Integer.compare(o2.score, o1.score);
			}
		});

		// for(ValueMove vm : valMove) {
		// System.out.println(vm.score);
		// }

		int max = Integer.MIN_VALUE;
		for (int i = 0; i < valMove.length - 1; i++) {
			if (valMove[i].score == valMove[i + 1].score) {
				moves.add(new Move(valMove[i].move.row, valMove[i].move.col));
			} else {
				moves.add(new Move(valMove[i].move.row, valMove[i].move.col));
				if (moves.size() > 4) {
					break;
				}
			}
		}

		// for(Move m : moves) {
		// System.out.println(m.row + " - " + m.col + " : " +
		// this.defAtkScore[m.row][m.col]);
		// }
		// System.out.println("\n\n");

		return moves;
	}

	public List<Move> getCandidateMoves1(Seed thePlayer) {
		List<Move> moves = new ArrayList<Move>();
		if (this.main.hasWon(mySeed, board) || this.main.hasWon(oppSeed, board) || this.main.isDraw()) {
			return moves;
		}
		this.getDefAtkScore(thePlayer);

		for(int i = 0; i < this.ROWS; i++) {
			for(int j = 0; j < this.COLS; j++) {
				if(this.defAtkScore[i][j] > 100) {
					moves.add(new Move(i, j));
				}
			}
		}
		
		int max = Integer.MIN_VALUE;
		int row = -1;
		int col = -1;
		if(moves.isEmpty()) {
			for(int i = 0; i < this.ROWS; i++) {
				for(int j = 0; j < this.COLS; j++) {
					if(this.defAtkScore[i][j] > max && this.board[i][j] == Seed.EMPTY) {
						max = this.defAtkScore[i][j];
						row = i;
						col = j;
					}
				}
			}
			moves.add(new Move(row, col));
		}

		return moves;
	}

	public abstract Move move();

	public void showBoardInfo(Seed thePlayer) {
		System.out.println("The Player: " + this.main.getSeedChar(thePlayer));
		System.out.println("\n-------------------\nThe Board:\n");

		for(int i = 0; i < this.ROWS; i++) {
			for(int j = 0; j < this.COLS; j++) {
				String str = this.main.getSeedChar(this.board[i][j]);
				System.out.print(str + "\t");
			}
			System.out.println();
		}
		System.out.println("\n-------------------\nThe Def Atk Score:\n");
		this.getDefAtkScore(thePlayer);
		for(int i = 0; i < this.ROWS; i++) {
			for(int j = 0; j < this.COLS; j++) {
				System.out.print(this.defAtkScore[i][j] + "\t");
			}
			System.out.println();
		}
		
		System.out.println("\n-------------------\nCandidate:\n");
		List<Move> list = this.getCandidateMoves(thePlayer);
		for(Move m : list) {
			System.out.println(m.toString() + "\t: " + this.defAtkScore[m.row][m.col]);
		}
	}
	
	public int evaluate(Seed thePlayer) {
		// use a string for eval
		Seed theOppPlayer = (thePlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
		int score = 0;
		String evalString = main.getEvalString();
		String[] arr = evalString.split(";");
		int scorePlayer = 0;
		int scoreOpp = 0;
		int countPlayer, countOppPlayer;
		for (String str : arr) {
			for (int i = 0; i < this.pattern.length; i++) {
				String tmp = this.parsePattern(i, Seed.CROSS);
				countPlayer = this.numOfContain(str, tmp);
				tmp = this.parsePattern(i, Seed.NOUGHT);
				countOppPlayer = this.numOfContain(str, tmp);
				scorePlayer += countPlayer * this.scoreMetricPlayer[i];
				scoreOpp += countOppPlayer * this.scoreMetricOpponent[i];
			}
		}
//		if (thePlayer == this.mySeed) {
//			return score;
//		} else {
//			return -score;
//		
//		}
		System.out.println("\n***********\nThe Score: " + (scoreOpp - scorePlayer));
		this.showBoardInfo(thePlayer);
		return scorePlayer - scoreOpp;
	}
	
	public int evaluate() {
		// use a string for eval
		int score = 0;
		String evalString = main.getEvalString();
		String[] arr = evalString.split(";");
		int scorePlayer = 0;
		int scoreOpp = 0;
		int countPlayer, countOppPlayer;
		for (String str : arr) {
			for (int i = 0; i < this.pattern.length; i++) {
				String tmp = this.parsePattern(i, Seed.NOUGHT);
				countPlayer = this.numOfContain(str, tmp);
				tmp = this.parsePattern(i, Seed.CROSS);
				countOppPlayer = this.numOfContain(str, tmp);
				scorePlayer += countPlayer * this.scoreMetricPlayer[i];
				scoreOpp += countOppPlayer * this.scoreMetricOpponent[i];
			}
		}
//		System.out.println("\n***********\nThe Score: " + (scoreOpp - scorePlayer));
//		this.showBoardInfo(mySeed);
		return scorePlayer - scoreOpp;
	}

	public int numOfContain(String src, String str) {
		int count = 0;
		int index = -str.length();
		while ((index = src.indexOf(str, index + str.length())) != -1) {
			count++;
		}
		return count;
	}
}
