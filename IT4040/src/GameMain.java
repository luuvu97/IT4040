import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Tic-Tac-Toe: Two-player Graphics version with Simple-OO
 */
@SuppressWarnings("serial")
public class GameMain extends JFrame {
	// Named-constants for the game board
	public static final int ROWS = 20; // ROWS by COLS cells
	public static final int COLS = 20;
	public static final String crossWonPattern = "xxxxx";
	public static final String noughtWonPattern = "ooooo";

	Move lastMove = new Move();
	// Named-constants of the various dimensions used for graphics drawing
	public static final int CELL_SIZE = 40; // cell width and height (square)
	public static final int CANVAS_WIDTH = CELL_SIZE * COLS; // the drawing canvas
	public static final int CANVAS_HEIGHT = CELL_SIZE * ROWS;
	public static final int GRID_WIDTH = 8; // Grid-line's width
	public static final int GRID_WIDHT_HALF = GRID_WIDTH / 2; // Grid-line's half-width
	// Symbols (cross/nought) are displayed inside a cell, with padding from border
	public static final int CELL_PADDING = CELL_SIZE / 6;
	public static final int SYMBOL_SIZE = CELL_SIZE - CELL_PADDING * 2; // width/height
	public static final int SYMBOL_STROKE_WIDTH = 8; // pen's stroke width

	private GameState currentState; // the current game state

	protected Seed currentPlayer; // the current player

	protected Seed[][] board; // Game board of ROWS-by-COLS cells
	protected DrawCanvas canvas; // Drawing canvas (JPanel) for the game board
	protected JLabel playerTurn;
	protected JButton resignBtn;
	protected JButton newGameBtn;
	protected AIPlayer computerPlayer;
	protected DboGame dboGame;
	protected boolean isComputerPlayFirst;
	protected AIMode aiMode;
	protected GetCandidateMode getCandidateMode;
	protected JComboBox cbGetCandidateMode;
	protected JCheckBox cbComputerPlayFirst;
	protected JRadioButton rdAlphaBeta;
	protected JRadioButton rdMinimax;

