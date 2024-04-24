package miniJava.ContextualAnalysis;

import miniJava.ErrorReporter;
import miniJava.AbstractSyntaxTrees.*;
import miniJava.AbstractSyntaxTrees.Package;
import miniJava.SyntacticAnalyzer.Token;
import miniJava.SyntacticAnalyzer.TokenType;

public class TypeChecking implements Visitor<Object, TypeDenoter> {
	private ErrorReporter _errors;

	public TypeChecking(ErrorReporter errors) {
		this._errors = errors;
	}

	public void parse(Package prog) {
		prog.visit(this, null);
	}

	private void reportTypeError(AST ast, String errMsg) {
		_errors.reportError(ast.posn == null ? "*** " + errMsg : "*** " + ast.posn.toString() + ": " + errMsg);
	}

	@Override
	public TypeDenoter visitPackage(Package prog, Object arg) {
		ClassDeclList cl = prog.classDeclList;
		for (ClassDecl c : cl) {
			c.visit(this, null);
		}
		return null;
	}

	@Override
	public TypeDenoter visitClassDecl(ClassDecl cd, Object arg) {
		for (FieldDecl f : cd.fieldDeclList) {
			f.visit(this, cd);
		}
		for (MethodDecl m : cd.methodDeclList) {
			m.visit(this, cd);
		}
		return null;
	}

	@Override
	public TypeDenoter visitFieldDecl(FieldDecl fd, Object arg) {

		return fd.type;
	}

	@Override
	public TypeDenoter visitMethodDecl(MethodDecl md, Object arg) {
		ParameterDeclList pdl = md.parameterDeclList;
		for (ParameterDecl pd : pdl) {
			pd.visit(this, null);
		}
		StatementList sl = md.statementList;
		for (Statement s : sl) {
			s.visit(this, md);
		}
		return md.type;
	}

	@Override
	public TypeDenoter visitParameterDecl(ParameterDecl pd, Object arg) {
		pd.type.visit(this, null);
		return pd.type;
	}

	@Override
	public TypeDenoter visitVarDecl(VarDecl decl, Object arg) {
		if (decl.type instanceof ClassType && ((ClassType) decl.type).className.spelling.equals("String")) {
			return new BaseType(TypeKind.UNSUPPORTED, decl.posn);
		}
		return decl.type;
	}

	@Override
	public TypeDenoter visitBaseType(BaseType type, Object arg) {

		return null;
	}

	@Override
	public TypeDenoter visitClassType(ClassType type, Object arg) {
		if(!(type.classDecl instanceof ClassDecl)) {
			reportTypeError(type,"Class type error");
		}
		return null;
	}

	@Override
	public TypeDenoter visitArrayType(ArrayType type, Object arg) {
		type.eltType.visit(this, null);
		return null;
	}

	@Override
	public TypeDenoter visitBlockStmt(BlockStmt stmt, Object arg) {
		MethodDecl md = (MethodDecl)arg;
		StatementList sl = stmt.sl;
        for (Statement s: sl) {
        	s.visit(this, md);
        }
		return null;
	}

	@Override
	public TypeDenoter visitVardeclStmt(VarDeclStmt stmt, Object arg) {
		MethodDecl md = (MethodDecl)arg;
		TypeDenoter tv = (TypeDenoter)stmt.varDecl.visit(this, md);
		TypeDenoter ti= (TypeDenoter)stmt.initExp.visit(this, md);
		if(ti != null) {
			if(!tv.equals(ti)) {
				reportTypeError(stmt,"Var declaration type error");
			}
		}else {
			reportTypeError(stmt,"Uexpected null");
		}
		return null;
	}

	@Override
	public TypeDenoter visitAssignStmt(AssignStmt stmt, Object arg) {
		MethodDecl md = (MethodDecl)arg;				
		TypeDenoter tr = (TypeDenoter)stmt.ref.visit(this, md);
		TypeDenoter tv = (TypeDenoter)stmt.val.visit(this, md);			
		if(!tr.equals(tv)) {
			reportTypeError(stmt,"Assigned value type error");
		}	
		return null;
	}

	@Override
	public TypeDenoter visitIxAssignStmt(IxAssignStmt stmt, Object arg) {
		MethodDecl md = (MethodDecl)arg;
		if(!(stmt.ref.decl.type instanceof ArrayType)){
			reportTypeError(stmt,"Expect array type");
			return null;
		}
		TypeDenoter ti = (TypeDenoter)stmt.ix.visit(this, md);
		TypeDenoter te = (TypeDenoter)stmt.exp.visit(this, md);
		if(ti.typeKind!= TypeKind.INT) {
			reportTypeError(stmt,"Expect int type");
		}
		if(!te.equals(((ArrayType)stmt.ref.decl.type).eltType)) {
			reportTypeError(stmt,"Wrong array type");
		}
		return null;
	}

