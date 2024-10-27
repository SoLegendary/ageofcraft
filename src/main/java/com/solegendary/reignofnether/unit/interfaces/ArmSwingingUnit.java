package com.solegendary.reignofnether.unit.interfaces;

import java.util.List;

public interface ArmSwingingUnit {

    int SWING_TIME_MAX = 80; // time for one full arm rotation

    Float[][] armRots = {
        { 0.0f, 0.0f, 0.0f},
        {-0.9f, 0.2f,-0.1f},
        {-1.2f, 0.3f,-0.1f},
        {-1.4f, 0.4f,-0.1f},
        {-1.5f, 0.2f,-0.2f},
        {-1.1f,-0.3f,-0.3f},
        {-0.8f,-0.6f,-0.3f},
        {-0.3f,-0.3f,-0.3f},
        {-0.2f, 0.0f,-0.2f},
        { 0.0f, 0.0f, 0.0f},
    };

    default List<Float> getNextArmRot() {
        float fracTotal = (float) getSwingTime() / ((float) armRots.length + 1);
        int index = (int) Math.floor(fracTotal);
        float frac = fracTotal - index; // float between 0-1 representing value between indices

        if (index < armRots.length - 1) {
            return List.of(
                    ((armRots[index+1][0] - armRots[index][0]) * frac) + armRots[index][0],
                    ((armRots[index+1][1] - armRots[index][1]) * frac) + armRots[index][1],
                    ((armRots[index+1][2] - armRots[index][2]) * frac) + armRots[index][2]);
        }
        else
            return List.of(0f,0f,0f);
    }

    int getSwingTime();

    void setSwingTime(int time);

    boolean isSwingingArmOnce(); // based on set boolean

    void setSwingingArmOnce(boolean swing);

    boolean isSwingingArmRepeatedly(); // based on condition like isBuilding
}
