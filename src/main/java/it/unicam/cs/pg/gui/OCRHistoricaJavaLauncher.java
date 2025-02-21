package it.unicam.cs.pg.gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Classe principale che rappresenta il launcher dell'applicazione OCR-Historica-Java.
 * Questa classe estende {@link Application} di JavaFX e fornisce un'interfaccia grafica
 * per selezionare e avviare diversi moduli dell'applicazione.
 */
public class OCRHistoricaJavaLauncher extends Application {

    /**
     * Metodo principale per l'avvio dell'applicazione.
     * Questo metodo richiama il metodo {@link #launch(String...)} di JavaFX per inizializzare
     * l'applicazione grafica.
     *
     * @param args argomenti della riga di comando (non utilizzati in questa applicazione).
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Metodo di inizializzazione dell'applicazione JavaFX.
     * Questo metodo viene chiamato automaticamente dal framework JavaFX dopo l'avvio dell'applicazione.
     * Configura la finestra principale e il layout grafico, definendo i pulsanti e le azioni associate
     * ai vari moduli dell'applicazione.
     *
     * @param primaryStage la finestra principale dell'applicazione.
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("OCR-Historica-Java");

        // Titolo e descrizione dell'applicazione
        Text title = new Text("OCR-Historica-Java");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        Text description = new Text("Seleziona un modulo per iniziare:");
        description.setFont(Font.font("Arial", 14));

        // Pulsante e descrizione per Pre-elaborazione Immagini (PreprocessingDl4jGUI)
        Button preprocessingButton = new Button("Pre-elaborazione Immagini\nPre processing");
        Label preprocessingDescription = new Label(
                "Preelaborazione delle immagini con testo utilizzando CNN o filtri matematici per migliorare la qualità e l'analisi dei dati."
        );

        // Pulsante e descrizione per Addestramento Modelli Dl4j (ModelTrainingDl4jGUI)
        Button modelTrainerButton = new Button("Training Modelli DL4J");
        Label modelTrainerDescription = new Label(
                "Addestra modelli di rete convolutiva DL4J per la preelaborazione di immagini con testo."
        );

        // Pulsante e descrizione per Elaborazione Immagini per estrazione testo (PreprocessingTess4jGUI)
        Button processingButton = new Button("Estrazione Testo OCR\nProcessing");
        Label processingDescription = new Label(
                "Analizza immagini contenenti testo, utilizzando l'OCR (Optical Character Recognition) per estrarne il contenuto."
        );

        // Pulsante e descrizione per Addestramento Modelli Tess4j (ModelTrainerTess4jGUI)
        Button tess4jButton = new Button("Training Modelli Tesseract");
        Label tess4jDescription = new Label(
                "Effettua il fine-tuning dei modelli LSTM di Tesseract per migliorare la trascrizione OCR."
        );

        // Pulsante e descrizione per Post Processing (ex Correzione Testo)
        Button textCorrectionButton = new Button("Analisi Lessicale\nPost processing");
        Label textCorrectionDescription = new Label(
                "Analizza il testo trascritto per individuare e correggere errori lessicali."
        );

        // Definisco gli stili di base per i pulsanti, con bordi arrotondati
        String orangeBase = "-fx-font-size: 14px; -fx-min-width: 200px; -fx-min-height: 40px; " +
                "-fx-border-color: #E6C4A7; -fx-border-width: 1px; " +
                "-fx-background-radius: 5px; -fx-border-radius: 5px;";
        String orangeButtonStyle = orangeBase + " -fx-background-color: #FFDAB9;";
        String orangeButtonHoverStyle = orangeBase + " -fx-background-color: #FFC8A0;";

        String blueBase = "-fx-font-size: 14px; -fx-min-width: 200px; -fx-min-height: 40px; " +
                "-fx-border-color: #9BC2CF; -fx-border-width: 1px; " +
                "-fx-background-radius: 5px; -fx-border-radius: 5px;";
        String blueButtonStyle = blueBase + " -fx-background-color: #ADD8E6;";
        String blueButtonHoverStyle = blueBase + " -fx-background-color: #9ACCE0;";

        // Assegno lo stile in base al colore
        preprocessingButton.setStyle(orangeButtonStyle);
        processingButton.setStyle(orangeButtonStyle);
        textCorrectionButton.setStyle(orangeButtonStyle);

        modelTrainerButton.setStyle(blueButtonStyle);
        tess4jButton.setStyle(blueButtonStyle);

        // Aggiungo l'effetto hover per i pulsanti
        preprocessingButton.setOnMouseEntered(e -> preprocessingButton.setStyle(orangeButtonHoverStyle));
        preprocessingButton.setOnMouseExited(e -> preprocessingButton.setStyle(orangeButtonStyle));
        processingButton.setOnMouseEntered(e -> processingButton.setStyle(orangeButtonHoverStyle));
        processingButton.setOnMouseExited(e -> processingButton.setStyle(orangeButtonStyle));
        textCorrectionButton.setOnMouseEntered(e -> textCorrectionButton.setStyle(orangeButtonHoverStyle));
        textCorrectionButton.setOnMouseExited(e -> textCorrectionButton.setStyle(orangeButtonStyle));

        modelTrainerButton.setOnMouseEntered(e -> modelTrainerButton.setStyle(blueButtonHoverStyle));
        modelTrainerButton.setOnMouseExited(e -> modelTrainerButton.setStyle(blueButtonStyle));
        tess4jButton.setOnMouseEntered(e -> tess4jButton.setStyle(blueButtonHoverStyle));
        tess4jButton.setOnMouseExited(e -> tess4jButton.setStyle(blueButtonStyle));

        // Stile per le descrizioni
        String descriptionStyle = "-fx-font-size: 12px; -fx-wrap-text: true;";
        preprocessingDescription.setStyle(descriptionStyle);
        modelTrainerDescription.setStyle(descriptionStyle);
        processingDescription.setStyle(descriptionStyle);
        tess4jDescription.setStyle(descriptionStyle);
        textCorrectionDescription.setStyle(descriptionStyle);

        // Azioni dei pulsanti
        preprocessingButton.setOnAction(e -> {
            PreprocessingDl4jGui preprocessingDl4jGUI = new PreprocessingDl4jGui();
            preprocessingDl4jGUI.start(new Stage());
        });

        modelTrainerButton.setOnAction(e -> {
            ModelTrainerDl4jGui trainingDl4jGUI = new ModelTrainerDl4jGui();
            trainingDl4jGUI.start(new Stage());
        });

        processingButton.setOnAction(e -> {
            ProcessingTess4jGui preprocessingTess4jGUI = new ProcessingTess4jGui();
            preprocessingTess4jGUI.start(new Stage());
        });

        tess4jButton.setOnAction(e -> {
            ModelTrainerTess4jGui modelTrainerTess4jGUI = new ModelTrainerTess4jGui();
            modelTrainerTess4jGUI.start(new Stage());
        });

        textCorrectionButton.setOnAction(e -> {
            TextCorrectionGui textCorrectionGUI = new TextCorrectionGui();
            textCorrectionGUI.start(new Stage());
        });

        // Layout principale (GridPane)
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Aggiungo gli elementi alla griglia
        grid.add(title, 0, 0, 2, 1);
        grid.add(description, 0, 1, 2, 1);
        grid.add(preprocessingButton, 0, 2);
        grid.add(preprocessingDescription, 1, 2);
        grid.add(modelTrainerButton, 0, 3);
        grid.add(modelTrainerDescription, 1, 3);
        grid.add(processingButton, 0, 4);
        grid.add(processingDescription, 1, 4);
        grid.add(tess4jButton, 0, 5);
        grid.add(tess4jDescription, 1, 5);
        grid.add(textCorrectionButton, 0, 6);
        grid.add(textCorrectionDescription, 1, 6);

        // Crea la scena e mostra la finestra
        Scene scene = new Scene(grid, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}