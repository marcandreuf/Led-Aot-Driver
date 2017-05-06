package com.andreufm.aot.driver.aotdrivers;

import android.os.SystemClock;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.userdriver.UserDriverManager;

import java.io.IOException;

/**
 * Created by marc on 1/05/17.
 */

public class Led implements AutoCloseable {
    /*package*/ static final String TAG = Led.class.getSimpleName();
    /*package*/ static final String MSG_TURNED_ON = "Led turned ON !";
    /*package*/ static final String MSG_TURNED_OFF = "Led turned OFF !";



    /**
     * Logic level when the Led is considered to be on.
     */
    public enum LogicState {
        ON_WHEN_HIGH,
        ON_WHEN_LOW
    }


    private final Gpio mLedGpio;


    /**
     * Interface definition for a callback to be invoked when the Led turn-on event occurs.
     */
    public interface OnLEDEventListener {
        /**
         * Called when a Led turn-on event occurs
         *
         * @param led the Led for which the event occurred
         * @param state true if the Led is now ON
         */
        void onButtonEvent(Led led, boolean state);
    }

    /**
     * Create a new Led driver for the given GPIO pin name.
     *
     * @param pin GPIO pin where the LED is attached.
     * @param logicState Logic level when the LED is considered to be ON.
     * @throws IOException
     */
    public Led(String pin, LogicState logicState) throws IOException {
        try {
            PeripheralManagerService pioService = UserDriverFactory.getPeripheralManagerService();
            mLedGpio = pioService.openGpio(pin);
            int direction = getInitialDirection(logicState);
            mLedGpio.setDirection(direction);
            int active = getActiveState(logicState);
            mLedGpio.setActiveType(active);
            mLedGpio.setValue(true);
        } catch (IOException|RuntimeException e) {
            Log.e(TAG, e.getLocalizedMessage());
            close();
            throw e;
        }
    }

    private int getActiveState(LogicState logicState) {
        return logicState.equals(LogicState.ON_WHEN_HIGH) ?
                Gpio.ACTIVE_HIGH : Gpio.ACTIVE_LOW;
    }

    private int getInitialDirection(LogicState logicState) {
        return logicState.equals(LogicState.ON_WHEN_HIGH) ?
                Gpio.DIRECTION_OUT_INITIALLY_LOW : Gpio.DIRECTION_OUT_INITIALLY_HIGH;
    }


    @Override
    public void close() throws IOException {
        mLedGpio.close();
    }

    public void turnOn() throws IOException {
        Log.d(TAG, MSG_TURNED_ON);
        mLedGpio.setValue(true);
    }

    public void turnOn(long timeOff) throws IOException {
        turnOn();
        SystemClock.sleep(timeOff);
        turnOff();
    }



    public void turnOff() throws IOException {
        Log.d(TAG, MSG_TURNED_OFF);
        mLedGpio.setValue(false);
    }

    public void toggle() throws IOException {
        boolean currentState = mLedGpio.getValue();
        mLedGpio.setValue(!currentState);
    }


}
