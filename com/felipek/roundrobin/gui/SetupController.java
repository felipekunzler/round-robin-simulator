package com.felipek.roundrobin.gui;

import com.felipek.roundrobin.core.RoundRobin;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SetupController implements Initializable
{
    @FXML
    private TextField newJobsFrequencyText;
    @FXML
    private TextField quantumText;
    @FXML
    private TextField jobDurationText;
    @FXML
    private TextField ioBlockPercentageText;
    @FXML
    private TextField ioBlockDurationText;
    @FXML
    private Button startButton;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
    }

    @FXML
    public void onStartClick(ActionEvent event) throws IOException
    {
        try
        {
            int newJobsFrequency = Integer.parseInt(newJobsFrequencyText.getText());
            int quantum = Integer.parseInt(quantumText.getText());
            int jobDuration = Integer.parseInt(jobDurationText.getText());
            int ioBlockPercentage = Integer.parseInt(ioBlockPercentageText.getText());
            int ioBlockDuration = Integer.parseInt(ioBlockDurationText.getText());

            RoundRobin roundRobinSimulator =
                    new RoundRobin(newJobsFrequency, quantum, jobDuration, ioBlockPercentage, ioBlockDuration);
            MainController.setRoundRobinSimulator(roundRobinSimulator);

            Stage stage = (Stage) newJobsFrequencyText.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main-layout.fxml"));
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        }
        catch (NumberFormatException e)
        {
            startButton.setStyle("-fx-border-color: red");
        }
    }

}
