package miniJava.ContextualAnalysis;

import miniJava.ErrorReporter;
import miniJava.AbstractSyntaxTrees.Package;
import miniJava.SyntacticAnalyzer.Token;
import miniJava.SyntacticAnalyzer.TokenType;
import miniJava.AbstractSyntaxTrees.*;

public class Identification implements Visitor<Object, Object> {
	private ErrorReporter _errors;
	private ScopedIdentification table;

	public Identification(ErrorReporter errors) {
		this._errors = errors;
		table = new ScopedIdentification(errors);
		// TODO: predefined names
		table.openScope();
		table.addDeclaration(new ClassDecl("_PrintStream", new FieldDeclList(),
				new MethodDeclList(
						new MethodDecl(new FieldDecl(false, false, new BaseType(TypeKind.VOID, null), "println", null),
								new ParameterDeclList(new ParameterDecl(new BaseType(TypeKind.INT, null), "n", null)),
								new StatementList(), null)),
				null));
		Token SysToken = new Token(TokenType.Class, "_PrintStream", null);
		Identifier SysId = new Identifier(SysToken);
		table.addDeclaration(new ClassDecl("System", new FieldDeclList(new FieldDecl(false, true,
				new ClassType(new Identifier(SysToken, table.findDeclaration(SysId, null)), null), "out", null)),
				new MethodDeclList(), null));
		table.addDeclaration(new ClassDecl("String", new FieldDeclList(), new MethodDeclList(), null));
	}

	public void parse(Package prog) throws IdentificationError {
		try {
			visitPackage(prog, null);
		} catch (IdentificationError e) {
			_errors.reportError(e.toString());
		}
	}

	public Object visitPackage(Package prog, Object arg) throws IdentificationError {
		ClassDeclList cl = prog.classDeclList;
		for (ClassDecl c : cl) {
			table.addDeclaration(c);
		}
		for (ClassDecl c : cl) {
			c.visit(this, null);
		}
		table.closeScope();
		return null;
	}

	class IdentificationError extends Error {
		private static final long serialVersionUID = -441346906191470192L;
		private String _errMsg;

		public IdentificationError(AST ast, String errMsg) {
			super();
			this._errMsg = ast.posn == null ? "*** " + errMsg : "*** " + ast.posn.toString() + ": " + errMsg;
		}

		@Override
		public String toString() {
			return _errMsg;
		}
	}

	@Override
	public Object visitClassDecl(ClassDecl cd, Object arg) throws IdentificationError {
		if(arg == null){
			table.openScope();
			for (FieldDecl f : cd.fieldDeclList) {
				table.addDeclaration(f);
			}
			for (MethodDecl m : cd.methodDeclList) {
				table.addDeclaration(m);
			}
			for (FieldDecl f : cd.fieldDeclList) {
				f.visit(this, cd);
			}
//			System.out.println(cd.methodDeclList.get(0).visit(this, cd));
			for (MethodDecl m : cd.methodDeclList) {	
				m.visit(this, cd);
			}
			table.closeScope();
		}else{
			Identifier searchingFor = (Identifier)arg;			
			for(FieldDecl fd : cd.fieldDeclList)
				if(fd.name.equals(searchingFor.spelling))
					return fd;			
			for(MethodDecl md : cd.methodDeclList)
				if(md.name.equals(searchingFor.spelling))
					return md;
		}
		
		return null;
	}

	@Override
	public Object visitFieldDecl(FieldDecl fd, Object arg) throws IdentificationError {
		fd.type.visit(this, null);
		fd.cntClass = (ClassDecl)arg;
		return null;
	}

	@Override
	public Object visitMethodDecl(MethodDecl md, Object arg) throws IdentificationError {
		md.type.visit(this, null);
		md.cntClass = (ClassDecl)arg;
		table.openScope();
        ParameterDeclList pdl = md.parameterDeclList;
        for (ParameterDecl pd: pdl) {
            pd.visit(this, null);
        }
        table.openScope();
        StatementList sl = md.statementList;
        for (Statement s: sl) {
            s.visit(this, md);
        }
        table.closeScope();
		table.closeScope();
		return null;
	}

	@Override
	public Object visitParameterDecl(ParameterDecl pd, Object arg) throws IdentificationError {
		pd.type.visit(this, null);
		table.addDeclaration(pd);
		return null;
	}

	@Override
	public Object visitVarDecl(VarDecl decl, Object arg) throws IdentificationError {
		decl.type.visit(this, null);
		table.addDeclaration(decl);
		return null;
	}

	@Override
	public Object visitBaseType(BaseType type, Object arg) throws IdentificationError {
		return null;
	}

	@Override
	public Object visitClassType(ClassType type, Object arg) throws IdentificationError {
//		待定
		type.classDecl = table.findDeclaration(type.className, null);
		return null;
	}

