package com.felipek.roundrobin.gui;

import com.felipek.roundrobin.core.RoundRobin;
import com.felipek.roundrobin.core.Util;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class MainController
{
    @FXML
    private ListView<String> listViewNew;
    @FXML
    private ListView<String> listViewRunning;
    @FXML
    private ListView<String> listViewFinished;
    @FXML
    private ListView<String> listViewQueue;
    @FXML
    private ListView<String> listViewIoQueue;

    public void startSimulator(RoundRobin roundRobinSimulator)
    {
        Thread thread = new Thread(() ->
        {
            bindEvents(roundRobinSimulator);
            roundRobinSimulator.start();
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void bindEvents(RoundRobin roundRobin)
    {
        roundRobin.onRunJob(runLater(s -> listViewRunning.getItems().add(s)));

        roundRobin.onNewJob(runLater(j ->
        {
            listViewNew.getItems().add(j.toString());
            listViewQueue.getItems().add(j.toString());
        }));

        roundRobin.onJobRan(runLater(j ->
        {
            listViewQueue.getItems().remove(j.toString());
            listViewQueue.getItems().add(j.toString());
        }));

        roundRobin.onJobFinished(runLater(j ->
        {
            listViewQueue.getItems().remove(j.toString());
            listViewFinished.getItems().add(j.toString());
        }));

        roundRobin.onJobIoBlocked(runLater(j ->
        {
            listViewQueue.getItems().remove(j.toString());
            listViewIoQueue.getItems().add(j.toString());
        }));

        roundRobin.onJobIoFinished(runLater(j ->
        {
            listViewIoQueue.getItems().remove(j.toString());
            listViewQueue.getItems().add(j.toString());
        }));
    }

    private <T> Consumer<T> runLater(Consumer<T> consumer)
    {
        return t -> Platform.runLater(() -> consumer.accept(t));
    }

}
