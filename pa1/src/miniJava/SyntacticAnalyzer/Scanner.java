package miniJava.SyntacticAnalyzer;

import java.io.IOException;
import java.io.InputStream;

import miniJava.ErrorReporter;

public class Scanner {
	private InputStream _in;
	private ErrorReporter _errors;
	private StringBuilder _currentText;
	private char _currentChar;
	private int line_number;

	private boolean eot = false;
	private TokenType type;

	public Scanner(InputStream in, ErrorReporter errors) {
		this._in = in;
		this._errors = errors;
		this._currentText = new StringBuilder();
		this.line_number=1;

		nextChar();
	}

	public Token scan() {
		// TODO: This function should check the current char to determine what the token
		// could be.

		// TODO: Consider what happens if the current char is whitespace

		// TODO: Consider what happens if there is a comment (// or /* */)

		// TODO: What happens if there are no more tokens?

		// TODO: Determine what the token is. For example, if it is a number
		// keep calling takeIt() until _currentChar is not a number. Then
		// create the token via makeToken(TokenType.IntegerLiteral) and return it.
		_currentText = new StringBuilder();
		while (!eot && (_currentChar == ' ' || _currentChar == '\r' || _currentChar == '\n' || _currentChar == 9)) {
			skipIt();
		}
		if (eot) {
			type = TokenType.EOT;
			return makeToken(type);
		} else {
			if (_currentChar == '/') {
				skipIt();
				if (eot) {
					_currentText.append('/');
					type = TokenType.Operator;
					return makeToken(type);
				} else if (_currentChar == '/') {
					while (!eot && _currentChar != '\r' && _currentChar != '\n') {
						skipIt();
					}
					scan();
				} else if (_currentChar == '*') {
					skipIt();
					while (!eot) {
						if (_currentChar == '*') {
							skipIt();
							if (_currentChar == '/') {
								skipIt();
								break;
							} else {
								if(eot) {
									_errors.reportError("Scan Error: comment fail");
									type = TokenType.ERROR;
									return makeToken(type);
								}
							}
						} else {
							skipIt();
							if(eot) {
								_errors.reportError("Scan Error: comment fail");
								type = TokenType.ERROR;
								return makeToken(type);
							}
						}
					}
					scan();
				} else {
					_currentText.append('/');
					type = TokenType.Operator;
					return makeToken(type);
				}
			} else if (_currentChar == '(') {
				takeIt();
				type = TokenType.LParen;
			} else if (_currentChar == ')') {
				takeIt();
				type = TokenType.RParen;
			} else if (_currentChar == '[') {
				takeIt();
				type = TokenType.LSquare;
			} else if (_currentChar == ']') {
				takeIt();
				type = TokenType.RSquare;
			} else if (_currentChar == '{') {
				takeIt();
				type = TokenType.LCurly;
			} else if (_currentChar == '}') {
				takeIt();
				type = TokenType.RCurly;
			} else if (_currentChar == ';') {
				takeIt();
				type = TokenType.Semicolon;
			} else if (_currentChar == ',') {
				takeIt();
				type = TokenType.Comma;
			} else if (_currentChar == '.') {
				takeIt();
				type = TokenType.Dot;
			} else if (_currentChar == '=') {
				takeIt();
				if (_currentChar == '=') {
					takeIt();
					type = TokenType.Operator;
				} else {
					type = TokenType.Equals;
				}
			} else if (_currentChar == '+' || _currentChar == '*' || _currentChar == '-') {
				takeIt();
				type = TokenType.Operator;
			} else if (_currentChar == '&') {
				takeIt();
				if (_currentChar == '&') {
					takeIt();
					type = TokenType.Operator;
				} else {
					_errors.reportError("Scan Error: Unrecognized character ' & ' in input");
					type = TokenType.ERROR;
				}
			} else if (_currentChar == '|') {
				takeIt();
				if (_currentChar == '|') {
					takeIt();
					type = TokenType.Operator;
				} else {
					_errors.reportError("Scan Error: Unrecognized character ' | ' in input");
					type = TokenType.ERROR;
				}
			} else if (_currentChar == '>' || _currentChar == '<' || _currentChar == '!') {
				takeIt();
				if (_currentChar == '=') {
					takeIt();
				}
				type = TokenType.Operator;
			} else if (Character.isLetter(_currentChar)) {
				if (isCorr("void")) {
					type = TokenType.Void;
				} else if (isCorr("class")) {
					type = TokenType.Class;
				} else if (_currentChar == 'p') {
					takeIt();
					if (isCorr("ublic")) {
						type = TokenType.Visibility;
					} else if (isCorr("rivate")) {
						type = TokenType.Visibility;
					} else {
						isLiteral();
					}
				} else if (isCorr("return")) {
					type = TokenType.Return;
				} else if (isCorr("while")) {
					type = TokenType.While;
				} else if (_currentChar == 'i') {
					takeIt();
					if (isCorr("nt")) {
						type = TokenType.INT;
					} else if (isCorr("f")) {
						type = TokenType.If;
					} else {
						isLiteral();
					}
				} else if (isCorr("static")) {
					type = TokenType.Static;
				} else if (isCorr("boolean")) {
					type = TokenType.Boolean;
				} else if (_currentChar == 't') {
					takeIt();
					if (isCorr("his")) {
						type = TokenType.This;
					} else if (isCorr("rue")) {
						type = TokenType.True;
					} else {
						isLiteral();
					}
				} else if (isCorr("else")) {
					type = TokenType.Else;
				}else if (_currentChar == 'n') {
					takeIt();
					if (isCorr("ew")) {
						type = TokenType.New;
					} else if (isCorr("ull")) {
						type = TokenType.Null;
					} else {
						isLiteral();
					}
				} else if (isCorr("false")) {
					type = TokenType.False;
				} else {
					isLiteral();
				}
			} else if (Character.isDigit(_currentChar)) {
				while (Character.isDigit(_currentChar)) {
					takeIt();
				}
				type = TokenType.NUM;
			} else {
				_errors.reportError("Scan Error: Unrecognized character '" + _currentChar + "' in input");
				type = TokenType.ERROR;
			}
//			System.out.println(type+", "+_currentText.toString());
			return makeToken(type);
		}
	}