	@Override
	public TypeDenoter visitCallStmt(CallStmt stmt, Object arg) {
		MethodDecl cnt = (MethodDecl)arg;		
		if(stmt.methodRef.decl instanceof MethodDecl){
			MethodDecl md = (MethodDecl)stmt.methodRef.decl;
			if(md.parameterDeclList.size() != stmt.argList.size()){
				reportTypeError(stmt,"Wrong arg number");
			}else{				
				for(int i = 0; i < md.parameterDeclList.size(); i++){
					Expression passedArg = stmt.argList.get(i);
					ParameterDecl param = md.parameterDeclList.get(i);
					TypeDenoter psa=(TypeDenoter)passedArg.visit(this, cnt);
					TypeDenoter pa=(TypeDenoter)param.visit(this, null);
					if(!pa.equals(psa)) {
						reportTypeError(stmt,"Wrong parameter type");
					}
				}
			}
		}else {
			reportTypeError(stmt,"Expect method");
		}		
		return null;
	}

	@Override
	public TypeDenoter visitReturnStmt(ReturnStmt stmt, Object arg) {
		MethodDecl md = (MethodDecl)arg;	
		if(stmt.returnExpr != null) {
			TypeDenoter tr = (TypeDenoter)stmt.returnExpr.visit(this, md);
			if(!tr.equals(md.type)) {
				reportTypeError(stmt,"Wrong return type");
			}
		}else{
			if(md.type.typeKind!=TypeKind.VOID) {
				reportTypeError(stmt,"Expect return");
			}
		}	
		return null;
	}

	@Override
	public TypeDenoter visitIfStmt(IfStmt stmt, Object arg) {
		MethodDecl md = (MethodDecl)arg;	
		TypeDenoter tc = (TypeDenoter)stmt.cond.visit(this, md);
		if(tc.typeKind!=TypeKind.BOOLEAN) {
			reportTypeError(stmt,"Expect boolean type");
		}
		if(stmt.thenStmt instanceof VarDeclStmt) {
			reportTypeError(stmt,"Solitary variable declaration");
		}
		stmt.thenStmt.visit(this, md);		
		if(stmt.elseStmt != null){
			if(stmt.elseStmt instanceof VarDeclStmt) {
				reportTypeError(stmt,"Solitary variable declaration");
			}
			stmt.elseStmt.visit(this, md);
		}
		return null;
	}

	@Override
	public TypeDenoter visitWhileStmt(WhileStmt stmt, Object arg) {
		MethodDecl md = (MethodDecl)arg;
		TypeDenoter tc = (TypeDenoter)stmt.cond.visit(this, md);
		if(tc.typeKind!=TypeKind.BOOLEAN) {
			reportTypeError(stmt,"Expect boolean type");
		}
		if(stmt.body instanceof VarDeclStmt) {
			reportTypeError(stmt,"Solitary variable declaration");
		}
		stmt.body.visit(this, md);		
		return null;
	}

	@Override
	public TypeDenoter visitUnaryExpr(UnaryExpr expr, Object arg) {
		MethodDecl md = (MethodDecl)arg;
		TypeDenoter e = (TypeDenoter)expr.expr.visit(this, md);
		String op=expr.operator.spelling;
		if(op.equals("!")) {
			if(e.typeKind!=TypeKind.BOOLEAN) {
				reportTypeError(expr,"Expect boolean type");
			}
			return new BaseType(TypeKind.BOOLEAN, expr.posn);
		}else if(op.equals("-")) {
			if(e.typeKind!=TypeKind.INT) {
				reportTypeError(expr,"Expect int type");
			}
			return new BaseType(TypeKind.INT, expr.posn);
		}else {
			reportTypeError(expr,"Expect unary operator");
			return new BaseType(TypeKind.ERROR, expr.posn);
		}
	}

	@Override
	public TypeDenoter visitBinaryExpr(BinaryExpr expr, Object arg) {
		MethodDecl md = (MethodDecl)arg;	
		TypeDenoter left = (TypeDenoter)expr.left.visit(this, md);
		TypeDenoter right = (TypeDenoter)expr.right.visit(this, md);
		String op=expr.operator.spelling;
		if(op.equals("||")||op.equals("&&")) {
			if(TypeKind.BOOLEAN!=left.typeKind || TypeKind.BOOLEAN!=right.typeKind) {
				reportTypeError(expr,"Expect boolean type");
			}
			return new BaseType(TypeKind.BOOLEAN, expr.posn);
		}else if(op.equals("==")||op.equals("!=")){
			if(left.typeKind!=right.typeKind) {
				reportTypeError(expr,"Expect same type for comparison");
			}else if(left.typeKind==TypeKind.CLASS) {
				if(!left.equals(right)) {
					reportTypeError(expr,"Expect same type for comparison");
				}
			}
			return new BaseType(TypeKind.BOOLEAN, expr.posn);
		}else if(op.equals(">=")||op.equals("<=")|| op.equals(">")|| op.equals("<")) {
			if(TypeKind.INT!=left.typeKind || TypeKind.INT!=right.typeKind) {
				reportTypeError(expr,"Expect int type");
			}
			return new BaseType(TypeKind.BOOLEAN, expr.posn);
		}else if(op.equals("+")||op.equals("-")|| op.equals("*")|| op.equals("/")) {
			if(TypeKind.INT!=left.typeKind || TypeKind.INT!=right.typeKind) {
				reportTypeError(expr,"Expect int type");
			}
			return new BaseType(TypeKind.INT, expr.posn);
		}else {
			reportTypeError(expr,"Expect binary operator");
			return new BaseType(TypeKind.ERROR, expr.posn);
		}
	}

