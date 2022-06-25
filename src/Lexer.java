// The Lexer class contains a lexical analyzer that returns Tokens
//   from the input file on each call to getNextTokens. The class
//   also contains functions that adjust the spacing based on the
//   type and context of the Tokens.

import java.io.*;


public class Lexer {
	public static final int SUPPRESS_NEITHER_SPACE = 0,
			SUPPRESS_LEADING_SPACE = 1, SUPPRESS_TRAILING_SPACE = 2;
	private int i = 0, spacing;
	private char character;
	private String line = "";
	private BufferedReader file;
	private Output output;
	private Token currentToken, lastToken;
	private String currentLexeme, lastLexeme;

	// Constructor initializes private data members and opens the input
	//   file.

	public Lexer(String fileName, Output output)
			throws FileNotFoundException {
		file = new BufferedReader(new FileReader(fileName + ".c"));
		character = nextChar();
		currentLexeme = "";
		lastToken = Token.NONE;
		this.output = output;
	}

	// Closes input file

	public void close() throws IOException {
		file.close();
	}

	// adjustSpacing will set bits in the spacing word to indicate the
	//   type of spacing adjustment to be done, LEADING OR TRAILING.

	public void adjustSpacing(int spacingValue) {
		spacing |= spacingValue;
	}

	// checkDeclarationSpacing sets leading or trailing space bits in
	//   the variable "spacing" according to the type of current token.

	public void checkDeclarationSpacing(Token current) {
		if (currentToken == Token.UNARY_OR_BINARY_OPERATOR)
			spacing |= SUPPRESS_TRAILING_SPACE;
		else if (currentToken == Token.LEFT_PARENTHESIS)
			spacing |= SUPPRESS_LEADING_SPACE;
	}

	// checkExpressionSpacing sets leading or trailing space bits in
	//   the variable "spacing" according to the types of current and
	//   last tokens.

	public void checkExpressionSpacing(Token current, Token previous) {
		if (current == Token.PRE_OR_POST_UNARY_OPERATOR)
			if (previous == Token.IDENTIFIER ||
					previous == Token.RIGHT_BRACKET ||
					previous == Token.RIGHT_PARENTHESIS)
				spacing |= SUPPRESS_LEADING_SPACE;
			else
				spacing |= SUPPRESS_TRAILING_SPACE;
		else if (current == Token.UNARY_OR_BINARY_OPERATOR)
			if (previous != Token.IDENTIFIER &&
					previous != Token.RIGHT_BRACKET
					&& previous != Token.RIGHT_PARENTHESIS && previous !=
					Token.PRE_OR_POST_UNARY_OPERATOR)
				spacing |= SUPPRESS_TRAILING_SPACE;
	}

	//  getNextToken returns the next token in the input file and
	//    displays the previous token. Comment and preprocessor tokens
	//    are skipped.

	public Token getNextToken() {

		//assign currentToken to lastToken and empty lastToken
		if (lastToken != Token.NONE) {
			currentToken = lastToken;
			lastToken = Token.NONE;
			return currentToken;
		}

		output.outputToken(currentLexeme, spacing);
		spacing = SUPPRESS_NEITHER_SPACE;
		lastLexeme = currentLexeme;
		do {
			currentLexeme = "";
			while (character != 0 && Character.isWhitespace(character))
				character = nextChar();

			if (character == 0) {
				output.endLine(false);
				return Token.END_OF_FILE;
			}
			if (Character.isUpperCase(character)) {
				while (Character.isLetter(character) ||
						Character.isDigit(character) || character == '_') {
					currentLexeme += character;
					character = nextChar();
				}
				currentToken = Token.UPPER_CASE_IDENTIFIER;
			} else if (Character.isLetter(character) || character == '_') {
				while (Character.isLetter(character) ||
						Character.isDigit(character) || character == '_') {
					currentLexeme += character;
					character = nextChar();
				}
				currentToken = testToken(currentLexeme);
			} else if (Character.isDigit(character)) {
				while (Character.isLetter(character) ||
						Character.isDigit(character) || character == '.') {
					currentLexeme += character;
					character = nextChar();
				}
				currentToken = Token.CONSTANT;
			} else if ((currentToken =
					testOperator()) != Token.NOT_FOUND)
				;
			else if ((currentToken =
					testSeparator()) != Token.NOT_FOUND)
				;
			else
				currentToken = Token.NOT_FOUND;
		}
		while (currentToken == Token.COMMENT ||
				currentToken == Token.COMPILER_DIRECTIVE);
		return currentToken;
	}

