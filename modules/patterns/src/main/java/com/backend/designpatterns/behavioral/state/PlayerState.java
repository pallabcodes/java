package com.backend.designpatterns.behavioral.state;

// Role: State Interface
public interface PlayerState {
    void play(MediaPlayer player);
    void pause(MediaPlayer player);
    void stop(MediaPlayer player);
}