	@Override
	public Object visitArrayType(ArrayType type, Object arg) throws IdentificationError {
		type.eltType.visit(this, null);
		return null;
	}

	@Override
	public Object visitBlockStmt(BlockStmt stmt, Object arg) throws IdentificationError {
		MethodDecl md = (MethodDecl)arg;
		table.openScope();
		StatementList sl = stmt.sl;
        for (Statement s: sl) {
        	s.visit(this, md);
        }
        table.closeScope();
		return null;
	}

	@Override
	public Object visitVardeclStmt(VarDeclStmt stmt, Object arg) throws IdentificationError {
		MethodDecl md = (MethodDecl)arg;
		stmt.varDecl.visit(this, null);	
        stmt.initExp.visit(this, md);
        stmt.varDecl.isInitialized = true;
		return null;
	}

	@Override
	public Object visitAssignStmt(AssignStmt stmt, Object arg) throws IdentificationError {
		
		MethodDecl md = (MethodDecl)arg;
		stmt.ref.visit(this, md);
        stmt.val.visit(this, md);
		return null;
	}

	@Override
	public Object visitIxAssignStmt(IxAssignStmt stmt, Object arg) throws IdentificationError {
		
		MethodDecl md = (MethodDecl)arg;
		stmt.ref.visit(this, md);
        stmt.ix.visit(this, md);
        stmt.exp.visit(this, md);
		return null;
	}

	@Override
	public Object visitCallStmt(CallStmt stmt, Object arg) throws IdentificationError {
		
		MethodDecl md = (MethodDecl)arg;
		stmt.methodRef.visit(this, md);
        ExprList al = stmt.argList;
        for (Expression e: al) {
            e.visit(this, md);
        }
		return null;
	}

	@Override
	public Object visitReturnStmt(ReturnStmt stmt, Object arg) throws IdentificationError {
		
		MethodDecl md = (MethodDecl)arg;
		if (stmt.returnExpr != null)
            stmt.returnExpr.visit(this, md);
		return null;
	}

	@Override
	public Object visitIfStmt(IfStmt stmt, Object arg) throws IdentificationError {
		
		MethodDecl md = (MethodDecl)arg;
		stmt.cond.visit(this, md);
        stmt.thenStmt.visit(this, md);
        if (stmt.elseStmt != null)
            stmt.elseStmt.visit(this, md);
		return null;
	}

	@Override
	public Object visitWhileStmt(WhileStmt stmt, Object arg) throws IdentificationError {
		
		MethodDecl md = (MethodDecl)arg;
		stmt.cond.visit(this, md);
        stmt.body.visit(this, md);
		return null;
	}

	@Override
	public Object visitUnaryExpr(UnaryExpr expr, Object arg) throws IdentificationError {
		
		MethodDecl md = (MethodDecl)arg;
		expr.operator.visit(this, null);
        expr.expr.visit(this, md);
		return null;
	}

	@Override
	public Object visitBinaryExpr(BinaryExpr expr, Object arg) throws IdentificationError {
		
		MethodDecl md = (MethodDecl)arg;
		expr.operator.visit(this, null);
        expr.left.visit(this, md);
        expr.right.visit(this, md);
		return null;
	}

	@Override
	public Object visitRefExpr(RefExpr expr, Object arg) throws IdentificationError {
		
		MethodDecl md = (MethodDecl)arg;
		expr.ref.visit(this, md);
		return null;
	}

	@Override
	public Object visitIxExpr(IxExpr expr, Object arg) throws IdentificationError {
		
		MethodDecl md = (MethodDecl)arg;
		expr.ref.visit(this, md);
		expr.ixExpr.visit(this, md);
		return null;
	}

	@Override
	public Object visitCallExpr(CallExpr expr, Object arg) throws IdentificationError {

		MethodDecl md = (MethodDecl)arg;
		expr.functionRef.visit(this, md);
        ExprList al = expr.argList;
        for (Expression e: al) {
            e.visit(this, md);
        }
		return null;
	}

	@Override
	public Object visitLiteralExpr(LiteralExpr expr, Object arg) throws IdentificationError {
//		MethodDecl md = (MethodDecl)arg;
//		expr.lit.visit(this, md);
		return null;
	}

	@Override
	public Object visitNewObjectExpr(NewObjectExpr expr, Object arg) throws IdentificationError {
		
//		MethodDecl md = (MethodDecl)arg;
		expr.classtype.visit(this, null);
//		待定
		return null;
	}

	@Override
	public Object visitNewArrayExpr(NewArrayExpr expr, Object arg) throws IdentificationError {
		
		MethodDecl md = (MethodDecl)arg;
		expr.eltType.visit(this, null);
	    expr.sizeExpr.visit(this, md);
		return null;
	}