	//  Puts back the last token that was gotten.

	public void putLastToken() {
		lastToken = currentToken;
	}

	//  Returns the lexeme corresponding to the last token.

	public String getLastLexeme() {
		return lastLexeme;
	}

//  Returns the next character in the input buffer.
// i == char position
	private char nextChar() {
		try {
			if (line == null)
				return 0;
			if (i == line.length()) {
				line = file.readLine();
				i = 0;
				return '\n';
			}
			return line.charAt(i++);
		} catch (IOException exception) {
			return 0;
		}
	}

	// testOperator will return the token type if it is an operator.
	//   Otherwise, it returns NOT_FOUND. Spacing is set for some
	//   operators. Comments are ignored.

	private Token testOperator() {
		char lastCharacter;

		switch (character) {
			case '+':
				currentLexeme += character;
				character = nextChar();
				if (character == '=') {
					currentLexeme += character;
					character = nextChar();
					return Token.ASSIGNMENT_OPERATOR;
				} else if (character == '+') {
					currentLexeme += character;
					character = nextChar();
					return Token.PRE_OR_POST_UNARY_OPERATOR;
				} else
					return Token.UNARY_OR_BINARY_OPERATOR;
			case '-':
				currentLexeme += character;
				character = nextChar();
				if (character == '=') {
					currentLexeme += character;
					character = nextChar();
					return Token.ASSIGNMENT_OPERATOR;
				} else if (character == '-') {
					currentLexeme += character;
					character = nextChar();
					return Token.PRE_OR_POST_UNARY_OPERATOR;
				} else if (character == '>') {
					currentLexeme += character;
					character = nextChar();
					spacing = SUPPRESS_TRAILING_SPACE |
							SUPPRESS_LEADING_SPACE;
					return Token.STRUCTURE_OPERATOR;
				} else
					return Token.UNARY_OR_BINARY_OPERATOR;
			case '*':
				currentLexeme += character;
				character = nextChar();
				if (character == '=') {
					currentLexeme += character;
					character = nextChar();
					return Token.ASSIGNMENT_OPERATOR;
				} else
					return Token.UNARY_OR_BINARY_OPERATOR;
			case '%':
				currentLexeme += character;
				character = nextChar();
				if (character == '=') {
					currentLexeme += character;
					character = nextChar();
					return Token.ASSIGNMENT_OPERATOR;
				} else
					return Token.BINARY_OPERATOR;
			case '>':
				currentLexeme += character;
				character = nextChar();
				if (character == '=') {
					currentLexeme += character;
					character = nextChar();
					return Token.BINARY_OPERATOR;
				} else if (character == '>') {
					currentLexeme += character;
					character = nextChar();
					if (character == '=') {
						currentLexeme += character;
						character = nextChar();
						return Token.ASSIGNMENT_OPERATOR;
					} else
						return Token.BINARY_OPERATOR;
				} else
					return Token.BINARY_OPERATOR;
			case '<':
				currentLexeme += character;
				character = nextChar();
				if (character == '=') {
					currentLexeme += character;
					character = nextChar();
					return Token.BINARY_OPERATOR;
				} else if (character == '<') {
					currentLexeme += character;
					character = nextChar();
					if (character == '=') {
						currentLexeme += character;
						character = nextChar();
						return Token.ASSIGNMENT_OPERATOR;
					} else
						return Token.BINARY_OPERATOR;
				} else
					return Token.BINARY_OPERATOR;
			case '&':
				currentLexeme += character;
				character = nextChar();
				if (character == '=') {
					currentLexeme += character;
					character = nextChar();
					return Token.ASSIGNMENT_OPERATOR;
				} else if (character == '&') {
					currentLexeme += character;
					character = nextChar();
					return Token.BINARY_OPERATOR;
				} else
					return Token.UNARY_OR_BINARY_OPERATOR;
			case '|':
				currentLexeme += character;
				character = nextChar();
				if (character == '=') {
					currentLexeme += character;
					character = nextChar();
					return Token.ASSIGNMENT_OPERATOR;
				} else if (character == '|') {
					currentLexeme += character;
					character = nextChar();
					return Token.BINARY_OPERATOR;
				} else
					return Token.BINARY_OPERATOR;
			case '=':
				currentLexeme += character;
				character = nextChar();
				if (character == '=') {
					currentLexeme += character;
					character = nextChar();
					return Token.BINARY_OPERATOR;
				} else
					return Token.ASSIGNMENT_OPERATOR;
			case '!':
				currentLexeme += character;
				character = nextChar();
				if (character == '=') {
					currentLexeme += character;
					character = nextChar();
					return Token.BINARY_OPERATOR;
				} else {
					spacing = SUPPRESS_TRAILING_SPACE;
					return Token.UNARY_OPERATOR;
				}
			case '/':
				currentLexeme += character;
				character = nextChar();
				if (character == '=') {
					currentLexeme += character;
					character = nextChar();
					return Token.ASSIGNMENT_OPERATOR;
				}
				if (character == '*') {
					currentLexeme += character;
					character = nextChar();
					do {
						lastCharacter = character;
						character = nextChar();
					}
					while (character != '/' || lastCharacter != '*');
					character = nextChar();
					return Token.COMMENT;
				} else
					return Token.BINARY_OPERATOR;
			case '~':
				currentLexeme += character;
				character = nextChar();
				spacing = SUPPRESS_TRAILING_SPACE;
				return Token.UNARY_OPERATOR;
			case '.':
				currentLexeme += character;
				character = nextChar();
				spacing = SUPPRESS_TRAILING_SPACE |
						SUPPRESS_LEADING_SPACE;
				return Token.STRUCTURE_OPERATOR;
			case '?':
				currentLexeme += character;
				character = nextChar();
				return Token.TERNARY_OPERATOR;
			default:
				return Token.NOT_FOUND;
		}
	}

