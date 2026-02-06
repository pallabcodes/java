package com.backend.designpatterns.behavioral.state;

// Role: Concrete State
public class PlayingState implements PlayerState {

    @Override
    public void play(MediaPlayer player) {
        System.out.println("Already playing.");
    }

    @Override
    public void pause(MediaPlayer player) {
        System.out.println("Pausing playback...");
        player.setState(new PausedState());
    }

    @Override
    public void stop(MediaPlayer player) {
        System.out.println("Stopping playback...");
        player.setState(new StoppedState());
    }
}