	/** Constructor to setup the game and the GUI components */
	public GameMain() {
		Font myFont = new Font(Font.DIALOG_INPUT, Font.BOLD, 18);
		canvas = new DrawCanvas(); // Construct a drawing canvas (a JPanel)
		canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));

		this.resignBtn = new JButton("Resign");
		this.resignBtn.setFont(myFont);
		this.resignBtn.setForeground(Color.WHITE);
		this.resignBtn.setBackground(Color.RED);
		this.resignBtn.setVisible(false);
		this.resignBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.resignBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				getExternData();
				resign();				
			}
		});
		
		this.newGameBtn = new JButton("New Game");
		this.newGameBtn.setFont(myFont);
		this.newGameBtn.setBackground(Color.GREEN);
		this.newGameBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.newGameBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				initGame();	
				repaint();
			}
		});
		
		JPanel navPanel = new JPanel();
		navPanel.setLayout(new BorderLayout());
		
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		JLabel turnLabel = new JLabel();
		turnLabel.setText("Player Turn");
		turnLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		turnLabel.setFont(myFont);
		
		this.playerTurn = new JLabel();
		this.playerTurn.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 32));
		this.playerTurn.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.playerTurn.setText("X");
		
		rightPanel.add(turnLabel);
		rightPanel.add(this.playerTurn);
		rightPanel.add(this.newGameBtn);
		this.newGameBtn.setBorder(new EmptyBorder(5, 5, 5, 5));
		rightPanel.add(this.resignBtn);
		this.resignBtn.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		navPanel.add(rightPanel, BorderLayout.CENTER);
		
		JPanel gameOption = new JPanel();
		gameOption.setLayout(new BoxLayout(gameOption, BoxLayout.Y_AXIS));
		gameOption.setBorder(new TitledBorder("Option"));
		gameOption.setFont(myFont);
		
		this.cbComputerPlayFirst = new JCheckBox("Computer play First");
		this.cbComputerPlayFirst.setFont(myFont);
		this.cbComputerPlayFirst.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		String[] str = {"Around 1", "Def-Atk Score"};
		this.cbGetCandidateMode = new JComboBox(str);
		this.cbGetCandidateMode.setFont(myFont);
		this.cbGetCandidateMode.setSelectedIndex(1);
		this.cbGetCandidateMode.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		JPanel tmpPanel = new JPanel();
		ButtonGroup bgAiMode = new ButtonGroup();
		rdAlphaBeta = new JRadioButton("ALPHA-BETA");
		rdAlphaBeta.setFont(myFont);
		rdAlphaBeta.setSelected(true);

		rdMinimax = new JRadioButton("MINIMAX");
		rdMinimax.setFont(myFont);
		
		bgAiMode.add(rdAlphaBeta);
		bgAiMode.add(rdMinimax);
		tmpPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		tmpPanel.add(rdAlphaBeta);
		tmpPanel.add(rdMinimax);
		
		gameOption.add(tmpPanel);
		gameOption.add(cbGetCandidateMode);
		gameOption.add(this.cbComputerPlayFirst);
		
		navPanel.add(gameOption, BorderLayout.PAGE_END);
		
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(canvas, BorderLayout.CENTER);
		cp.add(navPanel, BorderLayout.EAST);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack(); // pack all the components in this JFrame
		setTitle("Caro");
		setVisible(true); // show this JFrame

		board = new Seed[ROWS][COLS]; // allocate array
	}

	public void play() {
		if(this.isComputerPlayFirst == true) {
			this.board[this.ROWS / 2][this.COLS / 2] = this.currentPlayer;
			updateGame(currentPlayer,  this.ROWS / 2, this.COLS / 2); // update state
			currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
		}
		repaint(); // Call-back paintComponent().
		this.setListener();
	}
	
	public void setListener() {
		// The canvas (JPanel) fires a MouseEvent upon mouse-click
		this.canvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) { // mouse-clicked handler
				int mouseX = e.getX();
				int mouseY = e.getY();
				// Get the row and column clicked
				int rowSelected = mouseY / CELL_SIZE;
				int colSelected = mouseX / CELL_SIZE;

				if (currentState == GameState.PLAYING) {
					if (rowSelected >= 0 && rowSelected < ROWS && colSelected >= 0 && colSelected < COLS
							&& board[rowSelected][colSelected] == Seed.EMPTY) {
						updateGame(currentPlayer,  rowSelected, colSelected); // update state
						if (currentState == GameState.PLAYING) {
							// Switch player
							currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
							lastMove = computerPlayer.move();
							board[lastMove.row][lastMove.col] = currentPlayer; // Make a move
							updateGame(currentPlayer,  lastMove.row, lastMove.col); // update state
							currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
						}
					}
					repaint(); // Call-back paintComponent().
				} 
				// Refresh the drawing canvas
			}
		});

	}
	
	public void getExternData() {
		if(this.cbComputerPlayFirst.isSelected()) {
			this.isComputerPlayFirst = true;
		}else {
			this.isComputerPlayFirst = false;
		}
		
		if(this.rdAlphaBeta.isSelected()) {
			this.aiMode = AIMode.ALPHABETA;
		}else {
			this.aiMode = AIMode.MINIMAX;
		}
		
		String[] str = {"Around 1", "Def-Atk Score"};
		int index = this.cbGetCandidateMode.getSelectedIndex();
		if(index == 0) {
			this.getCandidateMode = GetCandidateMode.AROUND1;
			AIPlayer.MAX_DEPTH = 3;
		}else {
			this.getCandidateMode = GetCandidateMode.DEFATK;
			AIPlayer.MAX_DEPTH = 5;
		}
	}
	
	/** Initialize the game-board contents and the status */
	public void initGame() {
		for (int row = 0; row < ROWS; ++row) {
			for (int col = 0; col < COLS; ++col) {
				board[row][col] = Seed.EMPTY; // all cells empty
			}
		}
		this.newGameBtn.setVisible(false);
		this.resignBtn.setVisible(true);
		this.lastMove = new Move();
		this.playerTurn.setText("X");
		this.currentState = GameState.PLAYING; // ready to play
		this.currentPlayer = Seed.CROSS; // cross plays first
		this.getExternData();
		this.dboGame = new DboGame(this, this.aiMode, this.getCandidateMode, this.isComputerPlayFirst);
		this.play();
		Seed computer = (this.isComputerPlayFirst ? Seed.CROSS : Seed.NOUGHT);
		this.computerPlayer = new AIPlayerMiniMax(this, computer);
	}

	/**
	 * Update the currentState after the player with "theSeed" has placed on
	 * (rowSelected, colSelected).
	 */
	public void updateGame(Seed theSeed, int row, int col) {
		this.board[row][col] = theSeed;
		this.dboGame.move(row, col);
		if (hasWon(theSeed, this.board)) { // check for win
			currentState =(theSeed == Seed.CROSS) ? GameState.CROSS_WON : GameState.NOUGHT_WON;
			this.newGameBtn.setVisible(true);
			this.resignBtn.setVisible(false);
			this.dboGame.setWinner(theSeed);
			this.dboGame.save();
		} else if (isDraw()) { // check for draw
			currentState = GameState.DRAW;
		}
		// Otherwise, no change to current state (still GameState.PLAYING).
	}

	/** Return true if it is a draw (i.e., no more empty cell) */
	public boolean isDraw() {
		for (int row = 0; row < ROWS; ++row) {
			for (int col = 0; col < COLS; ++col) {
				if (board[row][col] == Seed.EMPTY) {
					return false; // an empty cell found, not draw, exit
				}
			}
		}
		return true; // no more empty cell, it's a draw
	}

	/**
	 * Return true if the player with "theSeed" has won after placing at
	 * (rowSelected, colSelected)
	 */
	public boolean hasWon(Seed theSeed, Seed[][] board) {
		String str = this.getEvalString();
		String pattern = null;
		if (theSeed == Seed.CROSS) {
			pattern = this.crossWonPattern;
		} else {
			pattern = this.noughtWonPattern;
		}
		return str.contains(pattern);
	}
	
	public Seed whoWon() {
		String str = this.getEvalString();
		if(str.contains(this.crossWonPattern)) {
			return Seed.CROSS;
		}
		if(str.contains(this.noughtWonPattern)) {
			return Seed.NOUGHT;
		}
		return Seed.EMPTY;
	}
	
	public void resign() {
//		this.dboGame.save();
		this.currentState = GameState.NOUGHT_WON;
		this.newGameBtn.setVisible(true);
		this.repaint();
	}

	/**
	 * Inner class DrawCanvas (extends JPanel) used for custom graphics drawing.
	 */
	class DrawCanvas extends JPanel {
		@Override
		public void paintComponent(Graphics g) { // invoke via repaint()
			super.paintComponent(g); // fill background
			setBackground(Color.WHITE); // set its background color

			// Draw the grid-lines
			g.setColor(Color.LIGHT_GRAY);
			for (int row = 1; row < ROWS; ++row) {
				g.fillRoundRect(0, CELL_SIZE * row - GRID_WIDHT_HALF, CANVAS_WIDTH - 1, GRID_WIDTH, GRID_WIDTH,
						GRID_WIDTH);
			}
			for (int col = 1; col < COLS; ++col) {
				g.fillRoundRect(CELL_SIZE * col - GRID_WIDHT_HALF, 0, GRID_WIDTH, CANVAS_HEIGHT - 1, GRID_WIDTH,
						GRID_WIDTH);
			}

			// Draw the Seeds of all the cells if they are not empty
			// Use Graphics2D which allows us to set the pen's stroke
			Graphics2D g2d = (Graphics2D) g;
			g2d.setStroke(new BasicStroke(SYMBOL_STROKE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); // Graphics2D
																												// only
			for (int row = 0; row < ROWS; ++row) {
				for (int col = 0; col < COLS; ++col) {
					int x1 = col * CELL_SIZE + CELL_PADDING;
					int y1 = row * CELL_SIZE + CELL_PADDING;
					if(row == lastMove.row && col == lastMove.col) {
						g.setColor(Color.GREEN);
						g.fillRect(x1 - CELL_PADDING, y1 - CELL_PADDING, CELL_SIZE, CELL_SIZE);
					}
					if (board[row][col] == Seed.CROSS) {
						g2d.setColor(Color.RED);
						int x2 = (col + 1) * CELL_SIZE - CELL_PADDING;
						int y2 = (row + 1) * CELL_SIZE - CELL_PADDING;
						g2d.drawLine(x1, y1, x2, y2);
						g2d.drawLine(x2, y1, x1, y2);
					} else if (board[row][col] == Seed.NOUGHT) {
						g2d.setColor(Color.BLUE);
						g2d.drawOval(x1, y1, SYMBOL_SIZE, SYMBOL_SIZE);
					}
				}
			}

			// Print status-bar message
			if (currentState == GameState.PLAYING) {
				if (currentPlayer == Seed.CROSS) {
					playerTurn.setForeground(Color.RED);
					playerTurn.setText("X");
				} else {
					playerTurn.setForeground(Color.BLUE);
					playerTurn.setText("O");
				}
			} else if (currentState == GameState.DRAW) {
				playerTurn.setForeground(Color.GREEN);
				playerTurn.setText("It's a Draw! Click to play again.");
			} else if (currentState == GameState.CROSS_WON) {
				playerTurn.setForeground(Color.RED);
				playerTurn.setText("X WON");
			} else if (currentState == GameState.NOUGHT_WON) {
				playerTurn.setForeground(Color.BLUE);
				playerTurn.setText("O WON");
			}
		}
	}

	public String getSeedChar(Seed seed) {
		if (seed == Seed.CROSS) {
			return "x";
		} else if (seed == Seed.NOUGHT) {
			return "o";
		}
		return " ";
	}

	public String getEvalString() {
		
		String str = "";
		// for each row
		for (int row = 0; row < this.ROWS; row++) {
			for (int col = 0; col < this.COLS; col++) {
				str += this.getSeedChar(board[row][col]);
			}
			str += ";";
		}
//		str += "\n";
		// for each col
		for (int col = 0; col < this.COLS; col++) {
			for (int row = 0; row < this.ROWS; row++) {
				str += this.getSeedChar(board[row][col]);
			}
			str += ";";
		}
//		str += "\n";
		// for each Diagonal (left -> right)
		// co cac duong cheo (trai -> phai) bat dau tu 0-0; 0-1; ...; 0-(COLS-5) && 0-0,
		// 1-0, 2-0,..., (ROWS-5)-0
		for (int row = 0; row < this.ROWS - 4; row++) {
			int r = row;
			int c = 0;
			while (r < this.ROWS && c < this.COLS) {
				str += this.getSeedChar(board[r++][c++]);
			}
//			str += ";";
		}
		// 0-0 da duoc tinh
			for (int col = 1; col < this.COLS - 4; col++) {
				int r = 0;
				int c = col;
				while (r < this.ROWS && c < this.COLS) {
					str += this.getSeedChar(board[r++][c++]);
				}
				str += ";";
			}
//		str += "\n";
		// for each oppDiagonal (right -> left)
		// co cac duong cheo bat dau tu 0-(COLS -1), 0-(COLS-2),..., 0-4 && 0-(COLS -
		// 1), 1-(COLS-1),..., (ROWS -5)-(COLS-1)
		for (int row = 0; row < this.ROWS - 4; row++) {
			int r = row;
			int c = this.COLS - 1;
			while (r < this.ROWS && c >= 0) {
				str += this.getSeedChar(board[r++][c--]);
			}
			str += ";";
		}
		// 0-(COLS-1) da duoc tinh
			for (int col = 4; col < this.COLS - 1; col++) {
				int r = 0;
				int c = col;
				while (r < this.ROWS && c >= 0) {
					str += this.getSeedChar(board[r++][c--]);
				}
				str += ";";
			}
//		str += "\n\n";
		return str;
	}

	/** The entry main() method */
	public static void main(String[] args) {
		// Run GUI codes in the Event-Dispatching thread for thread safety
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new GameMain(); // Let the constructor do the job
			}
		});
	}
}