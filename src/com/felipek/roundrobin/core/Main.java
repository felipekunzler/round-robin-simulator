package com.felipek.roundrobin.core;

public class Main
{

    public static void main(String[] args)
    {
        RoundRobin roundRobinSimulator = new RoundRobin(3000, 1000, 10000, 90, 1000);
        roundRobinSimulator.start();
    }

}