	// testSeparator will return the token type if it is a separator,
	//   otherwise, it returns NOT_FOUND. Compiler directives are
	//   printed out as they are found.

	private Token testSeparator() {
		switch (character) {
			case ':':
				currentLexeme += character;
				character = nextChar();
				return Token.COLON;
			case '(':
				currentLexeme += character;
				character = nextChar();
				spacing = SUPPRESS_TRAILING_SPACE;
				return Token.LEFT_PARENTHESIS;
			case ')':
				currentLexeme += character;
				character = nextChar();
				spacing = SUPPRESS_LEADING_SPACE;
				return Token.RIGHT_PARENTHESIS;
			case '[':
				currentLexeme += character;
				character = nextChar();
				spacing = SUPPRESS_LEADING_SPACE | SUPPRESS_TRAILING_SPACE;
				return Token.LEFT_BRACKET;
			case ']':
				currentLexeme += character;
				character = nextChar();
				spacing = SUPPRESS_LEADING_SPACE;
				return Token.RIGHT_BRACKET;
			case '{':
				currentLexeme += character;
				character = nextChar();
				spacing = SUPPRESS_TRAILING_SPACE;
				return Token.LEFT_BRACE;
			case '}':
				currentLexeme += character;
				character = nextChar();
				spacing = SUPPRESS_LEADING_SPACE;
				return Token.RIGHT_BRACE;
			case ';':
				currentLexeme += character;
				character = nextChar();
				spacing = SUPPRESS_LEADING_SPACE;
				return Token.SEMICOLON;
			case ',':
				currentLexeme += character;
				character = nextChar();
				spacing = SUPPRESS_LEADING_SPACE;
				return Token.COMMA;
			case '#':
				while (character != '\n' && character != 0) {
					currentLexeme += character;
					character = nextChar();
				}
				output.endLine(false);
				output.outputDirective(currentLexeme);
				return Token.COMPILER_DIRECTIVE;
			case '\'':
				currentLexeme += character;
				character = nextChar();
				while (character != '\'') {
					if (character == '\\') {
						currentLexeme += character;
						character = nextChar();
					}
					currentLexeme += character;
					character = nextChar();
				}
				currentLexeme += character;
				character = nextChar();
				return Token.CONSTANT;
			case '"':
				currentLexeme += character;
				character = nextChar();
				while (character != '"') {
					if (character == '\\') {
						currentLexeme += character;
						character = nextChar();
					}
					currentLexeme += character;
					character = nextChar();
				}
				currentLexeme += character;
				character = nextChar();
				return Token.STRING;
			default:
				return Token.NOT_FOUND;
		}
	}

