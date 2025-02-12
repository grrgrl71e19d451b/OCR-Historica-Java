package it.unicam.cs.pg.gui;

import it.unicam.cs.pg.preprocessing.ImageProcessingTask;
import it.unicam.cs.pg.processing.ImageUtils;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.opencv.core.Mat;

import java.io.File;

/**
 * Main GUI application per il preprocessing ed elaborazione di immagini utilizzando DL4J e filtri OpenCV.
 * Permette il caricamento di un'immagine e di un modello DL4J e applica, in seguito, una serie di filtri.
 */
public class PreprocessingDl4jGui extends Application {

    private static final String PROJECT_ROOT = System.getProperty("user.dir");
    private static final String OUTPUT_NO_FILTER_DIR = "C:\\OCR-Historica-Java\\output_images\\background-rumor-remove";

    private TextArea areaLog;
    private ComboBox<String> processingTypeComboBox;
    private FileChooser fileChooser;

    private MultiLayerNetwork modello;
    private Mat originalImage;


    /**
     * Avvia l'applicazione JavaFX e inizializza l'interfaccia utente.
     *
     * @param primaryStage il palco principale dell'applicazione
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Preprocessing e Elaborazione Immagine con DL4J");

        fileChooser = new FileChooser();
        File projectRootDir = new File(PROJECT_ROOT);
        if (projectRootDir.exists() && projectRootDir.isDirectory()) {
            fileChooser.setInitialDirectory(projectRootDir);
        }

        // Pulsante per caricare l'immagine
        Button btnCaricaImmagine = new Button("Carica Immagine");
        btnCaricaImmagine.setPrefWidth(120);
        btnCaricaImmagine.setAlignment(Pos.CENTER_LEFT);

        // Pulsante per caricare il modello DL4J e label descrittiva
        Button btnCaricaModello = new Button("Carica Modello DL4J");
        btnCaricaModello.setPrefWidth(150);
        Label lblModelInfo = new Label("CNN pre-addestrata per correggere le immagini.");
        lblModelInfo.setWrapText(true);
        lblModelInfo.setMaxWidth(400);
        HBox modelHBox = new HBox(10, btnCaricaModello, lblModelInfo);

        // ComboBox per selezionare il filtro OpenCV e label descrittiva
        processingTypeComboBox = new ComboBox<>();
        processingTypeComboBox.getItems().addAll("Nessuno", "Adaptive Thresholding", "Edge Detection", "Filtro Mediano", "Filtro Gaussiano");
        processingTypeComboBox.getSelectionModel().selectFirst();
        processingTypeComboBox.setPrefWidth(150);
        Label lblPreprocessingInfo = new Label("Filtri OpenCV basati su trasformazioni matematiche.");
        lblPreprocessingInfo.setWrapText(true);
        lblPreprocessingInfo.setMaxWidth(400);
        HBox preprocessingHBox = new HBox(10, processingTypeComboBox, lblPreprocessingInfo);

        // Pulsante per elaborare l'immagine
        Button btnElaboraImmagine = new Button("Elabora Immagine");
        btnElaboraImmagine.setPrefWidth(150);

        // Area per il log
        areaLog = new TextArea();
        areaLog.setEditable(false);
        areaLog.setWrapText(true);
        areaLog.setPrefHeight(150);
        areaLog.setPrefWidth(580);
        areaLog.setMaxWidth(580);

        // Pulsante per cancellare le immagini di output
        Button btnClearOutput = new Button("Cancella Output Immagini");
        btnClearOutput.setPrefWidth(150);

        // Configurazione del layout con GridPane
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(10);
        grid.setHgap(10);
        grid.add(btnCaricaImmagine, 0, 0, 2, 1);
        grid.add(modelHBox, 0, 1, 2, 1);
        grid.add(preprocessingHBox, 0, 2, 2, 1);
        grid.add(btnElaboraImmagine, 0, 3, 2, 1);
        grid.add(areaLog, 0, 4, 2, 1);
        grid.add(btnClearOutput, 0, 5, 2, 1);

        // Imposta gli handler per i pulsanti
        btnCaricaModello.setOnAction(e -> caricaModello(primaryStage));
        btnCaricaImmagine.setOnAction(e -> caricaImmagine(primaryStage));
        btnElaboraImmagine.setOnAction(e -> elaboraImmagine());
        btnClearOutput.setOnAction(e -> clearOutputImages());

        Scene scene = new Scene(grid, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * Avvia il task di elaborazione dell'immagine e visualizza il risultato.
     * Se l'elaborazione va a buon fine, apre una nuova finestra con l'immagine elaborata.
     */
    private void elaboraImmagine() {
        String selectedFilter = processingTypeComboBox.getSelectionModel().getSelectedItem();
        if (modello == null && "Nessuno".equals(selectedFilter)) {
            areaLog.appendText("Caricare un modello oppure selezionare un filtro.\n");
            return;
        }
        if (originalImage == null || originalImage.empty()) {
            areaLog.appendText("Caricare prima un'immagine.\n");
            return;
        }

        ImageProcessingTask task = new ImageProcessingTask(modello, originalImage, selectedFilter);
        task.setOnSucceeded(e -> {
            String outputPathFinal = task.getValue();
            areaLog.appendText("Elaborazione completata. Apro l'immagine elaborata...\n");
            Stage stage = new Stage();
            stage.setTitle("Immagine Elaborata");
            ImageView iv = new ImageView(new Image(new File(outputPathFinal).toURI().toString()));
            ScrollPane sp = new ScrollPane(iv);
            Scene scene = new Scene(sp, 800, 600);
            stage.setScene(scene);
            stage.show();
        });
        task.setOnFailed(e -> areaLog.appendText("Errore durante l'elaborazione: " + task.getException().getMessage() + "\n"));
        new Thread(task).start();
    }

