package fi.oulu.tol.esde35.ohapclient35;

/**
 * Created by Hannu Raappana on 6.5.2015.
 *
 * Interface including the methods handling the orientation.
 */
public interface DeviceOrientationInterface {

    public void tiltedAway();
    public void tiltedTowards();
    public void tiltedLeft();
    public void tiltedRight();
    public void faceDown();
    public void faceUp();
}