	private boolean isCorr(String s) {
		boolean isCorrect = true;
		char[] dismantled = s.toCharArray();
		for (int i = 0; i < dismantled.length; i++) {
			if (!eot && _currentChar == dismantled[i]) {
				takeIt();
			} else {
				isCorrect = false;
				break;
			}
		}
		if (Character.isLetterOrDigit(_currentChar) || _currentChar == '_') {
			isCorrect = false;
		}
		return isCorrect;
	}

	private void isLiteral() {
		while (Character.isLetterOrDigit(_currentChar) || _currentChar == '_') {
			takeIt();
		}
		type = TokenType.Identifier;
	}

	private void takeIt() {
		_currentText.append(_currentChar);
		nextChar();
	}

	private void skipIt() {
		nextChar();
	}

	private void nextChar() {
		try {
			if (!eot) {
				int c = _in.read();
//				System.out.println(c);
				if (c >= 128) {
					_errors.reportError("Scan Error: non-regular ASCII character");
					eot = true;
				} else if (c == -1) {
					eot = true;
				} else {
					_currentChar = (char) c;
					if(_currentChar == '\r'|| _currentChar == '\n') {
						line_number=line_number+1;
					}
				}
				// TODO: What happens if c == -1?
				// TODO: What happens if c is not a regular ASCII character?
			}
		} catch (IOException e) {
			// TODO: Report an error here
			_errors.reportError("Scan Error: I/O Exception!");
			eot = true;
		}
	}

	private Token makeToken(TokenType toktype) {
		// TODO: return a new Token with the appropriate type and text
		// contained in
		return new Token(toktype, _currentText.toString(), new SourcePosition(line_number));
	}
}
