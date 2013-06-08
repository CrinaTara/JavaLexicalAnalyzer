package com.icemandailing.JavaLex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

public class JavaLex {

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
