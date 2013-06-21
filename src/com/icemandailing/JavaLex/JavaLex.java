package com.icemandailing.JavaLex;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Scanner;

/**
 * @author starsasumi
 *
 */
public class JavaLex {
	
	private int line = -1;
	private int rowInParsingLine = 0;	// point to the begin row of a word
	private int index = 0;	// point to the row of NEXT CHARACTER
	private String parsingLine = null;
//	private String wordValue = "";	// value of word
	private String nextChar = null;
	private InputStream source = null;
	private Scanner scanner = null;
	
	private static final HashSet<String> keywords;
	private static final String KEYWORD_LIST_PATH = "data/keyword_list.txt";
	private static final HashSet<String> operators;
	private static final String OPERATOR_LIST_PATH = "data/operator_list.txt";
	private static final HashSet<String> delimiters;
	private static final String DELIMITER_LIST_PATH = "data/delimiter_list.txt";
	private static final String NUM_CHAR = "[\\d]";
	private static final String OCT_CHAR = "[0-7]";
	private static final String HEX_CHAR = "[0-9a-fA-F]";
	private static final String INT_SUFFIX = "[lL]";
	private static final String REAL_SUFFIX = "[DdFf]";
	private static final String STRING_DELIMITER = "[\"\']";	
	private static final String IDENTIFIER_CHAR = "[_a-zA-Z]";

