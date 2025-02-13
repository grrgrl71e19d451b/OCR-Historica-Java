package it.unicam.cs.pg.processing;

import javafx.scene.image.WritableImage;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * Classe di utilità per l'elaborazione e conversione di immagini tra diversi formati.
 * Supporta operazioni con OpenCV, JavaFX e AWT BufferedImage.
 */
public class ImageUtils {

    // Caricamento della libreria nativa OpenCV
    static {
        System.setProperty("java.library.path", "C:\\opencv\\build\\java\\x64");
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * Carica un'immagine dal filesystem in scala di grigi.
     *
     * @param path Percorso assoluto del file immagine
     * @return Matrice OpenCV in formato scala di grigi
     */
    public static Mat loadImage(String path) {
        return Imgcodecs.imread(path, Imgcodecs.IMREAD_GRAYSCALE);
    }

    /**
     * Converte una matrice OpenCV in un'immagine BufferedImage di AWT.
     *
     * @param image Matrice OpenCV da convertire
     * @return Immagine BufferedImage nel formato appropriato (scala di grigi o BGR)
     */
    public static BufferedImage matToBufferedImage(Mat image) {
        int type = (image.channels() > 1) ? BufferedImage.TYPE_3BYTE_BGR : BufferedImage.TYPE_BYTE_GRAY;

        byte[] buffer = new byte[image.channels() * image.cols() * image.rows()];
        image.get(0, 0, buffer);

        BufferedImage bufferedImage = new BufferedImage(image.cols(), image.rows(), type);
        System.arraycopy(buffer, 0,
                ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData(),
                0,
                buffer.length);

        return bufferedImage;
    }

    /**
     * Converte un'immagine JavaFX in una matrice OpenCV (formato BGR).
     *
     * @param image Immagine JavaFX da convertire
     * @return Matrice OpenCV in formato BGR a 3 canali
     */
    public static Mat convertFXImageToMat(WritableImage image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        // Estrazione dei pixel in formato BGRA (4 canali)
        byte[] buffer = new byte[width * height * 4];
        image.getPixelReader().getPixels(0, 0, width, height,
                javafx.scene.image.PixelFormat.getByteBgraInstance(),
                buffer, 0, width * 4);

        // Creazione matrice OpenCV temporanea in BGRA
        Mat mat = new Mat(height, width, CvType.CV_8UC4);
        mat.put(0, 0, buffer);

        // Conversione a BGR rimuovendo il canale alpha
        Mat bgrMat = new Mat();
        Imgproc.cvtColor(mat, bgrMat, Imgproc.COLOR_BGRA2BGR);

        return bgrMat;
    }
}