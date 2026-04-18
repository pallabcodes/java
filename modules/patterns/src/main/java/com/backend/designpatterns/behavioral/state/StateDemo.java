package com.backend.designpatterns.behavioral.state;

public class StateDemo {

    public static void main(String[] args) {
        System.out.println("--- State Pattern Demo ---");

        // Use Case: Use State when an object's behavior depends on its internal state, 
        // and it must change its behavior at runtime (e.g., Media Player transitions).

        MediaPlayer player = new MediaPlayer();

        player.play();  // Stopped -> Playing
        player.pause(); // Playing -> Paused
        player.play();  // Paused -> Playing
        player.stop();  // Playing -> Stopped
    }
}
