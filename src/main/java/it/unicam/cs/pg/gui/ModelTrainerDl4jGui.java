package it.unicam.cs.pg.gui;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import it.unicam.cs.pg.modelDl4jTraining.ImagePreprocessor;
import it.unicam.cs.pg.modelDl4jTraining.ModelArchitecture;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Applicazione GUI per il training di un modello DL4J per la rimozione di sfondo e rumore.
 * Gestisce la preparazione del dataset (processamento immagini), l’addestramento e il salvataggio del modello.
 */
public class ModelTrainerDl4jGui extends Application {
    static {
        // Carica la libreria nativa di OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    private static final String PROJECT_ROOT = System.getProperty("user.dir");
    private static final String DATASET_DIR = PROJECT_ROOT + File.separator + "dl4j dataset";
    private static final String PATH_ORIGINALI = DATASET_DIR + File.separator + "originali";
    private static final String PATH_MASCHE = DATASET_DIR + File.separator + "maschere";

    private TextArea areaLog;
    private ProgressBar barraProgresso;
    private MultiLayerNetwork modello;
    private TextField txtEpochs;

    /**
     * Punto d'ingresso principale dell'applicazione.
     *
     * @param args argomenti della riga di comando
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Metodo principale per avviare l'applicazione JavaFX.
     *
     * @param stagePrincipale la finestra principale dell'applicazione
     */
    @Override
    public void start(Stage stagePrincipale) {
        stagePrincipale.setTitle("Training - Rimozione Sfondo e Rumore");

        // Creazione delle cartelle del dataset se non esistono
        creaCartelleDataset();

        // Configurazione del layout principale
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(10);
        grid.setHgap(10);

        // Riga 0: Pulsante "Processa Immagini" con descrizione
        Button btnProcessaImmagini = new Button("Processa Immagini");
        btnProcessaImmagini.setPrefWidth(150);
        btnProcessaImmagini.setOnAction(e -> processaImmagini());
        Label lblProcessaImmagini = new Label("Preparazione delle immagini per il training: normalizzazione, ridimensionamento, ecc.");
        lblProcessaImmagini.setWrapText(true);
        lblProcessaImmagini.setMaxWidth(400);
        HBox processaHBox = new HBox(10, btnProcessaImmagini, lblProcessaImmagini);
        grid.add(processaHBox, 0, 0, 2, 1);

        // Riga 1: Pulsante "Addestra Nuovo Modello" con descrizione
        Button btnAddestraModello = new Button("Addestra Nuovo Modello");
        btnAddestraModello.setPrefWidth(150);
        btnAddestraModello.setOnAction(e -> addestraModello());
        Label lblAddestraModello = new Label("Addestramento supervisionato della rete CNN.");
        lblAddestraModello.setWrapText(true);
        lblAddestraModello.setMaxWidth(400);
        HBox addestraHBox = new HBox(10, btnAddestraModello, lblAddestraModello);
        grid.add(addestraHBox, 0, 1, 2, 1);

        // Riga 2: Campo per il numero di epoche
        Label lblEpochs = new Label("Numero di Epoche:");
        txtEpochs = new TextField("50");
        txtEpochs.setPrefWidth(50);
        HBox epochsHBox = new HBox(10, lblEpochs, txtEpochs);
        grid.add(epochsHBox, 0, 2, 2, 1);

        // Riga 3: Pulsante "Salva Modello DL4J"
        Button btnSalvaModello = new Button("Salva Modello DL4J");
        btnSalvaModello.setPrefWidth(150);
        btnSalvaModello.setOnAction(e -> salvaModello());
        grid.add(btnSalvaModello, 0, 3, 2, 1);

        // Riga 4: Area di log
        areaLog = new TextArea();
        areaLog.setEditable(false);
        areaLog.setWrapText(true);
        areaLog.setPrefHeight(150);
        grid.add(areaLog, 0, 4, 2, 1);

        // Riga 5: Barra di progresso
        barraProgresso = new ProgressBar(0);
        barraProgresso.setVisible(false);
        grid.add(barraProgresso, 0, 5, 2, 1);

        // Imposta la scena e mostra la finestra
        Scene scene = new Scene(grid, 600, 600);
        stagePrincipale.setScene(scene);
        stagePrincipale.setResizable(false);
        stagePrincipale.show();
    }

    /**
     * Crea le cartelle necessarie per il dataset se non esistono già.
     */
    private void creaCartelleDataset() {
        File dirDataset = new File(DATASET_DIR);
        if (!dirDataset.exists() && dirDataset.mkdirs()) {
            System.out.println("[INFO] Cartella dataset creata: " + dirDataset.getAbsolutePath());
        }
        File dirOriginali = new File(PATH_ORIGINALI);
        if (!dirOriginali.exists() && dirOriginali.mkdirs()) {
            System.out.println("[INFO] Cartella 'originali' creata: " + dirOriginali.getAbsolutePath());
        }
        File dirMaschere = new File(PATH_MASCHE);
        if (!dirMaschere.exists() && dirMaschere.mkdirs()) {
            System.out.println("[INFO] Cartella 'maschere' creata: " + dirMaschere.getAbsolutePath());
        }
    }

    /**
     * Processa le immagini presenti nelle cartelle specificate per prepararle al training.
     */
    public void processaImmagini() {
        areaLog.appendText("[INFO] Avvio preprocessing immagini...\n");
        File cartellaOriginali = new File(PATH_ORIGINALI);
        File cartellaMaschere = new File(PATH_MASCHE);
        if (!cartellaOriginali.exists() || !cartellaMaschere.exists()) {
            areaLog.appendText("[ERRORE] Cartelle non trovate: " + PATH_ORIGINALI + " o " + PATH_MASCHE + "\n");
            return;
        }
        List<File> listaOriginali = new ArrayList<>();
        List<File> listaMaschere = new ArrayList<>();
        caricaImmaginiDaCartella(cartellaOriginali, listaOriginali);
        caricaImmaginiDaCartella(cartellaMaschere, listaMaschere);

        for (File file : listaOriginali) {
            try {
                ImagePreprocessor.processAndSaveImage(file, PATH_ORIGINALI, areaLog);
            } catch (Exception ex) {
                areaLog.appendText("[ERRORE] Preprocessing immagine originale fallito: "
                        + file.getName() + " - " + ex.getMessage() + "\n");
            }
        }

        for (File file : listaMaschere) {
            try {
                ImagePreprocessor.processAndSaveImage(file, PATH_MASCHE, areaLog);
            } catch (Exception ex) {
                areaLog.appendText("[ERRORE] Preprocessing maschera fallito: "
                        + file.getName() + " - " + ex.getMessage() + "\n");
            }
        }

        areaLog.appendText("[INFO] Preprocessing completato.\n");
        verificaImmaginiOpenCV(PATH_ORIGINALI);
        verificaImmaginiOpenCV(PATH_MASCHE);
    }

    /**
     * Carica le immagini da una cartella specificata e le aggiunge a una lista.
     *
     * @param cartella      la cartella da cui caricare le immagini
     * @param listaImmagini la lista in cui aggiungere le immagini caricate
     */
    private void caricaImmaginiDaCartella(File cartella, List<File> listaImmagini) {
        File[] files = cartella.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        if (files != null && files.length > 0) {
            listaImmagini.addAll(Arrays.asList(files));
            areaLog.appendText("[INFO] Trovate " + files.length + " immagini .png"
                    + " in " + cartella.getAbsolutePath() + "\n");
        } else {
            areaLog.appendText("[ERRORE] Nessuna immagine .png"
                    + " trovata in " + cartella.getAbsolutePath() + "\n");
        }
    }

    /**
     * Verifica che le immagini nella cartella specificata possano essere caricate correttamente da OpenCV.
     *
     * @param cartellaPath il percorso della cartella contenente le immagini
     */
    public void verificaImmaginiOpenCV(String cartellaPath) {
        File cartella = new File(cartellaPath);
        File[] files = cartella.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        if (files == null) {
            areaLog.appendText("[ERRORE] Cartella non esiste o non contiene PNG: " + cartellaPath + "\n");
            return;
        }
        areaLog.appendText("[INFO] Verifica immagini in: " + cartellaPath + "\n");
        for (File file : files) {
            Mat img = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.IMREAD_UNCHANGED);
            if (img.empty()) {
                areaLog.appendText("[ERRORE] OpenCV non ha caricato l'immagine: "
                        + file.getAbsolutePath() + "\n");
            } else {
                areaLog.appendText("[INFO] Immagine caricata: " + file.getAbsolutePath()
                        + " (" + img.width() + "x" + img.height() + ")\n");
            }
        }
    }

