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

public class MainController implements Initializable
{

    @FXML
    private ListView<String> listViewNew;
    @FXML
    private ListView<String> listViewRunning;
    @FXML
    private ListView<String> listViewFinished;
    @FXML
    private ListView<Integer> listViewQueue;
    @FXML
    private ListView<Integer> listViewIoQueue;

    private static RoundRobin roundRobinSimulator;

    @Override
    public void initialize(URL location, ResourceBundle resources)
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
            listViewNew.getItems().add(String.format(Util.JOB_ADDED_MSG, j));
            listViewQueue.getItems().add(j.getPid());
        }));

        roundRobin.onJobRan(runLater(j ->
        {
            listViewQueue.getItems().remove(Integer.valueOf(j.getPid()));
            listViewQueue.getItems().add(j.getPid());
        }));

        roundRobin.onJobFinished(runLater(j ->
        {
            listViewQueue.getItems().remove(Integer.valueOf(j.getPid()));
            listViewFinished.getItems().add(String.format(Util.JOB_FINISHED_MSG, j));
        }));

        roundRobin.onJobIoBlocked(runLater(j ->
        {
            listViewQueue.getItems().remove(Integer.valueOf(j.getPid()));
            listViewIoQueue.getItems().add(j.getPid());
        }));

        roundRobin.onJobIoFinished(runLater(j ->
        {
            listViewIoQueue.getItems().remove(Integer.valueOf(j.getPid()));
            listViewQueue.getItems().add(j.getPid());
        }));
    }

    private <T> Consumer<T> runLater(Consumer<T> consumer)
    {
        return t -> Platform.runLater(() -> consumer.accept(t));
    }

    public static void setRoundRobinSimulator(RoundRobin roundRobinSimulator)
    {
        MainController.roundRobinSimulator = roundRobinSimulator;
    }

}
