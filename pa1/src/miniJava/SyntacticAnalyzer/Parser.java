package miniJava.SyntacticAnalyzer;

import miniJava.ErrorReporter;
import miniJava.AbstractSyntaxTrees.*;
import miniJava.AbstractSyntaxTrees.Package;

public class Parser {
	private Scanner _scanner;
	private ErrorReporter _errors;
	private Token _currentToken;

	public Parser(Scanner scanner, ErrorReporter errors) {
		this._scanner = scanner;
		this._errors = errors;
		this._currentToken = this._scanner.scan();
	}

	class SyntaxError extends Error {
		private static final long serialVersionUID = -6461942006097999362L;
	}

	public Package parse() {
		try {
			// The first thing we need to parse is the Program symbol
			ClassDeclList cdl=parseProgram();
			return new Package(cdl,_currentToken.getTokenPosition());
		} catch (SyntaxError e) {
		}
		return null;
	}

	// Program ::= (ClassDeclaration)* eot
	private ClassDeclList parseProgram() throws SyntaxError {
		// TODO: Keep parsing class declarations until eot
		ClassDeclList cdl=new ClassDeclList();
		while (_currentToken.getTokenType() != TokenType.EOT) {
			ClassDecl cd=parseClassDeclaration();
			cdl.add(cd);
		}
		accept(TokenType.EOT);
		return cdl;
	}

	// ClassDeclaration ::= class identifier { (FieldDeclaration|MethodDeclaration)*
	// }
	private ClassDecl parseClassDeclaration() throws SyntaxError {
		// TODO: Take in a "class" token (check by the TokenType)
		// What should be done if the first token isn't "class"?
		accept(TokenType.Class);
		// TODO: Take in an identifier token
		String name= _currentToken.getTokenText();
		accept(TokenType.Identifier);
		// TODO: Take in a {
		accept(TokenType.LCurly);
		// TODO: Parse either a FieldDeclaration or MethodDeclaration
		MethodDeclList mdl= new MethodDeclList();
		FieldDeclList fdl=new FieldDeclList();
		while (_currentToken.getTokenType() != TokenType.RCurly) {
			MemberDecl memd=parseFDorMD();
			if(memd instanceof FieldDecl) {
				FieldDecl fd=(FieldDecl) memd;
				fdl.add(fd);
			}else {
				MethodDecl md=(MethodDecl) memd;
				mdl.add(md);
			}
		}
		// TODO: Take in a }
		accept(TokenType.RCurly);
		return new ClassDecl(name,fdl,mdl,_currentToken.getTokenPosition());
	}

	private MemberDecl parseFDorMD() throws SyntaxError {
		boolean isPrivate=false;
		boolean isStatic=false;
		TypeDenoter td;
		TypeDenoter td2;
		String name;
		String name2;
		if (_currentToken.getTokenType() == TokenType.Visibility) {
			if( _currentToken.getTokenText().equals("private")) {
				isPrivate=true;
			}
			accept(TokenType.Visibility);
		}
		if (_currentToken.getTokenType() == TokenType.Static) {
			isStatic=true;
			accept(TokenType.Static);
		}
		if (_currentToken.getTokenType() == TokenType.Void) {
			td=new BaseType(TypeKind.VOID,_currentToken.getTokenPosition());
			accept(TokenType.Void);
			name= _currentToken.getTokenText();
			accept(TokenType.Identifier);
			accept(TokenType.LParen);
			ParameterDeclList pdl=new ParameterDeclList();
			while (_currentToken.getTokenType() != TokenType.RParen) {
				td2=parseType();
				name2= _currentToken.getTokenText();
				accept(TokenType.Identifier);
				pdl.add(new ParameterDecl(td2,name2,_currentToken.getTokenPosition()));
				if (_currentToken.getTokenType() == TokenType.Comma) {
					accept(TokenType.Comma);
					if(_currentToken.getTokenType() == TokenType.RParen) {
						_errors.reportError("Parse error: expecting \")\" but found \",\"");
					}
				}
			}
			accept(TokenType.RParen);
			accept(TokenType.LCurly);
			StatementList sl=new StatementList();
			while (_currentToken.getTokenType() != TokenType.RCurly) {
				Statement s=parseStatement();
				sl.add(s);
			}
			accept(TokenType.RCurly);
			return new MethodDecl(new FieldDecl(isPrivate,isStatic,td,name,_currentToken.getTokenPosition()),pdl,sl,_currentToken.getTokenPosition());
		} else {
			td=parseType();
			name=_currentToken.getTokenText();
			accept(TokenType.Identifier);
			if (_currentToken.getTokenType() == TokenType.LParen) {
				accept(TokenType.LParen);
				ParameterDeclList pdl=new ParameterDeclList();
				while (_currentToken.getTokenType() != TokenType.RParen) {
					td2=parseType();
					name2= _currentToken.getTokenText();
					accept(TokenType.Identifier);
					pdl.add(new ParameterDecl(td2,name2,_currentToken.getTokenPosition()));
					if (_currentToken.getTokenType() == TokenType.Comma) {
						accept(TokenType.Comma);
						if(_currentToken.getTokenType() == TokenType.RParen) {
							_errors.reportError("Parse error: expecting \")\" but found \",\"");
						}
					}
				}
				accept(TokenType.RParen);
				accept(TokenType.LCurly);
				StatementList sl=new StatementList();
				while (_currentToken.getTokenType() != TokenType.RCurly) {
					Statement s=parseStatement();
					sl.add(s);
				}
				accept(TokenType.RCurly);
				return new MethodDecl(new FieldDecl(isPrivate,isStatic,td,name,_currentToken.getTokenPosition()),pdl,sl,_currentToken.getTokenPosition());
			} else {
				accept(TokenType.Semicolon);
				return new FieldDecl(isPrivate,isStatic,td,name,_currentToken.getTokenPosition());
			}
		}
	}

