package com.pandadragonmaze.pandadragonmaze;

import android.graphics.Bitmap;

public class Level {

    private String originalMazeString;
    private String visitMsg;
    private String completeMsg;
    private Bitmap specialMapImg;
    private String currentMazeStr;
    private boolean isComplete;
    private boolean isVisited;

    public Level(String os, String vm, String cm){
        this.originalMazeString = os;
        this.visitMsg = vm;
        this.completeMsg = cm;
        this.specialMapImg = null;
        this.currentMazeStr = null;
        this.isComplete = false;
        this.isVisited = false;
    }

    public Level(String os) {
        this(os, null, null);
    }

    public Level(String os, String cm) {
        this(os, null, cm);
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }
    public String getOriginalMazeString() {
        return originalMazeString;
    }

    public void setOriginalMazeString(String originalMazeString) {
        this.originalMazeString = originalMazeString;
    }

    public String getVisitMsg() {
        return visitMsg;
    }

    public void setVisitMsg(String visitMsg) {
        this.visitMsg = visitMsg;
    }

    public String getCompleteMsg() {
        return completeMsg;
    }

    public void setCompleteMsg(String completeMsg) {
        this.completeMsg = completeMsg;
    }

    public Bitmap getSpecialMapImg() {
        return specialMapImg;
    }

    public void setSpecialMapImg(Bitmap specialMapImg) {
        this.specialMapImg = specialMapImg;
    }

    public String getCurrentMazeStr() {
        return currentMazeStr;
    }

    public void setCurrentMazeStr(String currentMazeStr) {
        this.currentMazeStr = currentMazeStr;
    }

    public boolean isVisited() {
        return isVisited;
    }

    public void setVisited(boolean visited) {
        isVisited = visited;
    }
}
