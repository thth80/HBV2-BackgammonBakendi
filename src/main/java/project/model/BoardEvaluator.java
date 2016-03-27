package project.model;

public class BoardEvaluator {
	private Board board;
	private int treeLevel, lastActingTeam;
	private static final int EASY = 0, MED = 1, HARD = 2, RAND = 3;
	private BoardEvaluator(Board board)
	{
		this.board = board;
	}

	public static double easyEval(Board board)
	{
		BoardEvaluator eval = new BoardEvaluator(board);
		return eval.startEasyEval();
	}
	public static double mediumEval(Board board)
	{
		BoardEvaluator eval = new BoardEvaluator(board);
		return eval.startEasyEval();
	}
	public static double hardEval(Board board)
	{
		BoardEvaluator eval = new BoardEvaluator(board);
		return eval.startEasyEval();
	}

	private double startEasyEval()
	{
		//Í byrjun ætti borðið að vera athugað og mismunandi leiðir farnar eftir því hvernig phase er í gangi
		//sbr race situation, þegar kallar eru settir á endastöð, ofl
		return Math.random();
	}
}
