package de.jeisfeld.coachat.client;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONArray;
import org.json.JSONObject;

public class DialogToJsonFileConverter1 {

	public static void main(final String[] args) {
		String inputFilePath = "D:\\Downloads\\dialog1.txt"; // Replace with the path to your input file
		String outputFilePath = "D:\\Downloads\\dialog1.json"; // Replace with the path to your output file

		try {
			String[] dialog = readDialogFromFile(inputFilePath);
			String jsonOutput = convertDialogToJson(dialog);
			writeJsonToFile(jsonOutput, outputFilePath);
			System.out.println("Conversion completed. Output saved to: " + outputFilePath);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String[] readDialogFromFile(final String filePath) throws IOException {
		return Files.readAllLines(Paths.get(filePath)).toArray(new String[0]);
	}

	private static String convertDialogToJson(final String[] dialog) {
		JSONArray jsonArray = new JSONArray();

		for (String line : dialog) {
			if (line.trim().isEmpty()) {
				continue; // Skip empty lines
			}

			JSONObject jsonEntry = new JSONObject();
			if (line.startsWith("User:")) {
				jsonEntry.put("role", "user");
				jsonEntry.put("content", line.substring(6).trim()); // Remove "User: "
			}
			else if (line.startsWith("AI:")) {
				jsonEntry.put("role", "assistant");
				jsonEntry.put("content", line.substring(4).trim()); // Remove "AI: "
			}

			jsonArray.put(jsonEntry);
		}

		return jsonArray.toString(4); // '4' is for pretty-printing
	}

	private static void writeJsonToFile(final String json, final String filePath) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
			writer.write(json);
		}
	}
}
