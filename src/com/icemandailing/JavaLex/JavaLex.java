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
	private int rowInParsingLine = 0;
	private String parsingLine = null;
	private InputStream source = null;
	private Scanner scanner = null;
	
	private static final HashSet<String> keywords;
	private static final String KEYWORD_LIST_PATH = "data/keyword_list.txt";
//	private static final String symbols = "+-*/%!|&=()[]{}<>.,?:;@";
	private static final HashSet<String> operators;
	private static final String OPERATOR_LIST_PATH = "data/operator_list.txt";
	private static final HashSet<String> delimiters;
	private static final String DELIMITER_LIST_PATH = "data/delimiter_list.txt";
	

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
//			try {
//				Double.parseDouble(num);
//				result = true;
//			} catch (NumberFormatException ex){
//				result = false;
//			}
//		return result;
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
	
	@SuppressWarnings("unused")
	private boolean isString(String value) {
		if (value.matches("(\"*\")|(\'*\')"))
			return true;
		else
			return false;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			JavaLex analyzer = new JavaLex(new File("SaxParserApp.java"));
//			JavaLex analyzer = new JavaLex("08.12d");
			while (analyzer.hasNextWord())
				System.out.println(analyzer.nextWord());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
