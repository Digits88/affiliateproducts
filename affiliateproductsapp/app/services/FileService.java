package services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileService {
	
	/**
	 * Split File functions are listed below
	 */

	public void split(File targetFile) {
	}

	/*public void splitToSmallFiles(File targetFile, int nol) {
		FileInputStream fstream = null;
		DataInputStream in = null;
		BufferedReader br = null;
		try {
			System.out
					.println("Split -- " + targetFile.getName() + " is Start");

			// Get the total Line number of file
			FileInputStream fs_countLine = new FileInputStream(targetFile);
			DataInputStream in_countLine = new DataInputStream(fs_countLine);
			BufferedReader br_countLine = new BufferedReader(
					new InputStreamReader(in_countLine));

			int count = 0;
			while (br_countLine.readLine() != null) {
				count++;
			}
			br_countLine.close();
			in_countLine.close();
			fs_countLine.close();

			// Log number of lines in the input file.
			System.out.println("Lines in the file: " + count);
			// logger.info(Log.message("Lines in the file: " + count));

			double temp = (count / nol);
			int temp1 = (int) temp;
			int nof = 0;
			if (temp1 == temp) {
				nof = temp1;
			} else {
				nof = temp1 + 1;
			}
			// Log number of files to be generated
			System.out.println("No. of files to be generated :" + nof);
			// logger.info(Log.message("No. of files to be generated :" + nof));

			// ---------------------------------------------------------------------------------------------------------

			// Actual splitting of file into smaller files

			fstream = new FileInputStream(targetFile);
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));

			String spliteFileName = targetFile.getAbsolutePath().replaceAll(
					"\\.txt$", "_");

			String strLine = null;

			for (int j = 1; j <= nof; j++) {
				FileWriter fstream1 = new FileWriter(spliteFileName + j
						+ ".txt"); // Destination File Location
				BufferedWriter out = new BufferedWriter(fstream1);
				for (int i = 1; i <= nol; i++) {
					strLine = br.readLine();
					if (strLine != null) {
						out.write(strLine);
						if (i != nol) {
							out.newLine();
						}
					}
				}
				out.close();
			}

			System.out.println("Split -- " + targetFile.getName() + " is done");
			// logger.info(Log.message("Split -- " + targetFile.getName() +
			// " is done"));
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			// logger.error(Log.message("COULD NOT download the file: " +
			// targetFile.getName()));
		} finally {
			try {
				if (br != null) {
					br.close();
					br = null;
				}
				if (in != null) {
					in.close();
					in = null;
				}
				if (fstream != null) {
					fstream.close();
					fstream = null;
				}
				
				 * if (targetFile.exists()) { targetFile.delete(); }
				 
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}*/
	
	/**
	 * Download Functions are listed here
	 */
}