	@Override
	public TypeDenoter visitRefExpr(RefExpr expr, Object arg) {

		return expr.ref.visit(this, null);
	}

	@Override
	public TypeDenoter visitIxExpr(IxExpr expr, Object arg) {
		MethodDecl md = (MethodDecl)arg;
		TypeDenoter refType = (TypeDenoter)expr.ref.visit(this, md);		
		if(!(refType instanceof ArrayType)){
			reportTypeError(expr,"Expect Array type");
			return new BaseType(TypeKind.ERROR, expr.posn);
		}		
		ArrayType at = (ArrayType)refType;
		if(TypeKind.INT!=((TypeDenoter)expr.ixExpr.visit(this, null)).typeKind) {
			reportTypeError(expr,"Expect int");
		}
		return at.eltType;
	}

	@Override
	public TypeDenoter visitCallExpr(CallExpr expr, Object arg) {
		MethodDecl cnt = (MethodDecl)arg;
		if(expr.functionRef.decl instanceof MethodDecl){
			MethodDecl md = (MethodDecl)expr.functionRef.decl;
			if(md.parameterDeclList.size() != expr.argList.size()){
				reportTypeError(expr,"Arg number wrong");
			}else{				
				for(int i = 0; i < md.parameterDeclList.size(); i++){
					Expression passedArg = expr.argList.get(i);
					ParameterDecl param = md.parameterDeclList.get(i);
					TypeDenoter tp=(TypeDenoter)param.visit(this, null);
					TypeDenoter tpa=(TypeDenoter)passedArg.visit(this, cnt);
					if(!tp.equals(tpa)) {
						reportTypeError(expr,"Method parameter type error");
					}
				}	
				return md.type;
			}
		}else{
			reportTypeError(expr,"Expected method");
		}
		return new BaseType(TypeKind.ERROR, expr.posn);
	}

	@Override
	public TypeDenoter visitLiteralExpr(LiteralExpr expr, Object arg) {

		return expr.lit.visit(this, null);
	}

	@Override
	public TypeDenoter visitNewObjectExpr(NewObjectExpr expr, Object arg) {
		if(expr.classtype.className.spelling.equals("String")) {
			return new BaseType(TypeKind.UNSUPPORTED, expr.posn);
		}		
		return expr.classtype;
	}

	@Override
	public TypeDenoter visitNewArrayExpr(NewArrayExpr expr, Object arg) {
		TypeDenoter ts=(TypeDenoter)expr.sizeExpr.visit(this, null);
		if(!ts.equals(new BaseType(TypeKind.INT, null))) {
			reportTypeError(expr,"Expected int");
		}
		return new ArrayType(expr.eltType, expr.posn);
	}

	@Override
	public TypeDenoter visitThisRef(ThisRef ref, Object arg) {
		ClassDecl cd = (ClassDecl)ref.decl;
		Token t=new Token(TokenType.Identifier, cd.name, cd.posn);
		return new ClassType(new Identifier(t,cd), cd.posn);
	}

	@Override
	public TypeDenoter visitIdRef(IdRef ref, Object arg) {
		if(ref.decl instanceof ClassDecl || ref.decl instanceof MethodDecl ) {
			reportTypeError(ref,"Expected id but got class/method");
		}
		if(ref.decl.type==null) {
			return new BaseType(TypeKind.ERROR, ref.posn);
		}else {
			return ref.decl.type;
		}
	}

	@Override
	public TypeDenoter visitQRef(QualRef ref, Object arg) {
		if(ref.decl instanceof MethodDecl || ref.ref.decl instanceof MethodDecl) {
			reportTypeError(ref,"Method type not expected");
		}
		return ref.decl.type;
	}

	@Override
	public TypeDenoter visitIdentifier(Identifier id, Object arg) {
		return id.d.visit(this, null);
	}

	@Override
	public TypeDenoter visitOperator(Operator op, Object arg) {
		return null;
	}

	@Override
	public TypeDenoter visitIntLiteral(IntLiteral num, Object arg) {
		return new BaseType(TypeKind.INT, num.posn);
	}

	@Override
	public TypeDenoter visitBooleanLiteral(BooleanLiteral bool, Object arg) {
		return new BaseType(TypeKind.BOOLEAN, bool.posn);
	}

	@Override
	public TypeDenoter visitNullLiteral(NullLiteral nullLit, Object arg) {
		return new BaseType(TypeKind.CLASS, nullLit.posn);
	}
}