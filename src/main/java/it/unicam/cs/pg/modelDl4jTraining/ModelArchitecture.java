package it.unicam.cs.pg.modelDl4jTraining;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.CnnLossLayer;
import org.deeplearning4j.nn.conf.layers.Deconvolution2D;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class ModelArchitecture {

    /**
     * Crea e inizializza la rete neurale (configurazione di esempio).
     *
     * @return il modello DL4J creato
     */
    public static MultiLayerNetwork createModel() {
        int height = 1700; // Altezza dell'input
        int width = 1200;  // Larghezza dell'input
        int channels = 3;  // Numero di canali (RGB)
        double lr = 1e-3;  // Learning rate

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123) // Imposta un seed per la riproducibilità
                .updater(new Adam(lr)) // Ottimizzatore Adam per l'aggiornamento dei pesi
                .weightInit(WeightInit.XAVIER) // Inizializzazione dei pesi con Xavier
                .list()
                // Encoder: Riduzione dimensionale e estrazione delle feature
                .layer(new ConvolutionLayer.Builder(3, 3) // Strato convoluzionale
                        .nIn(channels) // Numero di canali in input (3 per RGB)
                        .nOut(16) // Numero di filtri (feature maps) in output
                        .stride(1, 1) // Passo di scorrimento del kernel
                        .padding(1, 1) // Padding per mantenere le dimensioni dell'input
                        .activation(Activation.RELU) // Funzione di attivazione ReLU
                        .build()) // **Classificazione**: Estrae feature locali dall'immagine.
                .layer(new ConvolutionLayer.Builder(3, 3) // Secondo strato convoluzionale
                        .nOut(16) // Aumenta la profondità delle feature maps
                        .stride(1, 1)
                        .padding(1, 1)
                        .activation(Activation.RELU)
                        .build()) // **Classificazione**: Raffina le feature estratte dal primo strato.
                .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX) // Strato di pooling
                        .kernelSize(2, 2) // Dimensione del kernel di pooling
                        .stride(2, 2) // Passo di scorrimento del pooling
                        .build()) // **Classificazione**: Riduce la dimensionalità preservando le feature principali.

                // Strati intermedi: Ulteriore estrazione e raffinamento delle feature
                .layer(new ConvolutionLayer.Builder(3, 3) // Terzo strato convoluzionale
                        .nOut(32) // Aumenta ulteriormente la profondità delle feature maps
                        .stride(1, 1)
                        .padding(1, 1)
                        .activation(Activation.RELU)
                        .build()) // **Classificazione**: Estrae feature più complesse.
                .layer(new ConvolutionLayer.Builder(3, 3) // Quarto strato convoluzionale
                        .nOut(32)
                        .stride(1, 1)
                        .padding(1, 1)
                        .activation(Activation.RELU)
                        .build()) // **Classificazione**: Continua a raffinare le feature estratte.

                // Decoder: Ricostruzione dell'immagine originale
                .layer(new Deconvolution2D.Builder(2, 2) // Strato deconvoluzionale
                        .nOut(16) // Riduce la profondità delle feature maps
                        .stride(2, 2) // Passo di scorrimento inverso per aumentare la dimensione
                        .padding(0, 0)
                        .activation(Activation.RELU)
                        .build()) // **Classificazione**: Ricostruisce gradualmente l'immagine.
                .layer(new ConvolutionLayer.Builder(3, 3) // Quinto strato convoluzionale
                        .nOut(16)
                        .stride(1, 1)
                        .padding(1, 1)
                        .activation(Activation.RELU)
                        .build()) // **Classificazione**: Migliora i dettagli ricostruiti.

                // Output: Generazione dell'output finale
                .layer(new ConvolutionLayer.Builder(1, 1) // Strato convoluzionale 1x1
                        .nOut(1) // Un solo canale in output (maschera binaria)
                        .stride(1, 1)
                        .activation(Activation.SIGMOID) // Funzione di attivazione Sigmoid per valori tra 0 e 1
                        .build()) // **Classificazione**: Genera la maschera finale.
                .layer(new CnnLossLayer.Builder() // Strato di perdita
                        .lossFunction(LossFunctions.LossFunction.XENT) // Cross-Entropy Loss
                        .build()) // **Classificazione**: Calcola l'errore tra l'output predetto e quello reale.

                .setInputType(InputType.convolutional(height, width, channels)) // Specifica il tipo di input
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        return model;
    }
}