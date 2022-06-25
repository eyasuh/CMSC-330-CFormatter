
// The Format class contains the methods necessary for formatting a
//   C program. Only the file method is public. Recursive descent
//   parsing is used to parse the program and perform the formatting.

public class Format {
	private Lexer lexer;
	private Output output;
	private Token token;

	//  The constructor establishes the input lexer and the output
	//    private data members.

	public Format(Lexer lexer, Output output) {
		this.lexer = lexer;
		this.output = output;
	}

	//  file is the only public method. External
	//    declarations are formatted until a function is found, at
	//    which time functionBody is called to format it.

	public void file() {
		token = lexer.getNextToken();
		while (token != Token.END_OF_FILE)
			if (externalDeclaration())
				functionBody();
	}

	//  functionBody formats the declarations and statements in a
	//    function body.

	private void functionBody() {
		output.endLine(true);
		while (token == Token.TYPE_SPECIFIER ||
				token == Token.SC_SPECIFIER ||
				token == Token.STRUCT || token == Token.UNION ||
				token == Token.UPPER_CASE_IDENTIFIER)
			parameterDeclaration();
		output.indent();
		output.endLine(false);
		output.skipLine();
		compoundStatement();
		output.unindent();
		output.endLine(false);
		output.endPage();
	}

	// compoundStatement formats a multiple statement block

	private void compoundStatement() {
		int noOfDeclarations = 0;
		token = lexer.getNextToken();
		output.endLine(false);
		while (token == Token.TYPE_SPECIFIER ||
				token == Token.SC_SPECIFIER ||
				token == Token.STRUCT || token == Token.UNION ||
				token == Token.UPPER_CASE_IDENTIFIER) {
			declaration();
			noOfDeclarations++;
		}
		if (noOfDeclarations > 0)
			output.skipLine();
		while (token != Token.RIGHT_BRACE)
			statement();
		token = lexer.getNextToken();
		output.endLine(false);
	}

	//  statement determines the type of statement and calls the
	//    appropriate function to format it.

	private void statement() {
		if (token == Token.IDENTIFIER)
			if ((token = lexer.getNextToken()) == Token.COLON)
				token = lexer.getNextToken();
			else {
				token = Token.IDENTIFIER;
				lexer.putLastToken();
			}
		switch (token) {
			case LEFT_BRACE:
				compoundStatement();
				break;
			case SWITCH:
				switchStatement();
				break;
			case BREAK:
			case CONTINUE:
				verifyNextToken(Token.SEMICOLON);
				output.endLine(false);
				break;
			case RETURN:
				if ((token = lexer.getNextToken()) != Token.SEMICOLON) {
					expression(Token.SEMICOLON);
					token = lexer.getNextToken();
				} else
					token = lexer.getNextToken();
				output.endLine(false);
				break;
			case GOTO:
				verifyNextToken(Token.IDENTIFIER);
				verifyNextToken(Token.SEMICOLON);
				output.endLine(false);
				break;
			default:
				expression(Token.SEMICOLON);
				token = lexer.getNextToken();
				output.endLine(false);
		}
	}

	//  switchStatement formats a switch statement.

	private void switchStatement() {
		verifyNextToken(Token.LEFT_PARENTHESIS);
		expression(Token.RIGHT_PARENTHESIS);
		token = lexer.getNextToken();
		output.endLine(false);
		output.indent();
		verifyCurrentToken(Token.LEFT_BRACE);
		output.endLine(false);
		while (token == Token.CASE || token == Token.DEFAULT) {
			if (token == Token.CASE) {
				expression(Token.COLON);
				lexer.adjustSpacing(Lexer.SUPPRESS_LEADING_SPACE);
				token = lexer.getNextToken();
				output.endLine(false);
				output.indent();
				while (token != Token.CASE && token !=
						Token.DEFAULT && token != Token.RIGHT_BRACE)
					statement();
				output.unindent();
			} else {
				expression(Token.COLON);
				lexer.adjustSpacing(Lexer.SUPPRESS_LEADING_SPACE);
				token = lexer.getNextToken();
				output.endLine(false);
				output.indent();
				while (token != Token.CASE && token != Token.DEFAULT &&
						token != Token.RIGHT_BRACE)
					statement();
				output.unindent();
			}
		}
		verifyCurrentToken(Token.RIGHT_BRACE);
		output.endLine(false);
		output.unindent();
	}

	// externalDeclarations formats external declarations such as
	//   global variables and function prototypes. It returns if
	//   it encounters a function heading.