	// testToken will return the token type if it is a token.
	//  Otherwise,it returns IDENTIFIER.

	private Token testToken(String lexeme) {
		switch (lexeme.charAt(0)) {
			case 'a':
				if (lexeme.equals("auto"))
					return Token.SC_SPECIFIER;
				else
					return Token.IDENTIFIER;
			case 'b':
				if (lexeme.equals("break"))
					return Token.BREAK;
				else
					return Token.IDENTIFIER;
			case 'c':
				if (lexeme.equals("case"))
					return Token.CASE;
				else if (lexeme.equals("char"))
					return Token.TYPE_SPECIFIER;
				else if (lexeme.equals("continue"))
					return Token.CONTINUE;
				else
					return Token.IDENTIFIER;
			case 'd':
				if (lexeme.equals("default"))
					return Token.DEFAULT;
				else if (lexeme.equals("double"))
					return Token.TYPE_SPECIFIER;
				else if (lexeme.equals("do"))
					return Token.DO;
				else
					return Token.IDENTIFIER;
			case 'e':
				if (lexeme.equals("else"))
					return Token.ELSE;
				else if (lexeme.equals("entry"))
					return Token.SC_SPECIFIER;
				else if (lexeme.equals("extern"))
					return Token.SC_SPECIFIER;
				else
					return Token.IDENTIFIER;
			case 'f':
				if (lexeme.equals("for"))
					return Token.FOR;
				else if (lexeme.equals("float"))
					return Token.TYPE_SPECIFIER;
				else
					return Token.IDENTIFIER;
			case 'g':
				if (lexeme.equals("goto"))
					return Token.GOTO;
				else
					return Token.IDENTIFIER;
			case 'i':
				if (lexeme.equals("if"))
					return Token.IF;
				else if (lexeme.equals("int"))
					return Token.TYPE_SPECIFIER;
				else
					return Token.IDENTIFIER;
			case 'l':
				if (lexeme.equals("long"))
					return Token.TYPE_SPECIFIER;
				else
					return Token.IDENTIFIER;
			case 'r':
				if (lexeme.equals("register"))
					return Token.SC_SPECIFIER;
				else if (lexeme.equals("return"))
					return Token.RETURN;
				else
					return Token.IDENTIFIER;
			case 's':
				if (lexeme.equals("short"))
					return Token.TYPE_SPECIFIER;
				else if (lexeme.equals("sizeof"))
					return Token.SIZEOF;
				else if (lexeme.equals("static"))
					return Token.SC_SPECIFIER;
				else if (lexeme.equals("status"))
					return Token.STATUS;
				else if (lexeme.equals("struct"))
					return Token.STRUCT;
				else if (lexeme.equals("switch"))
					return Token.SWITCH;
				else
					return Token.IDENTIFIER;
			case 't':
				if (lexeme.equals("typedef"))
					return Token.SC_SPECIFIER;
				else
					return Token.IDENTIFIER;
			case 'u':
				if (lexeme.equals("union"))
					return Token.UNION;
				else if (lexeme.equals("unsigned"))
					return Token.TYPE_SPECIFIER;
				else
					return Token.IDENTIFIER;
			case 'v':
				if (lexeme.equals("void"))
					return Token.TYPE_SPECIFIER;
				else
					return Token.IDENTIFIER;
			case 'w':
				if (lexeme.equals("while"))
					return Token.WHILE;
				else
					return Token.IDENTIFIER;
			default:
				return Token.IDENTIFIER;
		}
	}
}
