package miniJava;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import miniJava.SyntacticAnalyzer.Parser;
import miniJava.SyntacticAnalyzer.Scanner;
import miniJava.AbstractSyntaxTrees.*;

public class Compiler {
	// Main function, the file to compile will be an argument.
	public static void main(String[] args) {
		// TODO: Instantiate the ErrorReporter object
		ErrorReporter reporter = new ErrorReporter();
		
//		File f=new File("D:\\Download\\pa1-tests-full\\pa1_tests\\pass155.java");
		File f=new File("D:\\Download\\pa2-tests-partial\\pa2_tests_partial\\pass155.java");
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(f);
			Scanner s=new Scanner(inputStream,reporter);		
			Parser p = new Parser(s, reporter);
			AST programAST=p.parse();
			if(reporter.hasErrors()) {
				System.out.println("Error");
				reporter.outputErrors();
			}else {
//				System.out.println("Success");
				ASTDisplay display=new ASTDisplay();
				display.showTree(programAST);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// TODO: Check to make sure a file path is given in args
//		if (args[0] == null || args[0].trim().isEmpty()) {
//	        System.out.println("Error");
//	    }else {
//	    	File f=new File(args[0]);
//	    	// TODO: Create the inputStream using new FileInputStream
//			// TODO: Instantiate the scanner with the input stream and error object
//			// TODO: Instantiate the parser with the scanner and error object
//			// TODO: Call the parser's parse function
//			try {
//				InputStream inputStream = new FileInputStream(f);
//				Scanner s=new Scanner(inputStream,reporter);		
//				Parser p = new Parser(s, reporter);
//				AST programAST=p.parse();
//				// TODO: Check if any errors exist, if so, println("Error")
//				//  then output the errors
//				// TODO: If there are no errors, println("Success")
//				if(reporter.hasErrors()) {
//					System.out.println("Error");
//					reporter.outputErrors();
//				}else {
////					System.out.println("Success");
//					ASTDisplay display=new ASTDisplay();
//					display.showTree(programAST);
//				}
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			} 
//	    }
		
	}
}