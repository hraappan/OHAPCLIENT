package fi.oulu.tol.esde35.ohap;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.*;
import android.os.Process;
import android.util.Log;

import com.opimobi.ohap.CentralUnit;
import com.opimobi.ohap.Container;
import com.opimobi.ohap.Device;
import com.opimobi.ohap.HbdpConnection;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import fi.oulu.tol.esde35.ohapclient35.DeviceService;

/**
 * Created by Hannu Raappana on 6.5.2015.
 */
public class CentralUnitConnection extends CentralUnit {

    private final static String TAG = "CentralUnitConnection";
    private InputStream inputStream;
    private OutputStream outputStream;
    private HbdpConnection connection;
    private DeviceService deviceService;
    private ConnectionManager cm;
    private Thread thread;


    private int nListeners;

    private void startNetworking() {
        Log.d(TAG, "Start networking. Connecting to;: " + getURL());
        connection = new HbdpConnection(getURL());

        inputStream = connection.getInputStream();
        outputStream = connection.getOutputStream();

        OutgoingMessage outgoingMessage = new OutgoingMessage();
        outgoingMessage.integer8(0x00)      // message-type-login
                .integer8(0x01)      // protocol-version
                .text("hraappan")        // login-name
                .text("vfLGK30S")    // login-password
                .writeTo(outputStream);


         thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Handler handler = new Handler(Looper.getMainLooper());
                    while (true) {
                        Log.d(TAG, "Start the handler.");
                        IncomingMessage incomingMessage = new IncomingMessage();
                        incomingMessage.readFrom(inputStream);
                        handler.post(new IncomingMessageHandler(incomingMessage));

                    }
                } catch (EOFException e) {
                    Log.d(TAG, "Networking stopped.");
                } catch (IOException e) {
                    Log.e(TAG, "Exception in startNetworking(): " + e.toString());
                }
            }
        });

