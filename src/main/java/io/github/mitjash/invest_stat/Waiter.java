package io.github.mitjash.invest_stat;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.time.Duration;


public class Waiter {

    private static final Waiter INSTANCE = new Waiter();

    private Map<String, TreeSet<Instant>> allInstansLists;
    private Map<String, Integer> limitsList;

    // Конструктор
    private Waiter() {
        limitsList = new HashMap<>();
        limitsList.put("MyInstrumentsService", 190);
        limitsList.put("MamasInstrumentsService", 190);
        limitsList.put("MyOperationsService", 190);
        limitsList.put("MamasOperationsService", 190);
        limitsList.put("MyUsersService", 90);
        limitsList.put("MamasUsersService", 90);
        allInstansLists = new HashMap<>();
    }

    public static Waiter getInstance() {
        return INSTANCE;
    }

    public void checkLimits(String limit_name) {

        // Unknown limit_name
        if (!limitsList.containsKey(limit_name)) {
            System.out.println("Unknown limit_name in Waiter: " + limit_name);
            return;
        }

        // Create new instantsList for new limit_name
        if (!allInstansLists.containsKey(limit_name)) {
            allInstansLists.put(limit_name, new TreeSet<Instant>());
        }

        TreeSet<Instant> instantsList = allInstansLists.get(limit_name);
/*
        // Remove instants older than 1 minute
        while (!instantsList.isEmpty() && instantsList.first().isBefore(Instant.now().minus(Duration.ofMinutes(1)))) {
            Instant removed = instantsList.pollFirst();
            System.out.println("Removed old instant: " + removed);
        }*/

        // If it's necessary, remove one more and sleep
        if (instantsList.size() == limitsList.get(limit_name) && instantsList.size() > 1) {
            Instant removed = instantsList.pollFirst();
            long sleep_duration = 1000*60 - Duration.between(instantsList.first(),instantsList.last()).toMillis();
            if (sleep_duration > 0) {
                System.out.println("Wait for " + sleep_duration + " msec. (" + limit_name + ")");
                //System.out.println("Removed " + removed + ", left: " + instantsList.first() + '-' + instantsList.last());
                try {
                    Thread.sleep(sleep_duration);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Thread was interrupted");
                }
            }
        }

        instantsList.add(Instant.now());
        //System.out.println(limit_name + " " + instantsList.size()+" "+instantsList.last());
    }
}
