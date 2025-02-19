package it.unicam.cs.pg.preprocessing;

import it.unicam.cs.pg.modelDl4jTraining.ImagePreprocessor;
import javafx.concurrent.Task;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

/**
 * Task che esegue l'elaborazione di un'immagine utilizzando un modello DL4J e/o filtri OpenCV.
 * Restituisce il percorso del file immagine elaborato.
 */
public class ImageProcessingTask extends Task<String> {
    private final MultiLayerNetwork modello;
    private final Mat originalImage;
    private final String selectedFilter;
    private static final String PROJECT_ROOT = System.getProperty("user.dir");
    private static final String OUTPUT_ADAPTIVE_THRESHOLD_DIR = PROJECT_ROOT + "\\output_images\\adaptive-thresholding-results";
    private static final String OUTPUT_EDGE_DETECTION_DIR = PROJECT_ROOT + "\\output_images\\edge-detection-results";
    private static final String OUTPUT_MEDIAN_FILTER_DIR = PROJECT_ROOT + "\\output_images\\median-filter-results";
    private static final String OUTPUT_GAUSSIAN_FILTER_DIR = PROJECT_ROOT + "\\output_images\\gaussian-filter-results";
    private static final String OUTPUT_NO_FILTER_DIR = PROJECT_ROOT + "\\output_images\\background-rumor-remove";

    /**
     * Costruttore per il task di elaborazione immagine.
     *
     * @param modello        modello DL4J da utilizzare (può essere null)
     * @param originalImage  immagine di input in formato OpenCV Mat
     * @param selectedFilter nome del filtro OpenCV da applicare
     */
    public ImageProcessingTask(MultiLayerNetwork modello, Mat originalImage, String selectedFilter) {
        this.modello = modello;
        this.originalImage = originalImage;
        this.selectedFilter = selectedFilter;
    }

    /**
     * Esegue il task di elaborazione.
     * Se il modello DL4J è fornito, lo applica all'immagine ridimensionata e poi eventualmente applica un filtro OpenCV.
     *
     * @return il percorso del file immagine elaborato
     * @throws Exception in caso di errori durante l'elaborazione
     */
    @Override
    protected String call() throws Exception {
        // 1. Salva l'immagine originale in un file temporaneo
        File tempFile = File.createTempFile("temp_image", ".png");
        Imgcodecs.imwrite(tempFile.getAbsolutePath(), originalImage);

        // 2. Utilizza processAndSaveImage per ridimensionare l'immagine
        // Scegliamo come destinazione la cartella temporanea di sistema
        String tempOutputFolder = System.getProperty("java.io.tmpdir");
        ImagePreprocessor.processAndSaveImage(tempFile, tempOutputFolder, null);

        // 3. Carica l'immagine ridimensionata dal file
        // processAndSaveImage salva l'immagine con lo stesso nome del file originale
        File processedFile = new File(tempOutputFolder, tempFile.getName());
        Mat resizedImage = Imgcodecs.imread(processedFile.getAbsolutePath());
        if (resizedImage.empty()) {
            throw new RuntimeException("Impossibile caricare l'immagine ridimensionata: "
                    + processedFile.getAbsolutePath());
        }

        // 4. Se è presente un modello DL4J, usa l'immagine ridimensionata per il processing
        Mat baseImage;
        if (modello != null) {
            INDArray input = convertMatToINDArray(resizedImage);
            ImagePreProcessingScaler scaler = new ImagePreProcessingScaler(0, 1);
            scaler.transform(input);
            INDArray output = modello.output(input);
            baseImage = convertINDArrayToMat(output);
        } else {
            baseImage = resizedImage.clone();
        }

        // 5. Applica, se richiesto, il filtro OpenCV selezionato
        Mat finalOutputMat = baseImage.clone();
        if (!"Nessuno".equals(selectedFilter)) {
            switch (selectedFilter) {
                case "Adaptive Thresholding":
                    finalOutputMat = PreprocessingFilters.adaptiveThreshold(finalOutputMat);
                    break;
                case "Edge Detection":
                    finalOutputMat = PreprocessingFilters.edgeDetection(finalOutputMat);
                    break;
                case "Filtro Mediano":
                    finalOutputMat = PreprocessingFilters.medianFilter(finalOutputMat);
                    break;
                case "Filtro Gaussiano":
                    finalOutputMat = PreprocessingFilters.gaussianFilter(finalOutputMat);
                    break;
                default:
                    break;
            }
        }

        // 6. Determina il percorso di salvataggio finale in base al filtro applicato
        String outputPathFinal;
        if (!"Nessuno".equals(selectedFilter)) {
            outputPathFinal = switch (selectedFilter) {
                case "Adaptive Thresholding" -> ensureDirectoryExists(OUTPUT_ADAPTIVE_THRESHOLD_DIR)
                        + "\\processed_image_adaptive_thresholding.png";
                case "Edge Detection" -> ensureDirectoryExists(OUTPUT_EDGE_DETECTION_DIR)
                        + "\\processed_image_edge_detection.png";
                case "Filtro Mediano" -> ensureDirectoryExists(OUTPUT_MEDIAN_FILTER_DIR)
                        + "\\processed_image_median_filter.png";
                case "Filtro Gaussiano" -> ensureDirectoryExists(OUTPUT_GAUSSIAN_FILTER_DIR)
                        + "\\processed_image_gaussian_filter.png";
                default -> "output.png";
            };
        } else {
            if (modello != null) {
                outputPathFinal = ensureDirectoryExists(OUTPUT_NO_FILTER_DIR)
                        + "\\processed_image_background_rumor_remove.png";
            } else {
                outputPathFinal = ensureDirectoryExists(OUTPUT_NO_FILTER_DIR) + "\\output.png";
            }
        }

        // 7. Salva l'immagine finale e pulisci i file temporanei
        Imgcodecs.imwrite(outputPathFinal, finalOutputMat);
        tempFile.delete();
        processedFile.delete();

        return outputPathFinal;
    }


