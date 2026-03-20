package ie.atu.sw;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Runner {

	public static void main(String[] args) throws Exception {

		System.out.println(ConsoleColour.BLUE);
		System.out.println("************************************************************");
		System.out.println("*     ATU - Dept. of Computer Science & Applied Physics    *");
		System.out.println("*                                                          *");
		System.out.println("*          Similarity Search with Word Embeddings          *");
		System.out.println("*                                                          *");
		System.out.println("************************************************************");

		Scanner scanner = new Scanner(System.in);
		Runner runner = new Runner();

		System.out.println("(1) Enter the path to the embeddings file:	  ");
		String embeddingsFilePath = scanner.nextLine();
		System.out.println("----------------------------------------------");

		System.out.println("(2) Enter the word to compare:				  ");
		String inputWord = scanner.nextLine();
		System.out.println("--------------------------------------------- ");

		System.out.println("(3) Enter the number of top matches to report:");
		int topN = scanner.nextInt();
		scanner.nextLine();
		System.out.println("----------------------------------------------");

		System.out.println("(4) Enter the path for the output file:		  ");
		String outputFilePath = scanner.nextLine();
		System.out.println("----------------------------------------------");

		try {
			runner.parseFile(embeddingsFilePath);
			runner.findMostSimilarWords(inputWord, topN, outputFilePath);
			System.out.println(ConsoleColour.GREEN_BOLD + "File successfully parsed!" + ConsoleColour.RESET);

		} catch (IOException e) {
			System.out.println(
					ConsoleColour.RED_BOLD + "An error occurred while reading the file." + ConsoleColour.RESET);
			e.printStackTrace();
		}

		scanner.close();

	
		int size = 100;
		for (int i = 0; i < size; i++) { // The loop equates to a sequence of processing steps
			printProgress(i + 1, size); // After each (some) steps, update the progress meter
			Thread.sleep(10); // Slows things down so the animation is visible
			System.out.print(ConsoleColour.YELLOW);
		}
	}

	public static void printProgress(int index, int total) {
		if (index > total)
			return; // Out of range
		int size = 50;
		char done = '█';
		char todo = '░';

		int complete = (100 * index) / total;
		int completeLen = size * complete / 100;

		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < size; i++) {
			sb.append((i < completeLen) ? done : todo);
		}

		System.out.print("\r" + sb + "] " + complete + "%");

		if (done == total)
			System.out.println("\n");
	}

	private String[] words;
	private double[][] embeddings;

	public void parseFile(String filePath) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		String line;
		int index = 0;

		words = new String[59602]; // Fixed-size array for words
		embeddings = new double[59602][50]; // 2D array for embedding

		while ((line = br.readLine()) != null && index < 59602) {

			String[] parts = line.split("[ ,]+");

			// First element is the word
			words[index] = parts[0];

			// Next 50 elements are the embeddings
			for (int i = 1; i <= 50; i++) {
				embeddings[index][i - 1] = Double.parseDouble(parts[i]);
			}
			index++;
		}
			br.close();
	}

	private double cosineSimilarity(double[] vectorA, double[] vectorB) {
		double dotProduct = 0.0;
		double normA = 0.0;
		double normB = 0.0;

		for (int i = 0; i < vectorA.length; i++) {
			dotProduct += vectorA[i] * vectorB[i];
			normA += Math.pow(vectorA[i], 2);
			normB += Math.pow(vectorB[i], 2);
		}
		return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	}

	// Implementation for finding the most similar words.
	public void findMostSimilarWords(String inputWord, int topN, String outputFile) throws IOException {
		double[] inputEmbedding = null;
		int inputIndex = -1;

		for (int i = 0; i < words.length; i++) {
			if (words[i].equalsIgnoreCase(inputWord)) {
				inputEmbedding = embeddings[i];
				inputIndex = i;
				break;
			}
		}

		if (inputEmbedding == null) {
			System.out.println(ConsoleColour.RED_BOLD + "Word not found in the embeddings file!" + ConsoleColour.RESET);
			return;
		}

		double[] similarities = new double[words.length];
		for (int i = 0; i < words.length; i++) {
			similarities[i] = cosineSimilarity(inputEmbedding, embeddings[i]);
		}

		// Find top N matches by sorting (not using lists)
		for (int i = 0; i < topN; i++) {
			int maxIndex = i;
			for (int j = i + 1; j < similarities.length; j++) {
				if (similarities[j] > similarities[maxIndex]) {
					maxIndex = j;
				}
			}
			// Swap the similarities and words
			double tempSimilarity = similarities[i];
			similarities[i] = similarities[maxIndex];
			similarities[maxIndex] = tempSimilarity;

			String tempWord = words[i];
			words[i] = words[maxIndex];
			words[maxIndex] = tempWord;
		}

		// Write top N results to the output file
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
		for (int i = 0; i < topN; i++) {
			bw.write(words[i] + " : " + similarities[i]);
			bw.newLine();
		}
		bw.close();
	}
}