	private TypeDenoter parseType() throws SyntaxError {
		if (_currentToken.getTokenType() == TokenType.INT) {
			accept(TokenType.INT);
			if (_currentToken.getTokenType() == TokenType.LSquare) {
				accept(TokenType.LSquare);
				accept(TokenType.RSquare);
				return new ArrayType(new BaseType(TypeKind.INT,_currentToken.getTokenPosition()),_currentToken.getTokenPosition());
			}
			return new BaseType(TypeKind.INT,_currentToken.getTokenPosition());
		} else if (_currentToken.getTokenType() == TokenType.Boolean) {
			accept(TokenType.Boolean);
			return new BaseType(TypeKind.BOOLEAN,_currentToken.getTokenPosition());
		} else {
			Token t=_currentToken;
			accept(TokenType.Identifier);
			if (_currentToken.getTokenType() == TokenType.LSquare) {
				accept(TokenType.LSquare);
				accept(TokenType.RSquare);
				return new ArrayType(new ClassType(new Identifier(t),_currentToken.getTokenPosition()),_currentToken.getTokenPosition());
			}
			return new ClassType(new Identifier(t),_currentToken.getTokenPosition());
		}
	}

	private Statement parseStatement() throws SyntaxError {
		if (_currentToken.getTokenType() == TokenType.LCurly) {
			accept(TokenType.LCurly);
			StatementList sl=new StatementList();
			while (_currentToken.getTokenType() != TokenType.RCurly) {
				Statement s=parseStatement();
				sl.add(s);
			}
			accept(TokenType.RCurly);
			return new BlockStmt(sl,_currentToken.getTokenPosition());
		} else if (_currentToken.getTokenType() == TokenType.Boolean || _currentToken.getTokenType() == TokenType.INT) {
			TypeDenoter td=parseType();
			String name=_currentToken.getTokenText();
			accept(TokenType.Identifier);
			accept(TokenType.Equals);
			Expression e=parseExpression();
			accept(TokenType.Semicolon);
			VarDecl vd=new VarDecl(td,name,_currentToken.getTokenPosition());
			return new VarDeclStmt(vd,e,_currentToken.getTokenPosition());
		} else if (_currentToken.getTokenType() == TokenType.Identifier) {
			Token t=_currentToken;
			accept(TokenType.Identifier);
			if (_currentToken.getTokenType() == TokenType.LSquare) {
				accept(TokenType.LSquare);
				if (_currentToken.getTokenType() == TokenType.RSquare) {
					accept(TokenType.RSquare);
					String name=_currentToken.getTokenText();
					accept(TokenType.Identifier);
					accept(TokenType.Equals);
					Expression e=parseExpression();
					accept(TokenType.Semicolon);
					TypeDenoter td=new ArrayType(new ClassType(new Identifier(t),_currentToken.getTokenPosition()),_currentToken.getTokenPosition());
					VarDecl vd=new VarDecl(td,name,_currentToken.getTokenPosition());
					return new VarDeclStmt(vd,e,_currentToken.getTokenPosition());
				} else {
					Expression i=parseExpression();
					accept(TokenType.RSquare);
					accept(TokenType.Equals);
					Expression e=parseExpression();
					accept(TokenType.Semicolon);
					IdRef ir=new IdRef(new Identifier(t),_currentToken.getTokenPosition());
					return new IxAssignStmt(ir,i,e,_currentToken.getTokenPosition());
				}
			} else if (_currentToken.getTokenType() == TokenType.Identifier) {
				String name=_currentToken.getTokenText();
				accept(TokenType.Identifier);
				accept(TokenType.Equals);
				Expression e=parseExpression();
				accept(TokenType.Semicolon);
				TypeDenoter td=new ClassType(new Identifier(t),_currentToken.getTokenPosition());
				VarDecl vd=new VarDecl(td,name,_currentToken.getTokenPosition());
				return new VarDeclStmt(vd,e,_currentToken.getTokenPosition());
			} else {
				Reference r=new IdRef(new Identifier(t),_currentToken.getTokenPosition());
				Identifier id=null;
				while (_currentToken.getTokenType() != TokenType.LParen
						&& _currentToken.getTokenType() != TokenType.LSquare
						&& _currentToken.getTokenType() != TokenType.Equals) {
					accept(TokenType.Dot);
					if (_currentToken.getTokenType() == TokenType.Identifier) {
						id=new Identifier(_currentToken);
					}
					accept(TokenType.Identifier);
					r=new QualRef(r,id,_currentToken.getTokenPosition());
				}
				return selectStatement(r);
			}
		} else if (_currentToken.getTokenType() == TokenType.This) {
			Reference r=parseReference();
			return selectStatement(r);
		} else if (_currentToken.getTokenType() == TokenType.Return) {
			Expression e=null;
			accept(TokenType.Return);
			if (_currentToken.getTokenType() == TokenType.Semicolon) {
				accept(TokenType.Semicolon);
			} else {
				e=parseExpression();
				accept(TokenType.Semicolon);
			}
			return new ReturnStmt(e,_currentToken.getTokenPosition());
		} else if (_currentToken.getTokenType() == TokenType.If) {
			accept(TokenType.If);
			accept(TokenType.LParen);
			Expression b=parseExpression();
			accept(TokenType.RParen);
			Statement t=parseStatement();
			if (_currentToken.getTokenType() == TokenType.Else) {
				accept(TokenType.Else);
				Statement e=parseStatement();
				return new IfStmt(b,t,e,_currentToken.getTokenPosition());
			}
			return new IfStmt(b,t,_currentToken.getTokenPosition());
		} else {
			accept(TokenType.While);
			accept(TokenType.LParen);
			Expression e=parseExpression();
			accept(TokenType.RParen);
			Statement s=parseStatement();
			return new WhileStmt(e,s,_currentToken.getTokenPosition());
		}
	}