    /**
     * Apre un dialogo per caricare un modello DL4J.
     *
     * @param stage il riferimento al palco principale per il dialogo
     */
    private void caricaModello(Stage stage) {
        fileChooser.setInitialDirectory(new File(PROJECT_ROOT));
        fileChooser.setTitle("Seleziona Modello DL4J");
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Modelli DL4J", "*.zip"));
        File modelFile = fileChooser.showOpenDialog(stage);
        if (modelFile != null) {
            try {
                modello = MultiLayerNetwork.load(modelFile, true);
                areaLog.appendText("Modello caricato: " + modelFile.getName() + "\n");
            } catch (Exception ex) {
                areaLog.appendText("Errore nel caricamento del modello: " + ex.getMessage() + "\n");
            }
        }
    }

    /**
     * Apre un dialogo per caricare un'immagine.
     *
     * @param stage il riferimento al palco principale per il dialogo
     */
    private void caricaImmagine(Stage stage) {
        fileChooser.setInitialDirectory(new File(PROJECT_ROOT));
        fileChooser.setTitle("Seleziona Immagine");
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Immagini", "*.jpg", "*.png", "*.jpeg"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            originalImage = ImageUtils.loadImage(file.getAbsolutePath());
            if (originalImage.empty()) {
                areaLog.appendText("Errore nel caricamento dell'immagine.\n");
                return;
            }
            areaLog.appendText("Immagine caricata: " + file.getName() + "\n");
        }
    }

    /**
     * Elimina i file presenti nella cartella di output e registra il numero di file cancellati.
     */
    private void clearOutputImages() {
        File dir = new File(OUTPUT_NO_FILTER_DIR);
        int totalDeleted = 0;
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.delete()) {
                        totalDeleted++;
                    }
                }
            }
        }
        areaLog.appendText("Cancellate " + totalDeleted + " immagini dalla cartella di output.\n");
    }

    /**
     * Metodo principale per lanciare l'applicazione.
     *
     * @param args argomenti della linea di comando
     */
    public static void main(String[] args) {
        launch(args);
    }
}