    /**
     * Avvia l'addestramento del modello utilizzando i dati pre-processati.
     */
    public void addestraModello() {
        barraProgresso.setVisible(true);
        barraProgresso.setProgress(0.1);
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                areaLog.appendText("[INFO] Inizio addestramento modello...\n");
                modello = ModelArchitecture.createModel();
                List<DataSet> trainingData = loadTrainingDataFromFolders();
                if (trainingData.isEmpty()) {
                    areaLog.appendText("[ERRORE] Nessuna coppia valida per l'addestramento.\n");
                    return null;
                }
                int numEpochs;
                try {
                    numEpochs = Integer.parseInt(txtEpochs.getText());
                } catch (NumberFormatException ex) {
                    areaLog.appendText("[ERRORE] Numero di epoche non valido, uso valore di default 50.\n");
                    numEpochs = 50;
                }
                trainModel(modello, trainingData, numEpochs);
                areaLog.appendText("[INFO] Addestramento completato.\n");
                barraProgresso.setProgress(1.0);
                return null;
            }
        };
        task.setOnSucceeded(e -> barraProgresso.setVisible(false));
        task.setOnFailed(e -> {
            barraProgresso.setVisible(false);
            areaLog.appendText("[ERRORE] Addestramento fallito.\n");
        });
        new Thread(task).start();
    }

    /**
     * Carica i dati di training dalle cartelle specificate.
     *
     * @return una lista di oggetti DataSet contenenti input e label per il training
     */
    private List<DataSet> loadTrainingDataFromFolders() {
        List<DataSet> datasets = new ArrayList<>();
        ImagePreProcessingScaler scaler = new ImagePreProcessingScaler(0, 1);
        File cartellaOriginali = new File(PATH_ORIGINALI);
        File cartellaMaschere = new File(PATH_MASCHE);
        File[] filesOriginali = cartellaOriginali.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        File[] filesMaschere = cartellaMaschere.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));

        if (filesOriginali == null || filesMaschere == null) {
            areaLog.appendText("[ERRORE] Una delle cartelle non contiene immagini PNG.\n");
            return datasets;
        }

        for (File originalFile : filesOriginali) {
            String nomeFile = originalFile.getName();
            File maskFile = new File(PATH_MASCHE, nomeFile);
            if (!maskFile.exists()) {
                areaLog.appendText("[ERRORE] Maschera non trovata per: " + nomeFile + "\n");
                continue;
            }
            try {
                INDArray input = loadImageWithImageIO(originalFile, 3);
                scaler.transform(input);
                INDArray label = loadImageWithImageIO(maskFile, 1);
                scaler.transform(label);
                datasets.add(new DataSet(input, label));
                areaLog.appendText("[INFO] Coppia caricata: " + nomeFile + "\n");
            } catch (Exception ex) {
                areaLog.appendText("[ERRORE] Problema caricamento coppia " + nomeFile + ": "
                        + ex.getMessage() + "\n");
            }
        }
        return datasets;
    }

    /**
     * Addestra il modello utilizzando i dati di training forniti.
     *
     * @param model         il modello da addestrare
     * @param trainingData  i dati di training
     * @param epochs        il numero di epoche per l'addestramento
     */
    private void trainModel(MultiLayerNetwork model, List<DataSet> trainingData, int epochs) {
        int batchSize = 1;
        ListDataSetIterator trainIter =
                new org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator<>(trainingData, batchSize);
        model.fit(trainIter, epochs);
    }

    /**
     * Carica un'immagine utilizzando ImageIO e applica un padding bianco per uniformare le dimensioni.
     *
     * @param file      il file immagine da caricare
     * @param channels  il numero di canali dell'immagine (es. 3 per RGB)
     * @return un array INDArray contenente i dati dell'immagine
     * @throws IOException se si verifica un errore durante il caricamento dell'immagine
     */
    private INDArray loadImageWithImageIO(File file, int channels) throws IOException {
        BufferedImage bf = ImageIO.read(file);
        if (bf == null) {
            throw new IOException("ImageIO non riconosce il file: " + file.getName());
        }
        bf.getWidth();
        bf.getHeight();

        BufferedImage paddedImage = new BufferedImage(1200, 1700, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2d = paddedImage.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, 1200, 1700);

        int x = 0;
        int y = 0;
        g2d.drawImage(bf, x, y, null);
        g2d.dispose();

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        String formatName = "png";
        ImageIO.write(paddedImage, formatName, baos);
        baos.flush();
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
        NativeImageLoader loader = new NativeImageLoader(1700, 1200, channels);
        return loader.asMatrix(bais);
    }

    /**
     * Salva il modello addestrato in un file ZIP.
     */
    private void salvaModello() {
        if (modello == null) {
            areaLog.appendText("[ERRORE] Nessun modello da salvare.\n");
            return;
        }
        File dir = new File(PROJECT_ROOT + File.separator + "dl4j model");
        if (!dir.exists() && !dir.mkdirs()) {
            areaLog.appendText("[ERRORE] Impossibile creare la cartella 'dl4j model'.\n");
            return;
        }
        File fileDestinazione = new File(dir, "modelloAddestrato.zip");
        try {
            modello.save(fileDestinazione);
            areaLog.appendText("[SUCCESSO] Modello salvato in: " + fileDestinazione.getAbsolutePath() + "\n");
        } catch (Exception ex) {
            areaLog.appendText("[ERRORE] Salvataggio modello fallito: " + ex.getMessage() + "\n");
        }
    }
}