	@Override
	public Object visitThisRef(ThisRef ref, Object arg) throws IdentificationError {
		
		MethodDecl md = (MethodDecl)arg;
		if(md.isStatic) {
			_errors.reportError("Identification error: non-static this");
			throw new IdentificationError(ref, "Identification error: non-static this");
		}
		ref.decl = md.cntClass;
		return null;
	}

	@Override
	public Object visitIdRef(IdRef ref, Object arg) throws IdentificationError{
		
		MethodDecl md = (MethodDecl)arg;
		Declaration d=(Declaration)ref.id.visit(this, md);
		ref.decl=d;
		return null;
	}

	@Override
	public Object visitQRef(QualRef ref, Object arg) throws IdentificationError {
		MethodDecl md = (MethodDecl)arg;
    	ref.ref.visit(this, md);
    	Declaration cnt = ref.ref.decl;
    	if(cnt==null) {
    		_errors.reportError("Identification error: context not found");
    		throw new IdentificationError(ref, "Identification error: context not found");
    	}
    	if(cnt instanceof ClassDecl){
			ClassDecl cd = (ClassDecl)cnt;
			Declaration d = (Declaration)cd.visit(this, ref.id);
			
			if(d == null){
				_errors.reportError("Identification error: id not found in reference");
				throw new IdentificationError(ref, "Identification error: id not found in reference");
			}
			
			if(d instanceof MemberDecl){
				MemberDecl mmd = (MemberDecl)d;
				if(md.isStatic && !mmd.isStatic){
					_errors.reportError("Identification error: reference non-static");
					throw new IdentificationError(ref, "Identification error: reference non-static");
				}
				
				if(mmd.isPrivate && cd != md.cntClass) {
					_errors.reportError("Identification error: private reference");
					throw new IdentificationError(ref, "Identification error: private reference");
				}
			}
			
			ref.id.d = d;
			ref.decl = ref.id.d;
		}else if(cnt instanceof LocalDecl){
			LocalDecl ld = (LocalDecl)cnt;
			if(ld.type.typeKind==TypeKind.CLASS) {
				ClassType ct = (ClassType)ld.type;
				ClassDecl cd = (ClassDecl)table.findDeclaration(ct.className, md);
				Declaration d = (Declaration)cd.visit(this, ref.id);
				
				if(d == null){
					_errors.reportError("Identification error: id not found");
					throw new IdentificationError(ref, "Identification error: id not found");
				}
				
				if(d instanceof MemberDecl){
					MemberDecl _d = (MemberDecl)d;
					if(_d.isPrivate && cd != md.cntClass) {
						_errors.reportError("Identification error: private reference");
						throw new IdentificationError(ref, "Identification error: private reference");
					}
				}
				
				ref.id.d = d;
				ref.decl = ref.id.d;
			}else {
				_errors.reportError("Identification error: wrong TypeKind");
				throw new IdentificationError(ref, "Identification error:  wrong TypeKind");
			}
		}else if(cnt instanceof MemberDecl){
			MemberDecl memd = (MemberDecl)cnt;
			if(memd.type.typeKind==TypeKind.CLASS) {
				ClassType ct = (ClassType)memd.type;
				ClassDecl cd = (ClassDecl)table.findDeclaration(ct.className, md);
				Declaration d = (Declaration)cd.visit(this, ref.id);
				
				if(d == null){
					_errors.reportError("Identification error: id not found");
					throw new IdentificationError(ref, "Identification error: id not found");
				}
				
				if(d instanceof MemberDecl){
					MemberDecl _d = (MemberDecl)d;
					if(_d.isPrivate && cd != md.cntClass) {
						_errors.reportError("Identification error: private reference");
						throw new IdentificationError(ref, "Identification error: private reference");
					}
				}
				
				ref.id.d = d;
				ref.decl = ref.id.d;
			}else {
				_errors.reportError("Identification error: wrong TypeKind");
				throw new IdentificationError(ref, "Identification error: wrong TypeKind");
			}
		}
		return null;
	}

	@Override
	public Object visitIdentifier(Identifier id, Object arg) throws IdentificationError {
		MethodDecl md = (MethodDecl)arg;
		return table.findDeclaration(id, md);
	}

	@Override
	public Object visitOperator(Operator op, Object arg) throws IdentificationError {
		return null;
	}

	@Override
	public Object visitIntLiteral(IntLiteral num, Object arg) throws IdentificationError {
		return null;
	}

	@Override
	public Object visitBooleanLiteral(BooleanLiteral bool, Object arg) throws IdentificationError {
		return null;
	}

	@Override
	public Object visitNullLiteral(NullLiteral nullLit, Object arg) throws IdentificationError {
		return null;
	}
}