	private boolean externalDeclaration() {
		int braceCount = 0;
		boolean indentAtSemicolon = false;
		Token lastToken = Token.NOT_FOUND;
		while ((braceCount > 0) || (token != Token.SEMICOLON)) {
			lexer.checkDeclarationSpacing(token);
			if (token == Token.LEFT_BRACE) {
				output.endLine(false);
				output.indent();
				lastToken = token;
				token = lexer.getNextToken();
				output.endLine(false);
				braceCount++;
			} else if (token == Token.RIGHT_BRACE) {
				lastToken = token;
				token = lexer.getNextToken();
				indentAtSemicolon = true;
				braceCount--;
			} else if (token == Token.LEFT_PARENTHESIS) {
				lastToken = token;
				token = lexer.getNextToken();
			} else if (token == Token.RIGHT_PARENTHESIS) {
				lastToken = token;
				token = lexer.getNextToken();
				if (token != Token.SEMICOLON)
					return true;
			} else if (token == Token.ASSIGNMENT_OPERATOR)
				while (token != Token.SEMICOLON) {
					lastToken = token;
					token = lexer.getNextToken();
					lexer.checkExpressionSpacing(token, lastToken);
				}
			else if (token == Token.SEMICOLON) {
				lastToken = token;
				token = lexer.getNextToken();
				if (braceCount > 0)
					output.endLine(false);
				if (indentAtSemicolon) {
					output.indent();
					indentAtSemicolon = false;
				}
			} else {
				lastToken = token;
				token = lexer.getNextToken();
			}
		}

		token = lexer.getNextToken();
		output.endLine(false);
		if (indentAtSemicolon)
			output.indent();
		return false;
	}

	//  parameterDeclaration formats parameter declarations.

	private void parameterDeclaration() {
		int braceCount = 0;
		while ((braceCount > 0) || (token != Token.SEMICOLON)) {
			lexer.checkDeclarationSpacing(token);
			if (token == Token.LEFT_BRACE) {
				output.endLine(false);
				output.indent();
				token = lexer.getNextToken();
				output.endLine(false);
				braceCount++;
			} else if (token == Token.RIGHT_BRACE) {
				token = lexer.getNextToken();
				output.indent();
				braceCount--;
			} else if ((braceCount > 0) && (token == Token.SEMICOLON)) {
				token = lexer.getNextToken();
				output.endLine(false);
			} else
				token = lexer.getNextToken();
		}
		token = lexer.getNextToken();
		output.endLine(false);
	}

	// declaration formats local declarations.

	private void declaration() {
		int braceCount = 0;
		boolean indentAtSemicolon = false;

		while ((braceCount > 0) || (token != Token.SEMICOLON)) {
			lexer.checkDeclarationSpacing(token);
			if (token == Token.LEFT_BRACE) {
				output.endLine(false);
				output.indent();
				token = lexer.getNextToken();
				output.endLine(false);
				braceCount++;
			} else if (token == Token.RIGHT_BRACE) {
				token = lexer.getNextToken();
				indentAtSemicolon = true;
				braceCount--;
			} else if (token == Token.SEMICOLON) {
				token = lexer.getNextToken();
				if (braceCount > 0)
					output.endLine(false);
				if (indentAtSemicolon) {
					output.indent();
					indentAtSemicolon = false;
				}
			} else if (token == Token.ASSIGNMENT_OPERATOR)
				expression(Token.SEMICOLON);
			else
				token = lexer.getNextToken();
		}
		token = lexer.getNextToken();
		output.endLine(false);
		if (indentAtSemicolon)
			output.indent();
	}

	//  expression formats an expression. The delimiting token must
	//    be provided.

	private void expression(Token terminator) {
		Token lastToken;

		lastToken = Token.NOT_FOUND;
		while (token != terminator) {
			lexer.checkExpressionSpacing(token, lastToken);

			if (token == Token.LEFT_PARENTHESIS) {
				if (lastToken == Token.IDENTIFIER ||
						lastToken == Token.UPPER_CASE_IDENTIFIER)
					lexer.adjustSpacing(Lexer.SUPPRESS_LEADING_SPACE);
				token = lexer.getNextToken();
				expression(Token.RIGHT_PARENTHESIS);
			}
			lastToken = token;
			token = lexer.getNextToken();
		}
	}

	// Gets the next token and then verifies that the supplied token is
	//   the required token.

	private void verifyNextToken(Token requiredToken) {
		token = lexer.getNextToken();
		verifyCurrentToken(requiredToken);
	}

	// Verifies that the supplied token is the current token.
	// Displays an error message if it is not.

	private void verifyCurrentToken(Token requiredToken) {
		if (token != requiredToken)
			output.outputError("MISSING " + requiredToken.name());
		else
			token = lexer.getNextToken();

	}
}