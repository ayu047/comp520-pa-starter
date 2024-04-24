/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.Token;

public class Identifier extends Terminal {

	public Declaration d;

	public Identifier(Token t) {
		super(t);
	}

	public Identifier(Token t, Declaration d) {
		super(t);
		this.d = d;
	}

	public <A, R> R visit(Visitor<A, R> v, A o) {
		return v.visitIdentifier(this, o);
	}
	
	public boolean equals(Identifier other){
		return this.spelling.equals(other.spelling) && this.kind.equals(other.kind);
	}

}
