package miniJava.SyntacticAnalyzer;

public class SourcePosition {
	private int line;
	
	public SourcePosition(int position) {
		// TODO: Store the token's type and text
		this.line = position;
	}
	
	@Override
    public String toString() {
        return "Line:"+this.line;
    }

}
