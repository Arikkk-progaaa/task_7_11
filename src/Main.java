import util.ArrayUtils;
import util.CliUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

import static util.CliUtils.getModuleDirectory;
import static util.CliUtils.parseCmdArgs;

public class Main {

    // Подготовка к прогону через solution
    public static void processFiles(String inputFile, String outputFile) throws Exception {
        int[][] array = ArrayUtils.readIntArray2FromFile(inputFile);

        if (array == null || array.length == 0) {
            System.err.printf("Error: Unable to read valid array from file \"%s\"%n", inputFile);
            System.exit(70);
        }

        int[][] result = solution(array);
        ArrayUtils.writeArrayToFile(outputFile, result);

        System.out.printf("Processed file: Input = %s, Output = %s%n", inputFile, outputFile);
    }

    public static int[][] solution(int[][] array){
        if (array == null || array.length == 0) {
            return array;
        }

        int rows = array.length;
        int maxCols = 0;

        for (int[] ints : array) {
            maxCols = Math.max(maxCols, ints.length);
        }

        int[][] rotated = new int[maxCols][rows];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < array[i].length; j++) {
                rotated[j][rows - 1 - i] = array[i][j];
            }
        }

        return rotated;
    }

    public static void main(String[] args) {
        CliUtils.CmdParams params = parseCmdArgs(args);

        try {
            if (params.test) {
                runTests();
            } else if (params.window) {
                Locale.setDefault(Locale.ROOT);

                java.awt.EventQueue.invokeLater(() -> new Frame().setVisible(true));
            } else {
                processFiles(params.inputFile, params.outputFile);
            }
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    // Запуск тестов
    public static void runTests() throws Exception {
        String moduleDir = getModuleDirectory();
        String testsDir = moduleDir + File.separator + "src" + File.separator + "tests";

        File inputDir = new File(testsDir, "input");
        File outputDir = new File(testsDir, "output");
        File expectedDir = new File(testsDir, "expected");

        if (!inputDir.exists() || !inputDir.isDirectory()) {
            System.err.printf("Input directory \"%s\" does not exist or is not a directory%n", inputDir.getAbsolutePath());
            System.exit(70);
        }
        if (!expectedDir.exists() || !expectedDir.isDirectory()) {
            System.err.printf("Expected directory \"%s\" does not exist or is not a directory%n", expectedDir.getAbsolutePath());
            System.exit(70);
        }
        if (!outputDir.exists()) {
            if (!outputDir.mkdir()) {
                System.err.printf("Failed to create output directory \"%s\"%n", outputDir.getAbsolutePath());
                System.exit(70);
            }
        }

        File[] inputFiles = inputDir.listFiles((dir, name) -> name.endsWith(".txt"));

        if (inputFiles == null || inputFiles.length == 0) {
            System.err.printf("No input files found in directory \"%s\"%n", inputDir.getAbsolutePath());
            System.exit(70);
        }

        for (File inputFile : inputFiles) {
            String baseName = inputFile.getName().replace("input", "").replace(".txt", "");
            File expectedFile = new File(expectedDir, "expected" + baseName + ".txt");
            File outputFile = new File(outputDir, "output" + baseName + ".txt");

            if (!expectedFile.exists()) {
                System.err.printf("Expected file \"%s\" not found%n", expectedFile.getAbsolutePath());
                continue;
            }

            System.out.printf("Processing test: Input = %s, Expected = %s, Output = %s%n",
                    inputFile.getAbsolutePath(), expectedFile.getAbsolutePath(), outputFile.getAbsolutePath());

            processFiles(inputFile.getAbsolutePath(), outputFile.getAbsolutePath());

            if (compareFiles(outputFile, expectedFile)) {
                System.out.printf("Test %s passed%n", baseName);
            } else {
                System.err.printf("Test %s failed%n", baseName);
            }
        }
    }

    private static boolean compareFiles(File outputFile, File expectedFile) throws IOException {
        try (BufferedReader outputReader = new BufferedReader(new FileReader(outputFile));
             BufferedReader expectedReader = new BufferedReader(new FileReader(expectedFile))) {

            String outputLine;
            String expectedLine;

            while ((outputLine = outputReader.readLine()) != null) {
                expectedLine = expectedReader.readLine();

                if (!outputLine.equals(expectedLine)) {
                    return false;
                }
            }

            return expectedReader.readLine() == null;
        }
    }
}