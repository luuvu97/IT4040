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
	protected static int MAX_DEPTH = 5;
	protected AIMode aiMode;
	protected GetCandidateMode getCandidateMode;
	protected int countTongSoNut = 0;	//Tong so nut tren cay tim kiem
	protected int countSoLanCatTia = 0;	//so lan cat tia
	protected int countSoNhanhDuyet = 0;	//so nut thuc te phai duyet qua tren cay tim kiem
	protected int countSoUngCuVien = 0;	//so UCV o nuoc di dau tien
	public long time = 0;

	public int defScore[] = { 0, 1, 10, 100, 1000 };
	public int atkScore[] = { 0, 2, 20, 110, 4000 };

	protected int[][] defAtkScore;

	protected Seed[][] board;
	protected Seed mySeed;
	protected Seed oppSeed;
	protected GameMain main;

	public String[] pattern1 = new String[] { 
			"xxxxx",
			"-xxxx-",
			"oxxxx-", "oxx-xx", "-xxxxo", "xx-xxo",
			"x-xxx", "xxx-x",
			"xx-xx",
			"-xxx--", "-x-xx-", "-xx-x-", "--xxx-",
			"oxxx--", "oxx-x-", "ox-xx-", "--xxxo", "-x-xxo", "-xx-xo", "o-xxx-o",
			"-xx--", "--xx-", "-x-x-", "-x--x-",
			"oxx---", "ox-x--", "ox--x-", "---xxo", "--x-xo", "-x--xo",
		};

		public final int[] scoreMetricPlayer1 = new int[] { 
			100000,
			10000,
			1200, 1200, 1200, 1200,
			1500, 1500,
			1300,
			2000, 2000, 2000, 2000,
			100, 100, 100, 100, 100, 100, 100,
			100, 100, 100, 100,
			10, 10, 10, 10, 10, 10,
		};
		
		public final int[] scoreMetricOpponent1 = new int[] { 
			100000,
			10000,
			1200, 1200, 1200, 1200,
			1500, 1500,
			1300,
			2000, 2000, 2000, 2000,
			100, 100, 100, 100, 100, 100, 100,
			100, 100, 100, 100,
			10, 10, 10, 10, 10, 10,
		};
		
		
		public String[] pattern2 = new String[] { 
				"-xx-", "-xxxo", "oxxx-", "-xxx-", "-x-xx-", "-xx-x-", "xx-xx", "-xxxxo", "oxxxx-",
					"-xxxx-", "xxxxx"
			};

			public final int[] scoreMetricPlayer2 = new int[] { 20, 30, 30, 50, 50, 50, 100, 100, 1000, 1000, 5000 };
			public final int[] scoreMetricOpponent2 = new int[] { 20, 30, 30, 50, 50, 50, 100, 100, 1000, 1000, 5000 };
		
		public String[] pattern = pattern1;
		public int[] scoreMetricPlayer = this.scoreMetricPlayer1;
		public int[] scoreMetricOpponent = this.scoreMetricOpponent1;

	
	public AIPlayer(GameMain main, Seed computer) {
		this.board = main.board;
		this.mySeed = computer;
		this.oppSeed = (computer == Seed.CROSS ? Seed.NOUGHT : Seed.CROSS);
		this.main = main;
		this.aiMode = this.main.aiMode;
		this.getCandidateMode = this.main.getCandidateMode;
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
							if (thePlayer == Seed.NOUGHT) {
								//luot O choi. co numNought O => cong
								this.defAtkScore[row][col + i] += this.atkScore[numNought];
							} else {
								//luot X choi. co numNought O => thu
								this.defAtkScore[row][col + i] += this.defScore[numNought];
							}
						} else if (numCross != 0 && numNought == 0) { // computer = 0
							if (thePlayer == Seed.NOUGHT) {
								//luot O choi. co numCross X => thu
								this.defAtkScore[row][col + i] += this.defScore[numCross];
							} else {
								//luot X choi. co Cross X => cong
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
							if (thePlayer == Seed.NOUGHT) {
								//luot O choi. co numNought O => cong
								this.defAtkScore[row + i][col] += this.atkScore[numNought];
							} else {
								//luot X choi. co numNought O => thu
								this.defAtkScore[row + i][col] += this.defScore[numNought];
							}
						} else if (numCross != 0 && numNought == 0) { // computer = 0
							if (thePlayer == Seed.NOUGHT) {
								//luot O choi. co numCross X => thu
								this.defAtkScore[row + i][col] += this.defScore[numCross];
							} else {
								//luot X choi. co Cross X => cong
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
							if (thePlayer == Seed.NOUGHT) {
								//luot O choi. co numNought O => cong
								this.defAtkScore[row + i][col + i] += this.atkScore[numNought];
							} else {
								//luot X choi. co numNought O => thu
								this.defAtkScore[row + i][col + i] += this.defScore[numNought];
							}
						} else if (numCross != 0 && numNought == 0) { // computer = 0
							if (thePlayer == Seed.NOUGHT) {
								//luot O choi. co numCross X => thu
								this.defAtkScore[row + i][col + i] += this.defScore[numCross];
							} else {
								//luot X choi. co Cross X => cong
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
							if (thePlayer == Seed.NOUGHT) {
								//luot O choi. co numNought O => cong
								this.defAtkScore[row + i][col - i] += this.atkScore[numNought];
							} else {
								//luot X choi. co numNought O => thu
								this.defAtkScore[row + i][col - i] += this.defScore[numNought];
							}
						} else if (numCross != 0 && numNought == 0) { // computer = 0
							if (thePlayer == Seed.NOUGHT) {
								//luot O choi. co numCross X => thu
								this.defAtkScore[row + i][col - i] += this.defScore[numCross];
							} else {
								//luot X choi. co Cross X => cong
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
		
		int prevMax = Integer.MIN_VALUE;
		int max = Integer.MIN_VALUE;
		for (int i = 0; i < valMove.length - 1; i++) {
			moves.add(new Move(valMove[i].move.row, valMove[i].move.col));
			if (valMove[i].score <= this.defScore[3]) {
				break;
			}
		}
		
		return moves;
	}

	public List<Move> getCandidateMoves1(Seed thePlayer) {
		List<Move> moves = new ArrayList<Move>();
		if (this.main.hasWon(mySeed, board) || this.main.hasWon(oppSeed, board) || this.main.isDraw()) {
			return moves;
		}
		this.getDefAtkScore(thePlayer);
		
		int max = Integer.MIN_VALUE;
		//find cell of cells have maximum score
		for(int row = 0; row < this.ROWS; row++) {
			for(int col = 0; col < this.COLS; col++) {
				if(this.defAtkScore[row][col] > max && this.board[row][col] == Seed.EMPTY) {
					max = this.defAtkScore[row][col];
					moves.clear();
					moves.add(new Move(row, col));
				}else if(this.defAtkScore[row][col] == max) {
					moves.add(new Move(row, col));
				}
			}
		}
		
		Move m = moves.get(0);
		//neu cell co diem cao nhat nay da co 4 o lien tiep (can danh ngay lap tuc) => lay ucv. khong can tim ucv nua
		if(this.defAtkScore[m.row][m.col] >= this.defScore[this.defScore.length - 1]) {
			return moves;
		}else {
			//khong co 4 o lien tiep nao => lua chon trong nhieu o khac
			//chon 4 diem cao nhat (distinct) co the nhieu o cung 1 muc diem
			//neu khong co 3 o lien tiep nao thi chi can chon 1 o duy nhat
			moves.clear();
			int[][] tempScoreBoard =  new int[this.ROWS][this.COLS];
			for(int row = 0; row < this.ROWS; row++) {
				tempScoreBoard[row] = Arrays.copyOf(this.defAtkScore[row], this.COLS);
			}
			ArrayList<Move> tempList = new ArrayList<Move>();
			for(int i = 0; i < 4; i++) {
				max = Integer.MIN_VALUE;
				for(int row = 0; row < this.ROWS; row++) {
					for(int col = 0; col < this.COLS; col++) {
						if(tempScoreBoard[row][col] > max && this.board[row][col] == Seed.EMPTY) {
							max = tempScoreBoard[row][col];
							tempList.clear();
							tempList.add(new Move(row, col));
						}else if(tempScoreBoard[row][col] == max && this.board[row][col] == Seed.EMPTY) {
							tempList.add(new Move(row, col));
						}
					}
				}
//				if(tempList.size() > 1) {
//					i--;
//				}
				if(max >= this.atkScore[2]) {
					for(Move move : tempList) {
						moves.add(move);
						tempScoreBoard[move.row][move.col] = 0;
					}
				}else {
					moves.add(tempList.get(0));
					break;
				}

				tempList.clear();
			}
			return moves;
		}
	}
	
	public List<Move> getCandidateAround(){
		int radius = 1;
		List<Move> moves = new ArrayList<Move>();
		if (this.main.hasWon(mySeed, board) || this.main.hasWon(oppSeed, board)) {
			return moves;
		}
		
		int rowStart = this.ROWS - 1, rowEnd = 0, colStart = this.COLS - 1, colEnd = 0;
		
		//xac dinh kich thuoc khu co dang danh
		for(int row = 0; row < this.ROWS; row++) {
			for(int col = 0; col < this.COLS; col++) {
				if(this.board[row][col] != Seed.EMPTY) {
					if(row < rowStart) {
						rowStart = row;
					}
					if(row > rowEnd) {
						rowEnd = row;
					}
					if(col < colStart) {
						colStart = col;
					}
					if(col > colEnd) {
						colEnd = col;
					}
				}
			}
		}
		
		//duyet trong ban kinh 1
		int rowOffset = 0, colOffset = 0;
		if(rowStart != 0) {
			rowOffset = -1;
		}
		if(colStart != 0) {
			colOffset = -1;
		}
		
//		System.out.println("Size: row: " + (rowStart + rowOffset) + " -> " + (rowEnd + 1) + "; col: " + (colStart + colOffset) + " -> " + (colEnd + 1));
		
		for(int i = rowStart + rowOffset; i <= rowEnd + 1 && i < this.ROWS; i++) {
			for(int j = colStart + colOffset; j <= colEnd + 1 && j < this.COLS; j++) {
				if(this.board[i][j] == Seed.EMPTY) {
					moves.add(new Move(i, j));
				}
			}
		}
		
		return moves;
	}
	
	public List<Move> getCandidateAround1(){
		List<Move> moves = new ArrayList<Move>();
		if (this.main.hasWon(mySeed, board) || this.main.hasWon(oppSeed, board)) {
			return moves;
		}
		
		int rowOffset = 0;
		int colOffset = 0;
		boolean mark[][] = new boolean[this.ROWS][this.COLS];
		//xac dinh kich thuoc khu co dang danh
		for(int row = 0; row < this.ROWS; row++) {
			for(int col = 0; col < this.COLS; col++) {
				if(this.board[row][col] != Seed.EMPTY) {
					rowOffset = 0; colOffset = 0;
					if(row != 0) {
						rowOffset = -1;
					}
					if(col != 0) {
						colOffset = -1;
					}
					
					for(int i = row + rowOffset; i <= row + 1 && i < this.ROWS; i++) {
						for(int j = col + colOffset; j <= col + 1 && j < this.COLS; j++) {
//							System.out.println(i + " - " + j);
							if(this.board[i][j] == Seed.EMPTY && mark[i][j] == false) {
								moves.add(new Move(i, j));
								mark[i][j] = true;
 							}
						}
					}
				}
			}
		}
		return moves;
	}
	
	public List<Move> getCandidateMoves2(Seed thePlayer) {
		List<Move> moves = new ArrayList<Move>();
		if (this.main.hasWon(mySeed, board) || this.main.hasWon(oppSeed, board) || this.main.isDraw()) {
			return moves;
		}
		this.getDefAtkScore(thePlayer);
		
		int max = Integer.MIN_VALUE;
		//find cell of cells have maximum score
		for(int row = 0; row < this.ROWS; row++) {
			for(int col = 0; col < this.COLS; col++) {
				if(this.defAtkScore[row][col] > max && this.board[row][col] == Seed.EMPTY) {
					max = this.defAtkScore[row][col];
					moves.clear();
					moves.add(new Move(row, col));
				}else if(this.defAtkScore[row][col] == max) {
					moves.add(new Move(row, col));
				}
			}
		}
		
		Move m = moves.get(0);
		return moves;
	}
	
	
	public List<Move> getCandidate(Seed thePlayer){
		if(this.getCandidateMode == GetCandidateMode.AROUND1) {
			return this.getCandidateAround1();
		}else {
			return this.getCandidateMoves1(thePlayer);
		}
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
		
//		System.out.println("\n-------------------\nThe Def Atk Score:\n");
		this.getDefAtkScore(thePlayer);
		for(int i = 0; i < this.ROWS; i++) {
			for(int j = 0; j < this.COLS; j++) {
				System.out.print(this.defAtkScore[i][j] + "\t");
			}
			System.out.println();
		}
		
		System.out.println("\n-------------------\nCandidate:\n");
		List<Move> list = this.getCandidate(thePlayer);
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
				String tmp = this.parsePattern(i, thePlayer);
				countPlayer = this.numOfContain(str, tmp);
				tmp = this.parsePattern(i, theOppPlayer);
				countOppPlayer = this.numOfContain(str, tmp);
				scorePlayer += countPlayer * this.scoreMetricPlayer[i];
				scoreOpp += countOppPlayer * this.scoreMetricOpponent[i];
			}
		}

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
				String tmp = this.parsePattern(i, this.mySeed);
				countPlayer = this.numOfContain(str, tmp);
				tmp = this.parsePattern(i, this.oppSeed);
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