	/**
	 * 
	 * @param source - the source file
	 * @throws FileNotFoundException
	 */
	static {
		keywords = new HashSet<String>();
		Scanner tempScanner = null;
		try {
			tempScanner = new Scanner(new FileInputStream(JavaLex.KEYWORD_LIST_PATH));
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}
		
		while (tempScanner.hasNextLine()) {
			keywords.add(tempScanner.nextLine());
		}
		
		operators = new HashSet<String>();
		
		try {
			tempScanner = new Scanner(new FileInputStream(JavaLex.OPERATOR_LIST_PATH));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		while (tempScanner.hasNextLine()) {
			operators.add(tempScanner.nextLine());
		}
		
		delimiters = new HashSet<String>();
		
		try {
			tempScanner = new Scanner(new FileInputStream(JavaLex.DELIMITER_LIST_PATH));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		while (tempScanner.hasNextLine()) {
			delimiters.add(tempScanner.nextLine());
		}
	}
	
	public JavaLex(File source) throws FileNotFoundException{
		initWithFile(source);
	}
	/**
	 * 
	 * @param source - the String that contains source code
	 */
	public JavaLex(String source) {
		initWithString(source);
	}
	
	public void reset(){
		this.line = -1;
		this.rowInParsingLine = 0;
		this.index = 0;
		this.parsingLine = null;
		this.scanner = null;
		this.source = null;
	}
	
	public void initWithFile(File source) throws FileNotFoundException{
		this.source = new FileInputStream(source);
		this.scanner = new Scanner(this.source);
	}
	
	public void initWithString(String source){
		this.source = new ByteArrayInputStream(source.getBytes());
		this.scanner = new Scanner(this.source);
	}
	
	public Word nextWord() {
		Word word = null;
		String value = "";
		
		while (this.parsingLine == null || this.parsingLine.trim().isEmpty()) {
			this.parsingLine = this.scanner.nextLine();
			this.rowInParsingLine = 0;
			this.line++;
		}
		
		int index = this.rowInParsingLine;
		String ch = this.parsingLine.substring(index, index+1);
		
		while (isBlank(ch) && (index+1 < this.parsingLine.length())) {
			index = ++(this.rowInParsingLine);
			ch = this.parsingLine.substring(index, index+1);
		}
		
		if (isChar(ch)) {
			value = value.concat(ch);
			index++;
			while(index < this.parsingLine.length()) {
				ch = this.parsingLine.substring(index, index+1);
				if (isChar(ch) || isNum(ch)) {
					value = value.concat(ch);
					index++;
				} else {
					break;
				}
			}
			
			if (isKeyword(value))
				word = new Word(this.line, this.rowInParsingLine, Word.KEYWORD, value);
			else
				word = new Word(this.line, this.rowInParsingLine, Word.IDENTIFIER, value);
			
			this.rowInParsingLine = index;
			
		} else if (isNum(ch)) {
			value = value.concat(ch);
			index++;
			while (index < this.parsingLine.length()) {
				ch = this.parsingLine.substring(index, index+1);
				if (isNum(value.concat(ch))) {
					value = value.concat(ch);
					index++;
				} else {
					break;
				}
			}
			
			word = new Word(this.line, this.rowInParsingLine, Word.CONSTANT, value);
			this.rowInParsingLine = index;
			
		} else if (isSymbol(ch)) {
			value = value.concat(ch);
			index++;
			
			while (index < this.parsingLine.length()) {
				ch = this.parsingLine.substring(index, index+1);
				
				if (isDelimiter(value.concat(ch)) || isOperator(value.concat(ch))) {
					value = value.concat(ch);
					index++;
				} else {
					break;
				}
			}
			
			if (isDelimiter(value))
				word = new Word(this.line, this.rowInParsingLine, Word.DELIMITER, value);
			else if (isOperator(value))
				word  = new Word(this.line, this.rowInParsingLine, Word.OPERATOR, value);
			else
				try {
					throw new Exception("Can not parse a word");
				} catch (Exception e) {
					e.printStackTrace();
				}
			
			this.rowInParsingLine = index;
			
		} else {
			index = (++this.rowInParsingLine);
		}
		
		if (index == this.parsingLine.length()) {
			this.parsingLine = null;
			this.rowInParsingLine = 0;
		}
		
		return word;
	}
	
	/**
	 * 
	 * @return : a word or null
	 * 
	 * use nextWord() to filter null out.
	 */
	public Word getNextWord() {
		this.rowInParsingLine = index-1;
		int beginLine = this.line;
		if (this.nextChar == null)
			this.nextChar = getNextChar();
		Word word = null;
		String value = "";
		
		if (nextChar.matches(NUM_CHAR)) {
			value = value.concat(nextChar);
			
			if (nextChar.equals("0")) {	// hex, oct(real) or 0(real)
				nextChar = getNextChar();
				
				if (nextChar.toLowerCase().equals("x")) {	// hex
					value = value.concat(nextChar);
					while ((nextChar = getNextChar()).matches(HEX_CHAR)) {
						value = value.concat(nextChar);
					}
					
					if (nextChar.matches(INT_SUFFIX)) {	// end: set nextChar null
						value = value.concat(nextChar);
						nextChar = null;
					}
					
					word = new Word(beginLine, this.rowInParsingLine, Word.CONSTANT, value);
					
				} else if (nextChar.matches(OCT_CHAR)) {	// oct(or not...)
					value = value.concat(nextChar);
					while ((nextChar = getNextChar()).matches(OCT_CHAR)) {
						value = value.concat(nextChar);
					}
					
					if (nextChar.matches(NUM_CHAR)) {
						value = value.concat(this.nextChar);
						while ((nextChar = getNextChar()).matches(NUM_CHAR)) {
							value = value.concat(nextChar);
						}
					}
					
					if (nextChar.equals(".")) {	// real or other number
						value = value.concat(nextChar);
						while ((nextChar = getNextChar()).matches(NUM_CHAR)) {
							value = value.concat(nextChar);
						}
						if (nextChar.matches(REAL_SUFFIX)) {	// end: set nextChar null
							value = value.concat(nextChar);
							nextChar = null;
						}
					} else if (nextChar.matches(REAL_SUFFIX) || nextChar.matches(INT_SUFFIX)) {	// oct; end: set nextChar null
						value = value.concat(nextChar);
						nextChar = null;
					}	
					word = new Word(beginLine, this.rowInParsingLine, Word.CONSTANT, value);
				} else {	// 0
					if (nextChar.equals(".")) {	// real
						value = value.concat(nextChar);
						while ((nextChar = getNextChar()).matches(NUM_CHAR)) {
							value = value.concat(nextChar);
						}
						if (nextChar.matches(REAL_SUFFIX)) {	// end: set nextChar null
							value = value.concat(nextChar);
							nextChar = null;
						}
					} else if (nextChar.matches(REAL_SUFFIX) || nextChar.matches(INT_SUFFIX)) {	// 0; end: set nextChar null
						value = value.concat(nextChar);
						nextChar = null;
					}	
					word = new Word(beginLine, this.rowInParsingLine, Word.CONSTANT, value);
				}
			} else {	// other numbers
				while ((this.nextChar = getNextChar()).matches(NUM_CHAR)) {
					value = value.concat(this.nextChar);
				}
				
				if (this.nextChar.equals(".")) {
					value = value.concat(this.nextChar);
					this.nextChar = getNextChar();
					
					if (this.nextChar.matches(NUM_CHAR)) {
						while (this.nextChar.matches(NUM_CHAR)) {
							value = value.concat(this.nextChar);
							this.nextChar = getNextChar();
						}
					}
				}
				
				if (this.nextChar.matches(REAL_SUFFIX) || this.nextChar.matches(INT_SUFFIX)) {	// end: set nextChar null
					value = value.concat(this.nextChar);
					this.nextChar = null;
				}
				
				word = new Word(beginLine, this.rowInParsingLine, Word.CONSTANT, value);
			}
		} else if (this.nextChar.matches(STRING_DELIMITER)) {
			String delimiter = this.nextChar;
			value = value.concat(this.nextChar);
			while (!(this.nextChar = getNextChar()).equals(delimiter)) {
				if (this.nextChar.equals("\n") || this.nextChar.equals("")) {
					break;
				}
				value = value.concat(this.nextChar);
				if (this.nextChar.equals("\\")) {
					this.nextChar = getNextChar();
					value = value.concat(this.nextChar);
				}
			}
			
			if (this.nextChar.equals(delimiter)) {
				value = value.concat(this.nextChar);	// String end: set nextChar null
			}
			this.nextChar = null;
			word = new Word(beginLine, this.rowInParsingLine, Word.CONSTANT, value);
		} else if (isSymbol(this.nextChar)) {
			while (isDelimiter(value.concat(nextChar)) || isOperator(value.concat(nextChar))) {
				value = value.concat(this.nextChar);
				this.nextChar = getNextChar();
			}
			
			if (value.equals("//")) {	// inline comment
				while ((!this.nextChar.equals("\n")) && (!this.nextChar.equals(""))) {
					value = value.concat(this.nextChar);
					this.nextChar = getNextChar();
				}
				
				this.nextChar = null;	// comment end: set nextChar null
				word = new Word(beginLine, this.rowInParsingLine, Word.COMMENT, value);
				
			} else if (value.equals("/*")) {	// block comment
				String end = "";
				while (!this.nextChar.equals("")) {
					if (this.nextChar.equals("*"))
						end = this.nextChar;
					
					value = value.concat(this.nextChar);
					this.nextChar = getNextChar();
					
					if (this.nextChar.equals("/")) {
						end = end.concat(this.nextChar);
						if (end.equals("*/")) {	// comment end: set nextChar null
							value = value.concat(this.nextChar);
							this.nextChar = null;
							break;
						}
					} else 
						end = "";
				}
				
				word = new Word(beginLine, this.rowInParsingLine, Word.COMMENT, value);
			} else if (value.equals(".")) {
				if (this.nextChar.matches(NUM_CHAR)) {
					while (this.nextChar.matches(NUM_CHAR)) {
						value = value.concat(this.nextChar);
						this.nextChar = getNextChar();
					}
					
					if (this.nextChar.matches(REAL_SUFFIX)) {	// end: set nextChar null
						value = value.concat(this.nextChar);
						this.nextChar = null;
					}
					
					word = new Word(beginLine, this.rowInParsingLine, Word.CONSTANT, value);
				} else {
					word = new Word(beginLine, this.rowInParsingLine, Word.DELIMITER, value);
				}
				
			} else if (isDelimiter(value)) {
				word = new Word(beginLine, this.rowInParsingLine, Word.DELIMITER, value);
			} else if (isOperator(value)) {
				word = new Word(beginLine, this.rowInParsingLine, Word.OPERATOR, value);
			} else {
				word = new Word(beginLine, this.rowInParsingLine, Word.UNDEFINED, value);
			}
		} else if (this.nextChar.matches(IDENTIFIER_CHAR)) {
			value = value.concat(this.nextChar);
			while ((this.nextChar = getNextChar()).matches(IDENTIFIER_CHAR) || this.nextChar.matches(NUM_CHAR)) {
				value = value.concat(this.nextChar);
			}
			
			if (isKeyword(value))
				word = new Word(beginLine, this.rowInParsingLine, Word.KEYWORD, value);
			else
				word = new Word(beginLine, this.rowInParsingLine, Word.IDENTIFIER, value);
			
		} else {
			nextChar = getNextChar();
		}
			
		return word;
	}
	
	
	/**
	 * 
	 * @return : the next character
	 * 
	 * return "" when there is no more character.
	 * 
	 */
	
	public String getNextChar() {
		while (this.parsingLine == null || this.parsingLine.trim().isEmpty()) {
			if (this.scanner.hasNextLine()) {
				this.parsingLine = this.scanner.nextLine();
				this.rowInParsingLine = 0;
				this.index = 0;
				this.line++;
			} else {
				return "";
			}
			return "\n";
		}
		
		String ch = this.parsingLine.substring(index, index+1);
		index++;
		
		if (index == this.parsingLine.length()) {
			this.parsingLine = null;
		}
		return ch;
	}
	
	public boolean hasNextWord() {
		if ((this.parsingLine != null) || this.scanner.hasNext())
			return true;
		else
			return false;
	}

	private boolean isKeyword(String value) {
		if (JavaLex.keywords.contains(value))
			return true;
		else
			return false;
	}
	private boolean isNum(String num) {
//		boolean result = false;
		if (num.matches("(0(x|X)[\\da-fA-F]*[lL]?)"))
			return true;
		else if (num.matches("(0[0-7]*[dDfFlL]?)"))
			return true;
		else if (num.matches("[\\d]\\.?[dDfFlL]?"))
			return true;
		else if (num.matches("[1-9][\\d]*\\.?[dDfFlL]?"))
			return true;
		else if (num.matches("([\\d]|[1-9][\\d]*)?\\.[\\d]+[dDfF]?"))
			return true;
		else
			return false; 
	}
	private boolean isChar(String ch) {
		if((!isNum(ch)) && (!isSymbol(ch)) && (!isBlank(ch)))
			return true;
		else
			return false;
	}
	private boolean isBlank(String ch) {
		String temp = ch.trim();
		if (temp.isEmpty())
			return true;
		else
			return false;
	}
	private boolean isSymbol(String ch) {
		if ((ch.length() == 1) && (JavaLex.delimiters.contains(ch) || JavaLex.operators.contains(ch)))
			return true;
		else
			return false;
	}
	
	private boolean isDelimiter(String value) {
		if (JavaLex.delimiters.contains(value))
			return true;
		else
			return false;
	}
	
	private boolean isOperator(String value) {
		if (JavaLex.operators.contains(value))
			return true;
		else
			return false;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

//		try {
//			JavaLex analyzer1 = new JavaLex(new File("test.txt"));
//			while (analyzer1.hasNextWord()) {
//				System.out.println(analyzer1.nextWord());
//
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		try {
			JavaLex analyzer = new JavaLex(new File("test.txt"));
			while (analyzer.hasNextWord()) {
				System.out.println(analyzer.getNextWord());

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