thread.start();



    }

    //Handler for the incoming messages.
    private class IncomingMessageHandler implements Runnable {
        private String unit;
        private double dataValue;
        private Device device;
        private int identifier;
        private String itemDataDescription;
        private long itemDataParentIdentifier;
        private String abbreviations;
        private String itemDataName;
        private double itemDataX;
        private double itemDataY;
        private double itemDataZ;
        private double min, max;
        private IncomingMessage incomingMessage;
        private boolean itemDataInternal;
        private boolean binaryValue;
        private CentralUnitConnection connection;

        public IncomingMessageHandler(IncomingMessage incomingMessage) {
            this.incomingMessage = incomingMessage;
            Log.d(TAG, "Creating new handler for the message.");

        }

        /**
         * Starts executing the active part of the class' code. This method is
         * called when a thread is started that has been created with a class which
         * implements {@code Runnable}.
         */

        @Override
        public void run() {


            int messageType = incomingMessage.integer8();
            Log.d(TAG, "The incoming message is type: " + messageType);

            switch (messageType) {
                //Logout
                case 0x01:
                    String text = incomingMessage.text();
                    Log.d(TAG, "The connection is closed!" + text);
                    break;
                //Message ping
                case 0x02:
                    Log.d(TAG, "Message PING received, Sending message PONG!");
                    int itemIdentifier = incomingMessage.integer32();
                    OutgoingMessage outgoingMessage = new OutgoingMessage();
                    outgoingMessage.integer8(0x03).integer32(itemIdentifier).writeTo(outputStream);
                    break;
                //Decimal device sensor.
                case 0x04:
                    Log.d(TAG, "Receiving decimal device sensor.");
                    identifier = incomingMessage.integer32();
                    Log.d(TAG, "The idetifier is: " + identifier);
                    dataValue = incomingMessage.decimal64();
                    Log.d(TAG, "The datavalue is:" + dataValue);

                    itemDataParentIdentifier = incomingMessage.integer32();
                    Log.d(TAG, "The parent identifier is: " + itemDataParentIdentifier);
                    itemDataName = incomingMessage.text();
                    Log.d(TAG, "THe name is: " + itemDataName);
                    itemDataDescription = incomingMessage.text();
                    Log.d(TAG, "The description is: " + itemDataDescription);
                    itemDataInternal = incomingMessage.binary8();
                    Log.d(TAG, "The internal data is: " + itemDataInternal);
                    itemDataX = incomingMessage.decimal64();
                    itemDataY = incomingMessage.decimal64();
                    itemDataZ = incomingMessage.decimal64();

                    min = incomingMessage.decimal64();
                    max = incomingMessage.decimal64();

                    unit = incomingMessage.text();
                    Log.d(TAG, "The unit is: " + unit);
                    abbreviations = incomingMessage.text();
                    Log.d(TAG, "The abbreviations are: " + abbreviations);
                    connection = (CentralUnitConnection) cm.getCentralUnit(getURL());
                    Container myUnit =(Container) connection.getItemById(itemDataParentIdentifier);
                    device = new Device(myUnit, identifier, Device.Type.SENSOR, Device.ValueType.DECIMAL);
                    device.setMinMaxValues(min, max);
                    device.setUnit(unit, abbreviations);
                    device.setName(itemDataName);
                    device.setDecimalValue(dataValue);
                    device.setDescription(itemDataDescription);
                    device.setLocation((int) itemDataX, (int) itemDataY, (int) itemDataZ);
                    device.setInternal(itemDataInternal);
                    itemAddedEventSource.fireEvent(device);

                    break;

                //decimal device actuator.
                case 0x05:
                    Log.d(TAG, "Receiving decimal device actuator.");
                    identifier = incomingMessage.integer32();
                    dataValue = incomingMessage.decimal64();

                    itemDataParentIdentifier = incomingMessage.integer32();
                    Log.d(TAG, "The parent identifier is: " + itemDataParentIdentifier);
                    itemDataName = incomingMessage.text();
                    itemDataDescription = incomingMessage.text();
                    itemDataInternal = incomingMessage.binary8();
                    itemDataX = incomingMessage.decimal64();
                    itemDataY = incomingMessage.decimal64();
                    itemDataZ = incomingMessage.decimal64();

                    min = incomingMessage.decimal64();
                    max = incomingMessage.decimal64();

                    unit = incomingMessage.text();
                    abbreviations = incomingMessage.text();
                    connection = (CentralUnitConnection) cm.getCentralUnit(getURL());
                    device = new Device(connection, identifier, Device.Type.ACTUATOR, Device.ValueType.DECIMAL);
                    device.setDecimalValue(dataValue);
                    device.setMinMaxValues(min, max);
                    device.setUnit(unit, abbreviations);
                    device.setName(itemDataName);
                    device.setDescription(itemDataDescription);
                    device.setLocation((int) itemDataX, (int) itemDataY, (int) itemDataZ);
                    device.setInternal(itemDataInternal);
                    itemAddedEventSource.fireEvent(device);
                    break;

                //Binary device sensor.
                case 0x06:
                    Log.d(TAG, "Receiving binary device sensor.");
                    identifier = incomingMessage.integer32();
                    binaryValue = incomingMessage.binary8();

                    itemDataParentIdentifier = incomingMessage.integer32();
                    itemDataName = incomingMessage.text();
                    itemDataDescription = incomingMessage.text();
                    itemDataInternal = incomingMessage.binary8();
                    itemDataX = incomingMessage.decimal64();
                    itemDataY = incomingMessage.decimal64();
                    itemDataZ = incomingMessage.decimal64();
                    connection = (CentralUnitConnection) cm.getCentralUnit(getURL());
                    device = new Device(connection, identifier, Device.Type.SENSOR, Device.ValueType.BINARY);
                    device.setName(itemDataName);
                    device.setDescription(itemDataDescription);
                    device.setLocation((int) itemDataX, (int) itemDataY, (int) itemDataZ);
                    device.setInternal(itemDataInternal);
                    itemAddedEventSource.fireEvent(device);
                    break;

                case 0x07:

                    Log.d(TAG, "Receiving binary device actuator.");
                    identifier = incomingMessage.integer32();
                    Log.d(TAG, "The binary device identifier is: " + identifier);

                    binaryValue = incomingMessage.binary8();
                    Log.d(TAG, "The binary value of the device is: " + binaryValue);
                    itemDataParentIdentifier = incomingMessage.integer32();
                    Log.d(TAG, "The parent identifier is: " + itemDataParentIdentifier);
                    itemDataName = incomingMessage.text();
                    Log.d(TAG, "The name of the device is: " + itemDataName);
                    itemDataDescription = incomingMessage.text();
                    Log.d(TAG, "The description of the device: " + itemDataDescription);
                    itemDataInternal = incomingMessage.binary8();
                    Log.d(TAG, "The item data is: " + itemDataInternal);
                    itemDataX = incomingMessage.decimal64();
                    Log.d(TAG, "X is: " + itemDataX);
                    itemDataY = incomingMessage.decimal64();
                    Log.d(TAG, "Y is: " + itemDataY);
                    itemDataZ = incomingMessage.decimal64();
                    Log.d(TAG, "Z is: " + itemDataZ);
                    connection = (CentralUnitConnection) cm.getCentralUnit(getURL());
                    Log.d(TAG, "The parent is: " + connection.getName());
                    try {
                        device = new Device(connection, identifier, Device.Type.ACTUATOR, Device.ValueType.BINARY);
                        device.setName(itemDataName);
                        device.setBinaryValue(binaryValue);
                        device.setDescription(itemDataDescription);
                        device.setLocation((int) itemDataX, (int) itemDataY, (int) itemDataZ);
                        device.setInternal(itemDataInternal);
                    }catch(IllegalArgumentException exception) {
                        Log.d(TAG, "There already is a device with same id.");
                    }
                    itemAddedEventSource.fireEvent(device);


                    break;

                case 0x08:
                    Log.d(TAG, "Getting container.");
                    identifier = incomingMessage.integer32();
                    Log.d(TAG, "The identifier is: " + identifier);

                    if(identifier == 0) {
                        Log.d(TAG, "Updating container.");
                        itemDataParentIdentifier = incomingMessage.integer32();
                        Log.d(TAG, "The parent identifier is: " + itemDataParentIdentifier);
                        itemDataName = incomingMessage.text();
                        Log.d(TAG, "The container is: " + itemDataName);
                        itemDataDescription = incomingMessage.text();
                        Log.d(TAG, "The item description is: " + itemDataDescription);
                        itemDataInternal = incomingMessage.binary8();
                        Log.d(TAG, "DataInternal is: " + itemDataInternal);
                        itemDataX = incomingMessage.decimal64();
                        Log.d(TAG, "X is: " + itemDataX);
                        itemDataY = incomingMessage.decimal64();
                        Log.d(TAG, "Y is: " + itemDataY);
                        itemDataZ = incomingMessage.decimal64();
                        Log.d(TAG, "Z is: " + itemDataZ);

                        CentralUnitConnection.this.setName(itemDataName);
                        CentralUnitConnection.this.setInternal(itemDataInternal);
                        CentralUnitConnection.this.setDescription(itemDataDescription);
                        CentralUnitConnection.this.setLocation((int) itemDataX, (int) itemDataY, (int) itemDataZ);
                        Log.d(TAG, "The container is now: " + CentralUnitConnection.this.getName());
                        itemAddedEventSource.fireEvent(CentralUnitConnection.this);
                        sendListeningStart(CentralUnitConnection.this);
                        break;
                    }



                    Log.d(TAG, "Creating container.");
                    itemDataParentIdentifier = incomingMessage.integer32();
                    Log.d(TAG, "The parent identifier is: " + itemDataParentIdentifier);
                    itemDataName = incomingMessage.text();
                    Log.d(TAG, "The container is: " + itemDataName);
                    itemDataDescription = incomingMessage.text();
                    Log.d(TAG, "The item description is: " + itemDataDescription);
                    itemDataInternal = incomingMessage.binary8();
                    Log.d(TAG, "DataInternal is: " + itemDataInternal);
                    itemDataX = incomingMessage.decimal64();
                    Log.d(TAG, "X is: " + itemDataX);
                    itemDataY = incomingMessage.decimal64();
                    Log.d(TAG, "Y is: " + itemDataY);
                    itemDataZ = incomingMessage.decimal64();
                    Log.d(TAG, "Z is: " + itemDataZ);
                    try {
                        Container newUnit = new Container(cm.getCentralUnit(getURL()), identifier);
                        newUnit.setName(itemDataName);
                        newUnit.setInternal(itemDataInternal);
                        newUnit.setDescription(itemDataDescription);
                        newUnit.setLocation((int) itemDataX, (int) itemDataY, (int) itemDataZ);
                        itemAddedEventSource.fireEvent(newUnit);

                    }catch(IllegalArgumentException exception) {
                        Log.d(TAG, "There already is a device with same id.");
                    }


                    break;

                case 0x09:
                    Log.d(TAG, "Decimal value changed.");
                    itemIdentifier = incomingMessage.integer32();
                    Log.d(TAG, "The itemidentifier is: " + itemIdentifier);
                    dataValue = incomingMessage.decimal64();
                    connection =(CentralUnitConnection) cm.getCentralUnit(getURL()).getItemById(itemDataParentIdentifier);
                    device = (Device) connection.getItemById(itemIdentifier);
                    device.setDecimalValue(dataValue);
                    break;

                case 0x0a:
                    Log.d(TAG, "Binary value changed.");
                    itemIdentifier = incomingMessage.integer32();
                    binaryValue = incomingMessage.binary8();
                    connection = (CentralUnitConnection) cm.getCentralUnit(getURL());
                    device = (Device) connection.getItemById(itemIdentifier);
                    device.setBinaryValue(binaryValue);

                    break;
                case 0x0b:
                    Log.d(TAG, "Item destroyed.");
                    itemIdentifier = incomingMessage.integer32();
                    connection = (CentralUnitConnection) cm.getCentralUnit(getURL());
                    device = (Device) connection.getItemById(itemIdentifier);
                    device.destroy();
                    break;

                default:
                    Log.d(TAG, "Cannot parse data." + messageType);

                    break;
            }

        }
    }

    private void stopNetworking() {
    Log.d(TAG, "Stopping networking");

        OutgoingMessage outgoingMessage = new OutgoingMessage();
        outgoingMessage
                .integer8(0x01)      // protocol-version
                .text("").writeTo(outputStream);

        try {
            outputStream.close();
            connection.close();
        }
        catch(IOException exception) {
            Log.d(TAG, "There was exception when closing the connections: " + exception);

        }
    }

    private void sendListeningStart(Container container) {
        OutgoingMessage message = new OutgoingMessage();
        Log.d(TAG, "Starting listening for: " + container.getId());
        message.integer8(0x0c).integer32((int)container.getId()).writeTo(outputStream);


    }

    private void sendListeningStop(Container container) {
        OutgoingMessage message = new OutgoingMessage();
        Log.d(TAG, "Stopping networking for: " + container.getId());
        message.integer8(0x0d).integer32((int)container.getId()).writeTo(outputStream);
    }

    public CentralUnitConnection(URL url) {
        super(url);
        cm = ConnectionManager.getInstance();


            //Dummy Device:
/*
            Device device = new Device(this, 1, Device.Type.ACTUATOR, Device.ValueType.DECIMAL);
            device.changeDecimalValue(40);
            device.setName("Ceiling Lamp");
            //Dummy Device:
            Device device1 = new Device(this, 2, Device.Type.ACTUATOR, Device.ValueType.BINARY);
            device1.setName("Outdoor lights");
            device1.setDescription("Lights outside at the pool");
            //Dummy Device:
            Device device2 = new Device(this, 3, Device.Type.ACTUATOR, Device.ValueType.DECIMAL);
            device2.setName("Sauna lights");
*/

    }
    /**
     * Asks the central unit to change the value of the specified actuator device, of which value
     * type is binary. The method is called only by the
     * {@link com.opimobi.ohap.Device#changeBinaryValue(boolean)} method, which also verifies the
     * prerequisites for the request.
     * <p/>
     * This method is abstract and must be provided by the implementation.
     *
     * @param device The device of which state is asked to be changed.
     * @param value  The new binary value for the device.
     * @see com.opimobi.ohap.Device#changeBinaryValue(boolean)
     */
    @Override
    protected void changeBinaryValue(Device device, boolean value) {
        Log.d(TAG, "The binary value of the device " + device.getId() + " has changed to " + value);
        OutgoingMessage message = new OutgoingMessage();
        message.integer8(0x0a).integer32((int)device.getId()).binary8(value).writeTo(outputStream);

    }

    /**
     * Asks the central unit to change the value of the specified actuator device, of which value
     * type is decimal. The method is called only by the
     * {@link com.opimobi.ohap.Device#changeDecimalValue(double)} method, which also verifies the
     * prerequisites for the request.
     * <p/>
     * This method is abstract and must be provided by the implementation.
     *
     * @param device The device of which state is asked to be changed.
     * @param value  The new binary value for the device.
     * @see com.opimobi.ohap.Device#changeDecimalValue(double)
     */
    @Override
    protected void changeDecimalValue(Device device, double value) {
        Log.d(TAG, "The decimal value of the device " + device.getId() + " has changed to " + value);
        OutgoingMessage message = new OutgoingMessage();
        message.integer8(0x09).integer32((int)device.getId()).decimal64(value).writeTo(outputStream);
    }

    /**
     * Notifies the central unit that the specified container has started or stopped listening.
     * When a client is listening a container, the server sends updates when items are added
     * or removed from it as well as when a value of some of its child items has changed.
     * <p/>
     * This method is abstract and must be provided by the implementation.
     *
     * @param container The container of which listening state has changed.
     * @param listening The new listening state of the container.
     */
    @Override
    protected void listeningStateChanged(Container container, boolean listening) {
        Log.d(TAG, "The listening state has changed!");
        if(nListeners == 0) {
            Log.d(TAG, "The networking is starting.");
            nListeners++;
            Log.d(TAG, "There are listeners: " + nListeners);
            startNetworking();
        }

        else if(listening == true) {
            Log.d(TAG, "" +container + " Starts listening.");
            sendListeningStart(container);
        }

        else if(listening == false) {
            Log.d(TAG, "Stop listening now");
            sendListeningStop(container);
            nListeners--;
        }
        if(nListeners == 0)
           stopNetworking();
    }


}
