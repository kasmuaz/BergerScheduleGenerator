package sample;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;

import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;


public class Controller {

    @FXML
    private TextField path, competitionName;
    @FXML
    private TextArea txt;
    @FXML
    private Label label;
    @FXML
    private ToggleGroup togglegroup;
    @FXML
    private MenuItem exit, neew, defaultLang, defaultLang1,help,saveM;
    private boolean lang_english = false;
    private boolean lang_bosnian = false;
    private boolean read_succ = false;

    int numOfTeams = 0;
    private ArrayList<String> listOfTeams = new ArrayList<String>();
    Model model;

//initializes FXML components
    @FXML
    private void initialize() {
        exit.setOnAction(actionEvent -> Platform.exit());
        neew.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                path.setText("");
                competitionName.setText("");
                txt.setText("");
                label.setText("Choose new file!");

            }
        });
        saveM.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                saveFile(null);
            }
        });
        defaultLang.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                lang_bosnian = true;
                lang_english = false;
            }
        });
        defaultLang1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                lang_english =  true;
                lang_bosnian = false;
            }
        });
        help.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                Label secondLabel = new Label("Application Help");
                TextArea help_text = new TextArea("This is application for generating fixtures according to Berger tables.\n" +
                        "\n" +
                        "Application is used in the following manner:\n" +
                        "\n" +
                        "Firstly, user specifies path to file which contains informations about teams \n" +
                        "participating in the league in the following way:\n" +
                        "\n" +
                        "Number of teams: 4\n" +
                        "Teams:\n" +
                        "Barcelona\n" +
                        "Real Madrid\n" +
                        "Sevilla\n" +
                        "Gijon\n" +
                        "\n" +
                        "File MUST BE structured like this. Clicking on Read Button will read information.\n" +
                        "\n" +
                        "After reading information clicking on Generate will generate pairings.\n" +
                        "Clicking on Save Button generated data can be saved either as .txt or .pdf file.");
                help_text.setMinSize(450,340);
                VBox secondaryLayout = new VBox();
                secondaryLayout.getChildren().addAll(secondLabel,help_text);

                Scene secondScene = new Scene(secondaryLayout, 450, 340);

                // New window (Stage)
                Stage newWindow = new Stage();
                newWindow.setTitle("Help");
                newWindow.setScene(secondScene);

                // Set position of second window, related to primary window.
                //newWindow.setX(primaryStage.getX() + 200);
                //newWindow.setY(primaryStage.getY() + 100);

                newWindow.show();
            }
        });

        togglegroup.selectedToggleProperty().addListener((observable, oldVal, newVal) ->
        {
            RadioButton rb_n = (RadioButton) newVal.getToggleGroup().getSelectedToggle();
            if (rb_n.getText().toLowerCase().equals("english")) {
                lang_bosnian = false;
                lang_english = true;
            } else {
                lang_bosnian = true;
                lang_english = false;
            }

        });
        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                // System.out.println("pozvan" + competitionName.getText());
                model.setCompetitionName(competitionName.getText());
            }
        };

        competitionName.setOnAction(event);

        competitionName.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
                if (!newPropertyValue) {
                    model.setCompetitionName(competitionName.getText());
                }

            }
        });
    }
//read txt file which contains infos about competition 
    public void readFile(ActionEvent evt) {
        String _path = path.getText();
        try {
            File file = new File(_path);
            if (!file.exists() || !file.isFile() || file.isDirectory()) throw new IOException("");

            read_succ = true;
            Scanner sc = new Scanner(file);
            int numLine = 0;
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (numLine == 0)
                    for (int i = 0; i < line.length(); i++) {
                        char c = line.charAt(i);
                        if (c < '0' || c > '9')
                            continue;
                        else {
                            String num = line.substring(i);
                            numOfTeams = Integer.parseInt(num);
                            numLine++;
                            break;
                        }
                    }
                else if (numLine == 1)
                    numLine++;
                else {
                    for (int i = 0; i < line.length(); i++) {
                        char c = line.charAt(i);
                        if (c < 'A' || c > 'Z')
                            continue;
                        String team = line.substring(i);
                        listOfTeams.add(team);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            label.setText("Selected file doesn't exist! Read file again!");
        }
        if (read_succ) {
            String language = " ";
            if (lang_english)
                language = "english";
            else
                language = "bosnian";
            model = new Model(numOfTeams, listOfTeams, language);
            label.setText("File is successfully read!");
            String details = "Number of teams: " + numOfTeams + "\nTeams: (Sorted according to competition number)";
            for (int i = 0; i < listOfTeams.size(); i++)
                details = details + "\n" + (i + 1) + ". " + listOfTeams.get(i);
            txt.setText(details);
            initialize();
        }
    }
    //generates schedule according to Bergers's tables
    public void generateSchedule(ActionEvent evt) {
        if (read_succ) {
            String language = "bosnian";
            if (lang_english)
                language = "english";
            else
                language = "bosnian";

            if (numOfTeams % 2 == 0)
                model.generateStringEven(language);
            else model.generateStringOdd(language);
            txt.setText(model.getSchedule_txt());
            label.setText("Schedule is successfully generated!");
        } else
            label.setText("Selected file doesn't exist! Read file again!");
    }
        //handles Saving procedure 
    public void saveFile(ActionEvent evt) {
        if (read_succ) {

            Text sample = new Text(model.getSchedule_txt());
            sample.setFont(new Font(14));

            FileChooser fileChooser = new FileChooser();

            //Set extension filter for text files
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
            FileChooser.ExtensionFilter extFilterPDF = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.getExtensionFilters().add(extFilterPDF);

            //Show save file dialog
            File file = fileChooser.showSaveDialog(null);

            if (file != null) {
                if (fileChooser.getSelectedExtensionFilter().equals(extFilter))
                    saveTextToFile(model.getSchedule_txt(), file);
                else
                    try {
                        savePDFFile(model.getSchedule_txt(), file);
                    } catch (Exception e) {
                    }
            }
        } else
            label.setText("Selected file doesn't exist! Read file again!");
    }
       //saving to text file     
    private void saveTextToFile(String content, File file) {
        try {
            PrintWriter writer;
            writer = new PrintWriter(file);
            writer.println(content);
            writer.close();
        } catch (IOException ex) {
            // Logger.getLogger(SaveFileWithFileChooser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   //saving to PDF file
    private void savePDFFile(String content, File file) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            //open
            document.open();

            com.itextpdf.text.Font f = new com.itextpdf.text.Font();
            f.setStyle(com.itextpdf.text.Font.NORMAL);
            f.setSize(12);

            document.add(new Paragraph(content, f));

            //close
            document.close();

        } catch (FileNotFoundException | DocumentException e) {
            e.printStackTrace();
        }

    }

}
