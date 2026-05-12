package com.logmaster.api.model;

public enum LogType {
    RUBBER_4FT(4.0),
    OTHER_4FT(4.0),
    RUBBER_3FT(3.0),
    OTHER_3FT(3.0);

    private final double lengthFt;

    LogType(double lengthFt){
        this.lengthFt = lengthFt;
    }

    public double getLengthFt(){
        return lengthFt;
    }
}
