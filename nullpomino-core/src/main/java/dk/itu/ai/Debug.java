package dk.itu.ai;

import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.play.GameEngine;

/**
 * A class with various debugging functions such as printing the stage to the console.
 * 
 * @author OliverPhillip
 */
public class Debug 
{
	private static int debugWidth = 14;
	private static int debugHeight = 24;
	private static int debugOffsetX = 2;
	private static int debugOffsetY = 2;
	
	private static char[][] debugField = new char[debugHeight][debugWidth];
	
	/**
	 * Reset the internal stage representation.
	 * 
	 * @param width The stage width.
	 * @param height The stage height.
	 */
	private static void resetDebugStage(int width, int height) 
	{
		for(int y = 0; y < debugHeight; ++y)
		{
			char[] line = debugField[y];
			
			for(int x = 0; x < debugWidth; ++x)
			{
				if((y < debugOffsetY)
					|| (y >= debugOffsetY + height)
					|| (x < debugOffsetX)
					|| (x >= debugOffsetX + width))
				{
					line[x] = (char)9618; //'#';
				}
				else
				{
					line[x] = (char)9617;//' z';
				}
			
				
			}
		}
	}
	
	/**
	 * Print the internal debug stage.
	 */
	private static void printDebugStage()
	{
		StringBuilder sb = new StringBuilder();
		for(char[] debugLine : debugField)
		{
			sb.append('\n');
			for(char block : debugLine)
			{
				sb.append(block);
			}
			
		}
		
		System.out.println(sb.toString());
		
	}
	
	/**
	 * Set a character in the internal debug stage representation.
	 * 
	 * @param c The character.
	 * @param x The x-coordinate.
	 * @param y The y-coordinate.
	 */
	private static void setDebugChar(char c, int x, int y)
	{
		debugField[debugOffsetY + y][debugOffsetX + x] = c;
	}
	
	/**
	 * Print a stage to the console.
	 * 
	 * @param engine The game engine containing the stage.
	 * @param pieceX The x-coordinate of a piece.
	 * @param pieceY The y-coordinate of a piece.
	 * @param pieceRt The rotation of a piece.
	 * @param piece The piece.
	 * @param pieceChar The character that should be used to represent the piece.
	 */
	public static void printStage(GameEngine engine, int pieceX, int pieceY, int pieceRt, Piece piece, char pieceChar)
	{
		Field field = engine.field;
		int height = field.getHeight();
		int width = field.getWidth();
		resetDebugStage(width, height);
			
		for(int y = 0; y < height; ++y)
		{
			Block[] row = field.getRow(y);
			
			for(int x = 0; x < width; ++x)
			{
				int color = row[x].color;
				
				if(color != Block.BLOCK_COLOR_NONE)
				{
					setDebugChar((char)9632, x, y);
				}
			}
		}
		
		int[] dataX = piece.dataX[pieceRt];
		int[] dataY = piece.dataY[pieceRt];
		
		if(dataX.length != dataY.length)
		{
			System.out.println("Length mismatch in piece data");
		}
		
		for(int i = 0; i < dataX.length; ++i)
		{
			int px = pieceX + dataX[i];
			int py = pieceY + dataY[i];
			
			setDebugChar(pieceChar, px, py);
		}
		
		char d = (char)(pieceRt + (int)'0');
		
		setDebugChar(d, pieceX, pieceY);
		
		printDebugStage();
	}
}
