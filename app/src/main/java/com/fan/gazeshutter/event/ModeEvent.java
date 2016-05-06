package com.fan.gazeshutter.event;


import com.fan.gazeshutter.activity.PilotStudyActivity;

public class ModeEvent {
    public PilotStudyActivity.MODE mode;

    public ModeEvent(int pos) {
        this.mode = PilotStudyActivity.MODE.values()[pos];
    }
}
