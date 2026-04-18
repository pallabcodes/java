package com.backend.designpatterns.behavioral.state;

// Role: Context
public class MediaPlayer {
    
    private PlayerState state;

    public MediaPlayer() {
        this.state = new StoppedState(); // Default state
    }

    public void setState(PlayerState state) {
        this.state = state;
    }

    public void play() { state.play(this); }
    public void pause() { state.pause(this); }
    public void stop() { state.stop(this); }
}
