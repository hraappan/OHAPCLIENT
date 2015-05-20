package com.opimobi.ohap;

import android.util.Log;

import java.util.ArrayList;

/**
 * A reusable implementation of an event source. An EventSource object may be aggregated into
 * a specific Source object as an public final member variable as follows:
 *
 * <pre>
 * {@code
 * public class Example {
 *     public static class MagicalEvent {
 *         // ...
 *     }
 *     public final EventSource<Example, MagicalEvent> magicalEventSource = new EventSource<>(this);
 * }
 * }
 * </pre>
 *
 * @author Henrik Hedberg &lt;henrik.hedberg@iki.fi
 * @version 1.0
 */
public class EventSource<Source, Event> {
    private final static String TAG = "EventSource";
    /**
     * Defines a generic listener for the event source.
     *
     * @param <Source> The type of the aggregating class.
     * @param <Event> The type of the fired event.
     */
    public static interface Listener<Source, Event> {

        /**
         * Handles an event.
         *
         * @param source The instance of the aggregating class.
         * @param event The event data. Implementation may left this null.
         */
        void onEvent(Source source, Event event);
    };

    /**
     * The aggregating object.
     */
    private Source source;

    /**
     * The list of listeners.
     */
    private ArrayList<Listener<Source, Event>> listeners;

    /**
     * Constructs a new event source for the aggregating source object.
     *
     * @param source The aggregating object.
     */
    public EventSource(Source source) {
        this.source = source;
    }

    /**
     * Adds a new listener into the event source.
     *
     * @param listener The listener to be added.
     */
    public void addListener(Listener<Source, Event> listener) {
        if (listeners == null)
            listeners = new ArrayList<>();

        listeners.add(listener);
    }

    /**
     * Removes an existing listener from the event source.
     *
     * @param listener The listener to be removed.
     */
    public void removeListener(Listener<Source, Event> listener) {
        listeners.remove(listener);
    }

    /**
     * Calls all event listeners with the specified event data as an argument.
     *
     * @param event The event data.
     */
    public void fireEvent(Event event) {
        Log.d(TAG, "Event happened here.");
        if (listeners == null) {
            Log.d(TAG, "There were no listeners");
            return;
        }

        for (Listener<Source, Event> listener : listeners) {
            Log.d(TAG, "Inform listener  " + listener);
            listener.onEvent(source, event);
        }
    }
}
