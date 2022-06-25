// The Output class controls the formation of lines and pages in the
//   output. It also provides explicit functions for controlling the
//   indentation and forcing new lines and pages.

import java.io.*;

public class Output {
	private static final int INDENT_INCREMENT = 4, LEFT_MARGIN = 0,
			LINES_PER_PAGE = 56, HEADING_LENGTH = 70,
			CHARACTERS_PER_LINE = 78;
	private PrintWriter file;
	private int linesOnPage;
	private int pageNumber;
	private int indentation;
	private String buffer = "";
	private String heading;

	// The constructor initializes the private instance variables.
	//   It constructs a page heading containing the input file name.

	public Output(String fileName)
			throws FileNotFoundException, IOException {
		file = new PrintWriter(new FileWriter(fileName + "_.c"));
		linesOnPage = LINES_PER_PAGE;
		pageNumber = 1;
		indentation = LEFT_MARGIN;
		heading = fileName;
		for (int i = 0; i < HEADING_LENGTH - fileName.length(); i++)
			heading += ' ';
	}

	// Closes the output file.

	public void close() {
		file.close();
	}

	// outputToken outputs the token string, adjusting spacing
	//   specified by the spacing word.

	public void outputToken(String token, int spacing) {
		if (buffer.length() + token.length() > CHARACTERS_PER_LINE) {
			outputLine(buffer);
			buffer = "";
		}
		if ((spacing & Lexer.SUPPRESS_LEADING_SPACE) != 0)
			if (buffer.length() > 0 &&
					buffer.charAt(buffer.length() - 1) == ' ')
				buffer = buffer.substring(0, buffer.length() - 1);
		buffer += token;
		if ((spacing & Lexer.SUPPRESS_TRAILING_SPACE) == 0)
			buffer += ' ';
	}

	// outputDirective prints out a compiler directive starting at the
	//   left margin.

	public void outputDirective(String directive) {
		outputLine(directive);
	}

	// outputError prints out error messages.

	public void outputError(String error) {
		file.println(error);
	}

	// indent increments the indentation variable.

	public void indent() {
		indentation += INDENT_INCREMENT;
	}

	// unindent decrements the indentation variable.

	public void unindent() {
		indentation -= INDENT_INCREMENT;
	}

	// endLine calls outputLine to write out a line. If the parameter
	//   forceNewPage is true, then newPage is called.

	public void endLine(boolean forceNewPage) {
		if (forceNewPage && (linesOnPage > 0))
			newPage();
		if (buffer != "")
			outputLine(buffer);
		buffer = "";
	}

	// skipLine skips a line.

	public void skipLine() {
		outputLine("");
	}

	// endPage sets linesOnPage to force a call to newPage.

	public void endPage() {
		linesOnPage = LINES_PER_PAGE;
	}

	// newPage does a form feed and prints a new page heading.

	public void newPage() {
		file.println("\f" + heading + "PAGE " + pageNumber++);
		linesOnPage = 0;
	}

	// outputLine fills up the number of spaces in the margin
	//   and prints a line from the buffer.
	//   It then increments lines per page.

	public void outputLine(String line) {
		String buffer = "";

		if (linesOnPage >= LINES_PER_PAGE)
			newPage();
		for (int space = 0; space < LEFT_MARGIN + indentation; space++)
			buffer += ' ';
		buffer += line;
		file.println(buffer);
		linesOnPage++;
	}
}