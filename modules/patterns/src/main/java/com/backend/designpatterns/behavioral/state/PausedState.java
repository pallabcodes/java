package com.backend.designpatterns.behavioral.state;

// Role: Concrete State
public class PausedState implements PlayerState {

    @Override
    public void play(MediaPlayer player) {
        System.out.println("Resuming playback...");
        player.setState(new PlayingState());
    }

    @Override
    public void pause(MediaPlayer player) {
        System.out.println("Already paused.");
    }

    @Override
    public void stop(MediaPlayer player) {
        System.out.println("Stopping from paused state...");
        player.setState(new StoppedState());
    }
}
