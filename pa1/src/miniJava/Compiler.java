package miniJava;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import miniJava.SyntacticAnalyzer.Parser;
import miniJava.SyntacticAnalyzer.Scanner;
import miniJava.AbstractSyntaxTrees.*;
import miniJava.AbstractSyntaxTrees.Package;
import miniJava.ContextualAnalysis.Identification;
import miniJava.ContextualAnalysis.TypeChecking;
//import miniJava.ContextualAnalysis.Identification;

public class Compiler {
	// Main function, the file to compile will be an argument.
	public static void main(String[] args) {
		// TODO: Instantiate the ErrorReporter object
		ErrorReporter reporter = new ErrorReporter();
		
//		File f=new File("D:\\Download\\pa1-tests-full\\pa1_tests\\pass155.java");
//		File f=new File("D:\\Download\\pa3-tests-partial\\pa3_tests\\fail340.java");
//		InputStream inputStream;
//		try {
//			inputStream = new FileInputStream(f);
//			Scanner s=new Scanner(inputStream,reporter);		
//			Parser p = new Parser(s, reporter);
//			AST programAST=p.parse();
//			Identification idf=new Identification(reporter);
//			idf.parse((Package)programAST);
//			if(reporter.hasErrors()) {
//				System.out.println("Error");
//				reporter.outputErrors();
//			}else {
//				TypeChecking tc = new TypeChecking(reporter);
//				tc.parse((Package)programAST);
//				if(reporter.hasErrors()) {
//					System.out.println("Error");
//					reporter.outputErrors();
//				}else {
//					System.out.println("Success");
//				}
////				ASTDisplay display=new ASTDisplay();
////				display.showTree(programAST);	
//			}
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		// TODO: Check to make sure a file path is given in args
		if (args[0] == null || args[0].trim().isEmpty()) {
	        System.out.println("Error");
	    }else {
	    	File f=new File(args[0]);
			try {
				InputStream inputStream = new FileInputStream(f);
				Scanner s=new Scanner(inputStream,reporter);		
				Parser p = new Parser(s, reporter);
				AST programAST=p.parse();
				Identification idf=new Identification(reporter);
				idf.parse((Package)programAST);
				if(reporter.hasErrors()) {
					System.out.println("Error");
					reporter.outputErrors();
				}else {
					TypeChecking tc = new TypeChecking(reporter);
					tc.parse((Package)programAST);
					if(reporter.hasErrors()) {
						System.out.println("Error");
						reporter.outputErrors();
					}else {
						System.out.println("Success");
					}
//					ASTDisplay display=new ASTDisplay();
//					display.showTree(programAST);	
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} 
	    }
		
	}
}