	private Expression parseExpression() throws SyntaxError {
		Expression exp=null;
		if (_currentToken.getTokenType() == TokenType.Operator) {
			if (_currentToken.getTokenText().equals("-")) {
				Token t=_currentToken;
				accept(TokenType.Operator);
				Expression e=parseExpression();
				Operator o=new Operator(t);
				exp=new UnaryExpr(o,e,_currentToken.getTokenPosition());
			} else if (_currentToken.getTokenText().equals("!")) {
				Token t=_currentToken;
				accept(TokenType.Operator);
				Expression e=parseExpression();
				Operator o=new Operator(t);
				exp=new UnaryExpr(o,e,_currentToken.getTokenPosition());
			} else {
				_errors.reportError("Parse error: expecting UNOP but found BINOP");
				throw new SyntaxError();
			}
		} else if (_currentToken.getTokenType() == TokenType.This
				|| _currentToken.getTokenType() == TokenType.Identifier) {
			Reference r=parseReference();
			exp=new RefExpr(r,_currentToken.getTokenPosition());
			if (_currentToken.getTokenType() == TokenType.LSquare) {
				accept(TokenType.LSquare);
				Expression e=parseExpression();
				accept(TokenType.RSquare);
				exp=new IxExpr(r,e,_currentToken.getTokenPosition());
			} else if (_currentToken.getTokenType() == TokenType.LParen) {
				accept(TokenType.LParen);
				ExprList el=new ExprList();
				if (_currentToken.getTokenType() == TokenType.RParen) {
					accept(TokenType.RParen);
				} else {
					Expression e=parseExpression();
					el.add(e);
					while (_currentToken.getTokenType() != TokenType.RParen) {
						accept(TokenType.Comma);
						e=parseExpression();
						el.add(e);
					}
					accept(TokenType.RParen);
				}
				exp=new CallExpr(r,el,_currentToken.getTokenPosition());
			}
		} else if (_currentToken.getTokenType() == TokenType.NUM) {
			Token token=_currentToken;
			accept(TokenType.NUM);
			Terminal t=new IntLiteral(token);
			exp=new LiteralExpr(t,_currentToken.getTokenPosition());
		} else if (_currentToken.getTokenType() == TokenType.True) {
			Token token=_currentToken;
			accept(TokenType.True);
			Terminal t=new BooleanLiteral(token);
			exp=new LiteralExpr(t,_currentToken.getTokenPosition());
		} else if (_currentToken.getTokenType() == TokenType.False) {
			Token token=_currentToken;
			accept(TokenType.False);
			Terminal t=new BooleanLiteral(token);
			exp=new LiteralExpr(t,_currentToken.getTokenPosition());
		} else if (_currentToken.getTokenType() == TokenType.New) {
			accept(TokenType.New);
			if (_currentToken.getTokenType() == TokenType.INT) {
				accept(TokenType.INT);
				accept(TokenType.LSquare);
				Expression e=parseExpression();
				accept(TokenType.RSquare);
				TypeDenoter td=new BaseType(TypeKind.INT,_currentToken.getTokenPosition());
				exp=new NewArrayExpr(td,e,_currentToken.getTokenPosition());
			} else {
				Token t=_currentToken;
				accept(TokenType.Identifier);
				if (_currentToken.getTokenType() == TokenType.LSquare) {
					accept(TokenType.LSquare);
					Expression e=parseExpression();
					accept(TokenType.RSquare);
					TypeDenoter td=new ClassType(new Identifier(t),_currentToken.getTokenPosition());
					exp=new NewArrayExpr(td,e,_currentToken.getTokenPosition());
				} else {
					accept(TokenType.LParen);
					accept(TokenType.RParen);
					ClassType ct=new ClassType(new Identifier(t),_currentToken.getTokenPosition());
					exp=new NewObjectExpr(ct,_currentToken.getTokenPosition());
				}
			}
		} else if (_currentToken.getTokenType() == TokenType.LParen) {
			accept(TokenType.LParen);
			Expression e=parseExpression();
			accept(TokenType.RParen);
			exp=e;
		}
		if (_currentToken.getTokenType() == TokenType.Operator) {
			if (!_currentToken.getTokenText().equals("!")) {
				Token t=_currentToken;
				accept(TokenType.Operator);
				Operator o=new Operator(t);
				Expression e=parseExpression();
				exp=new BinaryExpr(o,exp,e,_currentToken.getTokenPosition());
			} else {
				_errors.reportError("Parse error: expecting BINOP but found UNOP");
				throw new SyntaxError();
			}
		}
		return exp;
	}

