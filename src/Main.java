import java.io.*;

public class Main {

	//  The main function for the C formatter program.  It creates
	//     the three primary objects, an output object, a lexer object,
	//     and a formatter object. It then calls the file method of the
	//     formatter object to perform the formatting.

	private static final BufferedReader stdin =
			new BufferedReader(new InputStreamReader(System.in));

	public static void main(String[] args) throws IOException {
		String fileName;
		System.out.print("Enter file name without .c: ");
		fileName = stdin.readLine();
		Output output = new Output(fileName);
		Lexer lexer = new Lexer(fileName, output);
		Format format = new Format(lexer, output);

		format.file();
		lexer.close();
		output.close();
	}
}