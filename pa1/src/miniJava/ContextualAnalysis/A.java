//public void enter(Declaration dec){
//		// TODO: I think the condition isn't necessary, but will test later.
//		if(getLevel() >= 4){
//			for(int i = table.size() - 1; i >= 3; i--){
//				if(table.get(i).containsKey(dec.name)){
//					reporter.addError("*** line " + dec.posn.getStartLineNum() + ": attempts to declare " + ContextualAnalysis.localizeDeclName(dec) + " with conflicting declaration on line " + table.get(i).get(dec.name).posn.getStartLineNum() + "!");
//					return;
//				}
//			}
//			
//			table.peek().put(dec.name, dec);
//		}else{
//			if(table.peek().containsKey(dec.name)){
//				if(dec instanceof MethodDecl){
//					MethodDecl entering = (MethodDecl)dec;
//					Declaration conflicting = table.peek().get(dec.name);
//					
//					if(conflicting instanceof MultiMethodDecl) {
//						ArrayList<MethodDecl> possibleDecls = ((MultiMethodDecl)conflicting).possibleDecls;
//						
//						boolean foundGenuineConflict = false;
//						
//						for(MethodDecl pd : possibleDecls){
//							if(pd.parameterDeclList.size() == entering.parameterDeclList.size()){ // continue
//								boolean isSame = true;
//								
//								for(int i = 0; i < pd.parameterDeclList.size(); i++)
//									if(!pd.parameterDeclList.get(i).type.equals(entering.parameterDeclList.get(i).type)) {
//										isSame = false;
//										break;
//									}
//								
//								if(isSame){
//									foundGenuineConflict = true;
//									reporter.addError("*** line " + dec.posn.getStartLineNum() + ": attempts to declare method " + ContextualAnalysis.localizeDeclName(dec) + " with conflicting declaration on line " + conflicting.posn.getStartLineNum() + "!");
//									break;
//								}
//							}
//						}
//						
//						if(!foundGenuineConflict)
//							((MultiMethodDecl)conflicting).possibleDecls.add(entering);
//					}else if(conflicting instanceof MethodDecl){
//						MethodDecl conflictingMD = (MethodDecl)conflicting;
//						boolean foundGenuineConflict = false;
//						
//						if(conflictingMD.parameterDeclList.size() == entering.parameterDeclList.size()){
//							boolean isSame = true;
//							
//							for(int i = 0; i < conflictingMD.parameterDeclList.size(); i++)
//								if(!conflictingMD.parameterDeclList.get(i).type.equals(entering.parameterDeclList.get(i).type)){
//									isSame = false;
//									break;
//								}
//							
//							if(isSame){
//								foundGenuineConflict = true;
//								reporter.addError("*** line " + dec.posn.getStartLineNum() + ": attempts to declare method " + ContextualAnalysis.localizeDeclName(dec) + " with conflicting declaration on line " + conflicting.posn.getStartLineNum() + "!");
//							}
//						}
//						
//						if(!foundGenuineConflict)
//							table.peek().put(dec.name, new MultiMethodDecl(conflictingMD, entering));
//					}else
//						reporter.addError("*** line " + dec.posn.getStartLineNum() + ": attempts to declare " + ContextualAnalysis.localizeDeclName(dec) + " with conflicting declaration on line " + conflicting.posn.getStartLineNum() + "!");
//				}else
//					reporter.addError("*** line " + dec.posn.getStartLineNum() + ": attempts to declare " + ContextualAnalysis.localizeDeclName(dec) + " with conflicting declaration on line " + table.peek().get(dec.name).posn.getStartLineNum() + "!");
//			}else
//				table.peek().put(dec.name, dec);
//		}
//	}