	private Statement selectStatement(Reference r) throws SyntaxError {
		if (_currentToken.getTokenType() == TokenType.LParen) {
			accept(TokenType.LParen);
			ExprList el=new ExprList();
			if (_currentToken.getTokenType() == TokenType.RParen) {
				accept(TokenType.RParen);
				accept(TokenType.Semicolon);
			} else {
				Expression e = parseExpression();
				el.add(e);
				while (_currentToken.getTokenType() != TokenType.RParen) {
					accept(TokenType.Comma);
					e=parseExpression();
					el.add(e);
				}
				accept(TokenType.RParen);
				accept(TokenType.Semicolon);
			}
			return new CallStmt(r,el,_currentToken.getTokenPosition());
		} else if (_currentToken.getTokenType() == TokenType.LSquare) {
			accept(TokenType.LSquare);
			Expression i =parseExpression();
			accept(TokenType.RSquare);
			accept(TokenType.Equals);
			Expression e =parseExpression();
			accept(TokenType.Semicolon);
			return new IxAssignStmt(r,i,e,_currentToken.getTokenPosition());
		} else {
			accept(TokenType.Equals);
			Expression e =parseExpression();
			accept(TokenType.Semicolon);
			return new AssignStmt(r,e,_currentToken.getTokenPosition());
		}
	}

	private Reference parseReference() throws SyntaxError {
		Reference r;
		Identifier id=null;
		if (_currentToken.getTokenType() == TokenType.This) {
			accept(TokenType.This);
			r=new ThisRef(_currentToken.getTokenPosition());
		} else {
			Token t=_currentToken;
			accept(TokenType.Identifier);
			r=new IdRef(new Identifier(t),_currentToken.getTokenPosition());
		}
		while (_currentToken.getTokenType() == TokenType.Dot) {
			accept(TokenType.Dot);
			if (_currentToken.getTokenType() == TokenType.Identifier) {
				id=new Identifier(_currentToken);
			}
			accept(TokenType.Identifier);
			r=new QualRef(r,id,_currentToken.getTokenPosition());
		}
		return r;
	}

	// This method will accept the token and retrieve the next token.
	// Can be useful if you want to error check and accept all-in-one.
	private void accept(TokenType expectedType) throws SyntaxError {
//		System.out.println(_currentToken.getTokenType() + ", " + _currentToken.getTokenText()+ ", " + _currentToken.getTokenPosition());
//		System.out.print(_currentToken.getTokenText()+" ");
		if (_currentToken.getTokenType() == expectedType) {
			_currentToken = _scanner.scan();
			return;
		}

		// TODO: Report an error here.
		// "Expected token X, but got Y"
		_errors.reportError(
				"Parse error: expecting '" + expectedType + "' but found '" + _currentToken.getTokenType() + "'");
		throw new SyntaxError();
	}
}
