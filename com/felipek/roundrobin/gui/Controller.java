package com.felipek.roundrobin.gui;

import com.felipek.roundrobin.core.RoundRobin;
import com.felipek.roundrobin.core.Util;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class Controller implements Initializable
{

    public ListView<String> listViewNew;
    public ListView<String> listViewRunning;
    public ListView<String> listViewFinished;
    public ListView<Integer> listViewQueue;
    public ListView<Integer> listViewIoQueue;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        Thread thread = new Thread(() ->
        {
            RoundRobin roundRobinSimulator = new RoundRobin(500, 350, 700, 0, 1500);
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

}
