/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class ClassType extends TypeDenoter
{
    public ClassType(Identifier cn, SourcePosition posn){
        super(TypeKind.CLASS, posn);
        className = cn;
    }
            
    public <A,R> R visit(Visitor<A,R> v, A o) {
        return v.visitClassType(this, o);
    }
    
    @Override
	public boolean equals(TypeDenoter other){
    	if(other != null) {
//    		System.out.println(className.d);
//    		System.out.println(((ClassType)other).className.d);
    		return (typeKind == other.typeKind && other instanceof ClassType && className.equals(((ClassType)other).className))
    				|| (other.typeKind == TypeKind.CLASS && other instanceof BaseType);
    	}else {
    		return false;
    	}
//		return other != null && ((typeKind == other.typeKind && other instanceof ClassType && className.equals(((ClassType)other).className))
//				|| (other.typeKind == TypeKind.CLASS && other instanceof BaseType)); // null is fine
	}
    
    public Identifier className;
    public Declaration classDecl;
}
