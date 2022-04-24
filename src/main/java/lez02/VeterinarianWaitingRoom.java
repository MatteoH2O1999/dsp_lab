package lez02;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

class WaitingRoom {
    public static void main(String[] args) throws IOException, InterruptedException {
        int numberOfDogs, numberOfCats, maxSeconds;

        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        try {
            System.out.println("Insert number of dogs:");
            numberOfDogs = Integer.parseInt(userInput.readLine());
            System.out.println("Insert number of cats:");
            numberOfCats = Integer.parseInt(userInput.readLine());
            System.out.println("Insert maximum number of seconds to wait:");
            maxSeconds = Integer.parseInt(userInput.readLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number. Must be an Integer.");
            throw e;
        }
        VeterinarianWaitingRoom room = new VeterinarianWaitingRoom();
        ArrayList<Animal> animals = new ArrayList<>();

        for (int i = 0; i < numberOfDogs; i++) {
            animals.add(new Dog(room, maxSeconds));
        }
        for (int i = 0; i < numberOfCats; i++) {
            animals.add(new Cat(room, maxSeconds));
        }

        for (Animal a :
                animals) {
            a.start();
        }
        for (Animal a :
                animals) {
            a.join();
        }
    }
}

public class VeterinarianWaitingRoom {
    ArrayList<Animal> animals;

    public VeterinarianWaitingRoom() {
        this.animals = new ArrayList<>();
    }

    public synchronized boolean enterRoom(Animal animal) {
        if (animal == null) {
            return false;
        }
        while (!animal.canEnter(this.animals)) {
            try {
                wait();
            } catch (InterruptedException e) {
                return false;
            }
        }
        this.animals.add(animal);
        return true;
    }

    public synchronized boolean exitRoom(Animal animal) {
        if (animal == null) {
            return false;
        }
        if (!animals.contains(animal)) {
            System.out.println("The animal is not in the waiting room. It cannot exit it.");
            return false;
        }
        animals.remove(animal);
        if (animals.contains(animal)) {
            throw new RuntimeException("There was more than one copy of the animal in the waiting room");
        }
        notifyAll();
        return true;
    }
}

abstract class Animal extends Thread {
    final VeterinarianWaitingRoom room;
    int secondsToSleep;

    abstract public boolean canEnter(ArrayList<Animal> animals);

    public Animal(VeterinarianWaitingRoom room, int maxSeconds) {
        Random rnd = new Random();
        this.secondsToSleep = rnd.nextInt(maxSeconds - 1) + 1;
        this.room = room;
    }

    @Override
    public void run() {
        if (room.enterRoom(this)) {
            System.out.printf("Instance of %s entered the waiting room. Will stay for %d seconds.\n", this.getClass().getSimpleName(), this.secondsToSleep);
            try {
                Thread.sleep(this.secondsToSleep * 1000L);
            } catch (InterruptedException e) {
                System.out.println("Interrupted. Returning...");
                return;
            }
            if (room.exitRoom(this)) {
                System.out.printf("Instance of %s exited the waiting room.\n", this.getClass().getSimpleName());
                return;
            }
        }
        throw new RuntimeException("Something went wrong with the animal lifecycle");
    }
}

class Dog extends Animal {
    public Dog(VeterinarianWaitingRoom room, int maxSeconds) {
        super(room, maxSeconds);
    }

    @Override
    public boolean canEnter(ArrayList<Animal> animals) {
        if (animals.size() >= 4) {
            return false;
        }
        for (Animal a : animals) {
            if (a instanceof Cat) {
                return false;
            }
        }
        return true;
    }
}

class Cat extends Animal {
    public Cat(VeterinarianWaitingRoom room, int maxSeconds) {
        super(room, maxSeconds);
    }

    @Override
    public boolean canEnter(ArrayList<Animal> animals) {
        return !(animals.size() > 0);
    }
}
