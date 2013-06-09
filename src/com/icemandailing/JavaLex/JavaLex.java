package com.icemandailing.JavaLex;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * @author starsasumi
 *
 */
public class JavaLex {
	
	private int parsingLine = 0;
	private int rowInParsingLine = 0;
	private InputStream source = null;
	private Scanner scanner = null;

	/**
	 * 
	 * @param source - the source file
	 * @throws FileNotFoundException
	 */
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
		this.parsingLine = 0;
		this.rowInParsingLine = 0;
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

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		InputStream in;
		try {
			in = new FileInputStream(new File("SaxParserApp.java"));
	        Scanner s = new Scanner(in); 
	        while(s.hasNextLine()){ 
	        	System.out.println(s.nextLine()); 
	        }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	}

}