    /**
     * Converte una matrice OpenCV (Mat) in un INDArray.
     *
     * @param mat matrice OpenCV da convertire
     * @return INDArray convertito
     */
    private INDArray convertMatToINDArray(Mat mat) {
        int targetWidth = 1200;
        int targetHeight = 1700;
        NativeImageLoader loader = new NativeImageLoader(targetHeight, targetWidth, 3);
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".png", mat, matOfByte);
        byte[] byteArray = matOfByte.toArray();
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray)) {
            return loader.asMatrix(byteArrayInputStream);
        } catch (IOException e) {
            throw new RuntimeException("Errore nella conversione da Mat a INDArray: " + e.getMessage());
        }
    }

    /**
     * Converte un INDArray in una matrice OpenCV (Mat).
     *
     * @param output l'INDArray contenente l'immagine
     * @return la matrice OpenCV (Mat) convertita
     */
    private Mat convertINDArrayToMat(INDArray output) {
        int height = (int) output.size(2);
        int width = (int) output.size(3);
        int channels = (int) output.size(1);
        Mat mat = new Mat(height, width, channels == 1 ? org.opencv.core.CvType.CV_8UC1 : org.opencv.core.CvType.CV_8UC3);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (channels == 1) {
                    float v = output.getFloat(0, 0, y, x);
                    int pixel = Math.round(v * 255);
                    mat.put(y, x, pixel);
                } else if (channels == 3) {
                    float r = output.getFloat(0, 0, y, x);
                    float g = output.getFloat(0, 1, y, x);
                    float b = output.getFloat(0, 2, y, x);
                    int ir = Math.round(r * 255);
                    int ig = Math.round(g * 255);
                    int ib = Math.round(b * 255);
                    mat.put(y, x, ib, ig, ir); // OpenCV usa BGR
                }
            }
        }

        return mat;
    }

    /**
     * Verifica l'esistenza di una directory, creandola se necessario, e ne restituisce il percorso assoluto.
     *
     * @param dirPath il percorso della directory
     * @return il percorso assoluto della directory
     */
    private String ensureDirectoryExists(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir.getAbsolutePath();
    }
}