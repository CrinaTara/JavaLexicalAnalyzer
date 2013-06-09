package com.icemandailing.JavaLex;


/**
 * The Class Word is a data structure for JavaLex's output.
 */

public class Word {
	private int line;
	private int row;
	private int type;
	private String value;
	
	/**
	 * Here is all types that a Word object could be.
	 */
	public static final int KEYWORD = 1;
	public static final int IDENTIFIER = 2;
	public static final int OPERATOR = 3;
	public static final int CONSTANT = 4;
	public static final int DELIMITER = 5;
	
	public Word(int line, int row, int type, String value){
		setLine(line);
		setRow(row);
		setType(type);
		setValue(value);
	}

	/**
	 * @return the line
	 */
	public int getLine() {
		return line;
	}

	/**
	 * @return the row
	 */
	public int getRow() {
		return row;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param line the line to set
	 * @throws IllegalArgumentException 
	 */
	private void setLine(int line) throws IllegalArgumentException {
		if (line > 0)
			this.line = line;
		else
			throw new IllegalArgumentException("Line must be larger than 0");
	}

	/**
	 * @param row the row to set
	 * @throws IllegalArgumentException 
	 */
	private void setRow(int row) throws IllegalArgumentException {
		if (row > 0)
			this.row = row;
		else
			throw new IllegalArgumentException("Row must be larger than 0");
	}

	/**
	 * @param type the type to set
	 */
	private void setType(int type) {
		if (type >= KEYWORD && type <= DELIMITER)
			this.type = type;
		else 
			throw new IllegalArgumentException("Illegal type");
	}

	/**
	 * @param value the value to set
	 */
	private void setValue(String value) {
		this.value = value;
	}
	
}