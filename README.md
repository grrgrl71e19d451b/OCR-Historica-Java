
# OCR-Historica-Java

OCR-Historica-Java è un'applicazione Java che utilizza tecniche di OCR (Optical Character Recognition) per l'elaborazione di documenti storici. Il progetto si basa su librerie avanzate come Tess4J, OpenCV, DeepLearning4j e JavaFX per fornire un'interfaccia grafica utente (GUI) intuitiva e funzionalità di elaborazione avanzate.

## Indice

1. [Descrizione del Progetto](#descrizione-del-progetto)
2. [Requisiti di Sistema](#requisiti-di-sistema)
3. [Installazione](#installazione)
    - [Clonazione del Repository](#clonazione-del-repository)
    - [Dipendenze Maven](#dipendenze-maven)
    - [Configurazione Manuale delle Librerie](#configurazione-manuale-delle-librerie)
4. [Esecuzione del Progetto](#esecuzione-del-progetto)
5. [Struttura del Progetto](#struttura-del-progetto)
6. [Librerie Esterne Utilizzate](#librerie-esterne-utilizzate)
7. [Contributi](#contributi)
8. [Licenza](#licenza)

## Descrizione del Progetto

Il progetto OCR-Historica-Java è stato sviluppato per facilitare l'estrazione di testo da documenti storici tramite tecniche di OCR. L'applicazione include:

- Un'interfaccia grafica utente (GUI) realizzata con JavaFX.
- Funzionalità di elaborazione delle immagini tramite OpenCV.
- Integrazione con Tess4J per il riconoscimento ottico dei caratteri.
- Modelli di apprendimento automatico basati su DeepLearning4j per migliorare l'accuratezza dell'OCR.

## Limiti dei Dataset

I dataset presenti in questo repository sono **campioni rappresentativi** dei dati utilizzati per l'addestramento dei modelli OCR. Per motivi di dimensione, sono stati caricati solo un numero limitato di file, ma il dataset completo è stato utilizzato per il training e la validazione dei modelli. I dati completi sono disponibili **localmente**.

Questa versione ridotta è inclusa nel repository solo per scopi di dimostrazione e test.

## Requisiti di Sistema

Prima di iniziare, assicurati di soddisfare i seguenti requisiti:

- Java Development Kit (JDK): Versione 19 o superiore.
- Maven: Versione 3.8.1 o superiore.
- Sistema Operativo: Compatibile con le librerie native di OpenCV e ND4J (Windows, macOS, Linux).

## Installazione

### Clonazione del Repository

Per clonare il repository del progetto, esegui il seguente comando:

```
git clone https://github.com/grrgrl71e19d451b/OCR-Historica-Java.git
cd OCR-Historica-Java
```

### Dipendenze Maven

Le dipendenze del progetto sono gestite tramite Maven. Quando esegui il comando mvn install, Maven scaricherà automaticamente tutte le librerie necessarie dal repository centrale. Non è richiesta alcuna installazione manuale delle dipendenze.

Per installare le dipendenze, esegui:

```
mvn clean install
```

### Configurazione Manuale delle Librerie

La maggior parte delle librerie utilizzate nel progetto (ad esempio, Tess4J, OpenCV, DeepLearning4j) sono gestite automaticamente da Maven. Tuttavia, alcune librerie potrebbero richiedere configurazioni aggiuntive:

- **Tess4J**: Tess4J richiede l'installazione di Tesseract OCR sul sistema. Scarica e installa Tesseract dal sito ufficiale: Tesseract OCR Downloads. Dopo l'installazione, assicurati che il percorso di Tesseract sia aggiunto alla variabile di ambiente PATH.
- **OpenCV**: OpenCV viene gestito tramite la dipendenza Maven (org.bytedeco:opencv-platform). Non è richiesta alcuna installazione manuale, ma assicurati che il sistema supporti le librerie native di OpenCV.
- **ND4J**: ND4J utilizza librerie native per il calcolo efficiente. Maven scaricherà automaticamente le versioni appropriate per il tuo sistema operativo.

## Esecuzione del Progetto

Per eseguire il progetto, puoi utilizzare uno dei seguenti metodi:

### Esecuzione tramite IDE

1. Importa il progetto in un IDE Java (ad esempio, IntelliJ IDEA o Eclipse).
2. Configura il JDK corretto (versione 19 o superiore).
3. Esegui la classe principale specificata nel file pom.xml (gui.it.unicam.cs.pg.OCRHistoricaJavaLauncher).

### Esecuzione tramite JAR

L'applicazione utilizza JavaFX, che deve essere scaricato separatamente dal JDK (versione 11 e superiori). Dopo aver eseguito `mvn clean package`, esegui il JAR con il comando:

```
java --module-path C:\javafx-sdk-21.0.5\lib --add-modules javafx.controls,javafx.fxml -jar C:\OCR-Historica-Java\target\OCR-Historica-Java-1.0-SNAPSHOT-shaded.jar
```
Assicurati che i percorsi di JavaFX e del file JAR siano corretti in base alle tue installazioni personali.

## Struttura del Progetto

La struttura del progetto è organizzata come segue:

```
OCR-Historica-Java/
├── gui/
│   ├── ModelTrainerTess4jGui
│   ├── ModelTrainerDl4jGui
│   ├── OCRHistoricaJavaLauncher
│   ├── PreprocessingDl4jGui
│   ├── ProcessingTess4jGui
│   ├── TextCorrectionGui
│
├── modelDl4jTraining/
│   ├── BoxCleaner
│   ├── BoxTextEditor
│   ├── ModelArchitecture
│   ├── ModelTrainingExecutor
│
├── modelTess4JTraining/
│   ├── ImagePreprocessor
│
├── postprocessing/
│   ├── TextCorrector
│
├── preprocessing/
│   ├── ImageProcessingTask
│   ├── ImageUtils
│   ├── PreprocessingFilters
│
├── processing/
│   ├── ImageSelectionWindow
│   ├── OCR
│
├── resources/
│
├── test/
```

**Nota**: Ogni modulo dell'interfaccia grafica presente nella cartella `gui/` è associato a una sottocartella contenente le classi ausiliari specifiche per quel modulo. Alcune di queste classi ausiliari sono condivise tra diversi moduli dell'interfaccia per promuovere la modularità e il riutilizzo del codice.

## Librerie Esterne Utilizzate

Di seguito sono elencate le principali librerie esterne utilizzate nel progetto:

| Libreria         | Versione         | Descrizione                                          |
|------------------|------------------|------------------------------------------------------|
| Tess4J           | 5.0.0            | Wrapper Java per Tesseract OCR.                      |
| OpenCV           | 4.9.0-1.5.10     | Libreria per l'elaborazione delle immagini.          |
| DeepLearning4j   | 1.0.0-beta7      | Framework di deep learning per Java.                 |
| JavaFX           | 20               | Framework per la creazione di interfacce grafiche utente. |
| Jackson          | 2.15.2           | Libreria per la serializzazione/deserializzazione JSON. |
| ND4J             | 1.0.0-beta7      | Libreria per il calcolo numerico (backend di DeepLearning4j). |

## Contributi

Se desideri contribuire al progetto, segui questi passaggi:

1. Forka il repository.
2. Crea una nuova branch (git checkout -b feature/nome-feature).
3. Effettua le modifiche e invia una pull request.

## Licenza

Questo progetto è distribuito sotto la licenza MIT. Puoi utilizzare, copiare, modificare, fondere, pubblicare, distribuire, sublicenziare e/o vendere copie del Software, a condizione che siano soddisfatte le seguenti condizioni:

- La licenza deve essere inclusa in tutte le copie o porzioni sostanziali del Software.
- Il software è fornito "così com'è", senza garanzie di alcun tipo, espresse o implicite. In nessun caso gli autori o i detentori del copyright saranno responsabili per danni di qualsiasi tipo, anche se avvisati della possibilità di tali danni.
