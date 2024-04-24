package miniJava.ContextualAnalysis;

import java.util.HashMap;
import java.util.Stack;

import miniJava.ErrorReporter;
import miniJava.AbstractSyntaxTrees.Declaration;
import miniJava.AbstractSyntaxTrees.Identifier;
import miniJava.AbstractSyntaxTrees.MemberDecl;
import miniJava.AbstractSyntaxTrees.MethodDecl;
import miniJava.AbstractSyntaxTrees.VarDecl;

public class ScopedIdentification {
	private Stack<HashMap<String, Declaration>> table;
	private ErrorReporter _errors;

	public ScopedIdentification(ErrorReporter errors) {
		this._errors = errors;
		table = new Stack<>();
	}

//	class IdentificationError extends Error {
//		private static final long serialVersionUID = -441346906191470192L;
//		private String _errMsg;
//
//		public IdentificationError(AST ast, String errMsg) {
//			super();
//			this._errMsg = ast.posn == null ? "*** " + errMsg : "*** " + ast.posn.toString() + ": " + errMsg;
//		}
//
//		@Override
//		public String toString() {
//			return _errMsg;
//		}
//	}

	public void openScope() {
		table.add(new HashMap<>());
	}

	public void addDeclaration(Declaration d){
//		System.out.println(table.size()+" "+d.name);
		if (table.size() > 2) {
			for (int i = 2; i < table.size(); i++) {
				if (table.get(i).get(d.name) != null) {
					_errors.reportError("Identification error: String exists on level 2+");
//					throw new IdentificationError(d, "Identification error: String exists on level 2+");
				}
			}
			table.peek().put(d.name, d);
		} else {
			if (table.peek().containsKey(d.name)) {
				if (d instanceof MethodDecl) {
					MethodDecl entering = (MethodDecl) d;
					Declaration conflicting = table.peek().get(d.name);

					if (conflicting instanceof MethodDecl) {
						MethodDecl conflictingMD = (MethodDecl) conflicting;

						if (conflictingMD.parameterDeclList.size() == entering.parameterDeclList.size()) {
							boolean isSame = true;

							for (int i = 0; i < conflictingMD.parameterDeclList.size(); i++)
								if (!conflictingMD.parameterDeclList.get(i).type
										.equals(entering.parameterDeclList.get(i).type)) {
									isSame = false;
									break;
								}

							if (isSame) {
								_errors.reportError("Identification error: Method conflict");
//								throw new IdentificationError(d, "Identification error: Method conflict");
							}
						}
					} else
						_errors.reportError("Identification error: Declaration conflict");
//					throw new IdentificationError(d, "Identification error: Declaration conflict");
				} else
					_errors.reportError("Identification error: Declaration conflict");
//				throw new IdentificationError(d, "Identification error: Declaration conflict");
			} else
				table.peek().put(d.name, d);
		}
//		if(d instanceof ClassDecl) {
//			if(d.name.equals("System")) {
//				for(int i=table.size()-1;i>=0;i--) {
//					if(table.get(i).get("System")!=null) {
//						_errors.reportError("Identification error: predefined class customize");
//						throw new IdentificationError(d,"Identification error: predefined class customize");
//					}
//				}
//			}else if(d.name.equals("String")) {
//				for(int i=table.size()-1;i>=0;i--) {
//					if(table.get(i).get("String")!=null) {
//						_errors.reportError("Identification error: predefined class customize");
//						throw new IdentificationError(d,"Identification error: predefined class customize");
//					}
//				}
//			}else if(d.name.equals("_PrintStream")) {
//				for(int i=table.size()-1;i>=0;i--) {
//					if(table.get(i).get("_PrintStream")!=null) {
//						_errors.reportError("Identification error: predefined class customize");
//						throw new IdentificationError(d,"Identification error: predefined class customize");
//					}
//				}
//			}
//		}
	}

	public Declaration findDeclaration(Identifier id, MethodDecl cnt) {
		Declaration d = null;
		for (int i = table.size() - 1; i >= 0; i--) {
			d = table.get(i).get(id.spelling);
			if (d != null) {
//				System.out.println(d.name);
				break;
			}
		}
		if (d == null) {
			_errors.reportError("Identification error: Identifier " + id.spelling + " not found");
//			throw new IdentificationError(cnt, "Identification error: Identifier " + id.spelling + " not found");
		} else if (d instanceof MemberDecl && cnt!=null && cnt.isStatic && !((MemberDecl)d).isStatic) {
			_errors.reportError("Identification error: Non-static reference");
//			throw new IdentificationError(cnt, "Identification error: Non-static reference");
		}else if(d instanceof VarDecl && !((VarDecl)d).isInitialized) {
			_errors.reportError("Not initialized");
		}
		return d;
	}

	public void closeScope() {
		table.pop();
	}
}
