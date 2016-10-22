package com.felipek.roundrobin.gui;

import com.felipek.roundrobin.core.Job;
import com.felipek.roundrobin.core.RoundRobin;
import com.felipek.roundrobin.core.Util;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class Controller implements Initializable{

    public ListView<String> listViewNew;
    public ListView<String> listViewRunning;
    public ListView<String> listViewFinished;
    public ListView<Job> listViewQueue;
    public ListView<Job> listViewIoQueue;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Thread thread = new Thread(() -> {
            RoundRobin roundRobinSimulator = new RoundRobin(3000, 1000, 10000, 100);
            bindEvents(roundRobinSimulator);
            roundRobinSimulator.start();
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void bindEvents(RoundRobin roundRobin) {
        roundRobin.onRunJob(runLater(s -> listViewRunning.getItems().add(s)));

        roundRobin.onNewJob(runLater(j -> {
            listViewNew.getItems().add(String.format(Util.JOB_ADDED_MSG, j));
            listViewQueue.getItems().add(j);
        }));

        roundRobin.onJobRan(runLater(j -> {
            listViewQueue.getItems().remove(j);
            if (j.isFinished()) {
                listViewFinished.getItems().add(String.format(Util.JOB_FINISHED_MSG, j));
            }
            else {
                listViewQueue.getItems().add(j);
            }
        }));

        roundRobin.onJobIoBlocked(runLater(j -> {
            listViewQueue.getItems().remove(j);
            listViewIoQueue.getItems().add(j);
        }));

        roundRobin.onJobIoFinished(runLater(j -> {
            listViewIoQueue.getItems().remove(j);
            listViewQueue.getItems().add(j);
        }));
    }

    private static <T> Consumer<T> runLater(Consumer<T> consumer) {
        return t -> Platform.runLater(() -> consumer.accept(t));
    }

}
