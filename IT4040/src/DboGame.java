import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.mysql.cj.protocol.Resultset;

public class DboGame {
	protected final int ROWS = GameMain.ROWS;
	protected final int COLS = GameMain.COLS;
	
	private Seed computerPlayer;
	private GameMain gameMain;
	private int currentMoveNumber;
	private String status;
	private int depth;
	private AIMode aiMode;
	private GetCandidateMode getCandidateMode;
	private boolean isComputerPlayFirst;
	private int moveBoard[][];
	private ArrayList<GameInfo> gameInfo;
	
	private class GameInfo{
		private int move;
		private int soUngCuVien;
		private int soNutTrenCay;
		private int soNutThucTePhaiDuyet;
		private long thoiGianTinhToan;
		
		public GameInfo(int move, int soUngCuVien, int soNutTrenCay, int soNutThucTePhaiDuyet, long thoiGianTinhToan) {
			this.move = move;
			this.soUngCuVien = soUngCuVien;
			this.soNutTrenCay = soNutTrenCay;
			this.soNutThucTePhaiDuyet = soNutThucTePhaiDuyet;
			this.thoiGianTinhToan = thoiGianTinhToan;
		}
	}
	
	public DboGame(GameMain main, AIMode aiMode, GetCandidateMode getCandidateMode, boolean isComputerPlayFirst) {
		this.gameMain = main;
		this.currentMoveNumber = 0;
		this.status = "";
		this.depth = AIPlayer.MAX_DEPTH;
		this.aiMode = aiMode;
		this.getCandidateMode = getCandidateMode;
		this.isComputerPlayFirst = isComputerPlayFirst;
		if(isComputerPlayFirst == true) {
			this.computerPlayer = Seed.CROSS;
		}else {
			this.computerPlayer = Seed.NOUGHT;
		}
		this.moveBoard = new int[this.ROWS][this.COLS];
		this.gameInfo = new ArrayList<GameInfo>();
	}
	
	public void move(int row, int col) {
		this.currentMoveNumber++;
		this.moveBoard[row][col] = this.currentMoveNumber;
	}
	
	public void addInfo(int soUngCuVien, int soNutTrenCay, int soNutThucTePhaiDuyet, long thoiGianTinhToan) {
		this.gameInfo.add(new GameInfo(this.currentMoveNumber, soUngCuVien, soNutTrenCay, soNutThucTePhaiDuyet, thoiGianTinhToan));
	}
	
	public void move(int row, int col, int soUngCuVien, int soNutTrenCay, int soNutThucTePhaiDuyet, long thoiGianTinhToan) {
		this.move(row, col);
		this.gameInfo.add(new GameInfo(this.currentMoveNumber, soUngCuVien, soNutTrenCay, soNutThucTePhaiDuyet, thoiGianTinhToan));
	}
	
	public void setWinner(Seed winner) {
		if(winner == this.computerPlayer) {
			this.status = "WIN";
		}else {
			this.status = "DEFEAT";
		}
	}
	
	public String[] prepareText() {
		//0: aiMode,1: getCandidateMode
		String[] ret = new String[2];
		
	    if(this.aiMode == aiMode.ALPHABETA) {
	    	ret[0] = "ALPHABETA";
	    }else {
	    	ret[0] = "MINIMAX";
	    }
	    
	    if(this.getCandidateMode == GetCandidateMode.AROUND1) {
	    	ret[1] = "AROUND1";
	    }else if(this.getCandidateMode == GetCandidateMode.AROUND2) {
	    	ret[1] = "AROUND2";
	    }else {
	    	ret[1] = "DEFATK";
	    }
	    
		return ret;
	}
	
	public void save() {
		String url = "jdbc:mysql://localhost:3306/IT4040";
		String username = "root";
		String password = "user";

		try (Connection connection = DriverManager.getConnection(url, username, password)) {
		    Statement statement = connection.createStatement();
		    String strAiMode, strGetCandidateMode;
		    String arr[] = this.prepareText();
		    strAiMode = arr[0];
		    strGetCandidateMode = arr[1];
		    
		    String query = "INSERT INTO Game (Board, Depth, Status, AiMode, GetCandidateMode, IsComputerPlayFirst) "
		    		+ "VALUES (?, ?, ?, ?, ?, ?);";		    
		    PreparedStatement preparedStatement = connection.prepareStatement(query);
		    preparedStatement.setString(1, this.getBoardText());
		    preparedStatement.setInt(2, this.depth);
		    
		    preparedStatement.setString(3, this.status);
		    preparedStatement.setString(4, strAiMode);
		    preparedStatement.setString(5, strGetCandidateMode);
		    preparedStatement.setBoolean(6, this.isComputerPlayFirst);
		    preparedStatement.executeUpdate();

		    ResultSet rs = statement.executeQuery("SELECT last_insert_id() FROM Game");
		    rs.next();
		    int id = rs.getInt(1);
//		    System.out.println(i);
		    for(GameInfo g : this.gameInfo) {
		    	query = "INSERT INTO GameInfo (ID, Move, SoUngCuVien, SoNutTrenCayTimKiem, SoNutPhaiDuyet, ThoiGianTinhToan) VALUE (?, ?, ?, ?, ?, ?)";		    
			    preparedStatement = connection.prepareStatement(query);
			    preparedStatement.setInt(1, id);
			    preparedStatement.setInt(2, g.move);
			    preparedStatement.setInt(3, g.soUngCuVien);
			    preparedStatement.setInt(4, g.soNutTrenCay);
			    preparedStatement.setInt(5, g.soNutThucTePhaiDuyet);
			    preparedStatement.setLong(6, g.thoiGianTinhToan);
			    preparedStatement.executeUpdate();

		    }
		    connection.close();
		    
		} catch (SQLException e) {
		    throw new IllegalStateException(e);
		}
	}
	
	public String getBoardText() {
		String str = "";
		
		for(int row = 0; row < ROWS; row++) {
			for(int col = 0; col < COLS; col++) {
				str += String.format("%03d", this.moveBoard[row][col]) +  " ";
			}
			str += "\n";
		}
//		System.out.println(str);
		return str;
	}
}
