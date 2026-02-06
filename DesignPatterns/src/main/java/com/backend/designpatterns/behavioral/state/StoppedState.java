package com.backend.designpatterns.behavioral.state;

// Role: Concrete State
public class StoppedState implements PlayerState {

    @Override
    public void play(MediaPlayer player) {
        System.out.println("Starting playback...");
        player.setState(new PlayingState());
    }

    @Override
    public void pause(MediaPlayer player) {
        System.out.println("ERROR: Cannot pause when stopped.");
    }

    @Override
    public void stop(MediaPlayer player) {
        System.out.println("Already stopped.");
    }
}
