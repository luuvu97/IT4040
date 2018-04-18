import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

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
	protected JLabel statusBar; // Status Bar
	protected AIPlayer computerPlayer;

	/** Constructor to setup the game and the GUI components */
	public GameMain() {
		canvas = new DrawCanvas(); // Construct a drawing canvas (a JPanel)
		canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));

		// The canvas (JPanel) fires a MouseEvent upon mouse-click
		canvas.addMouseListener(new MouseAdapter() {
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
						board[rowSelected][colSelected] = currentPlayer; // Make a move
						updateGame(currentPlayer); // update state
						if (currentState != GameState.CROSS_WON) {
							// Switch player
							currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
							lastMove = computerPlayer.move();
							board[lastMove.row][lastMove.col] = currentPlayer; // Make a move
							updateGame(currentPlayer); // update state
							currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
//							System.out.println(getEvalString());
						}
					}
				} else { // game over
					initGame(); // restart the game
				}
				// Refresh the drawing canvas
				repaint(); // Call-back paintComponent().
			}
		});

		// Setup the status bar (JLabel) to display status message
		statusBar = new JLabel("  ");
		statusBar.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 15));
		statusBar.setBorder(BorderFactory.createEmptyBorder(2, 5, 4, 5));

		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(canvas, BorderLayout.CENTER);
		cp.add(statusBar, BorderLayout.PAGE_END); // same as SOUTH

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack(); // pack all the components in this JFrame
		setTitle("Tic Tac Toe");
		setVisible(true); // show this JFrame

		board = new Seed[ROWS][COLS]; // allocate array
		initGame(); // initialize the game board contents and game variables
	}

	/** Initialize the game-board contents and the status */
	public void initGame() {
		for (int row = 0; row < ROWS; ++row) {
			for (int col = 0; col < COLS; ++col) {
				board[row][col] = Seed.EMPTY; // all cells empty
			}
		}
		this.lastMove = new Move();
		currentState = GameState.PLAYING; // ready to play
		currentPlayer = Seed.CROSS; // cross plays first
		this.computerPlayer = new AIPlayerMiniMax(this);
	}

	/**
	 * Update the currentState after the player with "theSeed" has placed on
	 * (rowSelected, colSelected).
	 */
	public void updateGame(Seed theSeed) {
		if (hasWon(theSeed, this.board)) { // check for win
			currentState = (theSeed == Seed.CROSS) ? GameState.CROSS_WON : GameState.NOUGHT_WON;
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
				statusBar.setForeground(Color.BLACK);
				if (currentPlayer == Seed.CROSS) {
					statusBar.setText("X's Turn");
				} else {
					statusBar.setText("O's Turn");
				}
			} else if (currentState == GameState.DRAW) {
				statusBar.setForeground(Color.RED);
				statusBar.setText("It's a Draw! Click to play again.");
			} else if (currentState == GameState.CROSS_WON) {
				statusBar.setForeground(Color.RED);
				statusBar.setText("'X' Won! Click to play again.");
			} else if (currentState == GameState.NOUGHT_WON) {
				statusBar.setForeground(Color.RED);
				statusBar.setText("'O' Won! Click to play again.");
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