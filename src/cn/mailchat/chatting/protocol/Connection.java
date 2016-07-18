package cn.mailchat.chatting.protocol;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import cn.mailchat.GlobalConstants;
import cn.mailchat.R;
import cn.mailchat.utils.SystemUtil;

import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;


public class Connection {
	public static final String TAG = "MQTT";
	
    public static final String MQTT_STATUS_INTENT = "org.eclipse.paho.android.service.STATUS";
    public static final String MQTT_STATUS_CODE = "cn.mailchat.chatting.protocol.STATUS_CODE";
    public static final String MQTT_STATUS_MSG = "cn.mailchat.chatting.protocol.STATUS_MSG";
	private static Connection instance = null;
	/** ClientHandle for this Connection Object **/
	private static String clientHandle = null;

	/** The {@link Context} of the application this object is part of **/
	private static Context context = null;

	/** The {@link MqttConnectOptions} that were used to connect this client **/
	private MqttConnectOptions conOpt;
	
	/** True if this connection is secured using SSL **/
	private static boolean sslConnection = true;
	private static String clientId;
	private static String host;
	private static int port;

	private static MqttAndroidClient client;

	/** Persistence id, used by {@link Persistence} **/
	private long persistenceId = -1;
    /**
     * {@link ConnectionStatus} of the {@link MqttAndroidClient} represented by this <code>Connection</code> object. Default value is {@link ConnectionStatus#NONE}
     **/
    private ConnectionStatus connectionStatus = ConnectionStatus.INITIAL;
    private Timestamp connectionStatusChangeTime;
	  /**
     * Connections status for  a connection
     */
    public enum ConnectionStatus {
        INITIAL,
        /**
         * Client is Connecting
         **/
        CONNECTING,
        /**
         * Client is Connected
         **/
        CONNECTED,
        /**
         * Client is Disconnecting
         **/
        DISCONNECTING,
        /**
         * Client is Disconnected
         **/
        DISCONNECTED,
        /**
         * Client has encountered an Error
         **/
        ERROR,
        /**
         * Status is unknown
         **/
        NONE
    }

	/** Collection of {@link PropertyChangeListener} **/
	private ArrayList<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();

	public synchronized static Connection getInstance(Context context) {
		clientId = SystemUtil.getCliendId(context);
		Log.i("MQTT", "clientId:"+clientId);
	    if (instance == null) {
			host = GlobalConstants.MQTT_HOST;
			port = GlobalConstants.MQTT_PORT;
			String uri = getUri();
			clientHandle = uri + clientId;
	    	client = new MqttAndroidClient(context, uri, clientId);
	    	instance =new Connection(clientHandle, clientId, host, port, context, client,sslConnection);
	    }	  
		return instance;
	}

	public Connection(String clientHandle, String clientId, String host,
			int port, Context context, MqttAndroidClient client,
			boolean sslConnection) {
		// generate the client handle from its hash code
		this.clientHandle = clientHandle;
		this.clientId = clientId;
		this.host = host;
		this.port = port;
		this.context = context;
		this.client = client;
		this.sslConnection = sslConnection;
        changeAndSendbroadcastStatus(ConnectionStatus.CONNECTING);
	}

	/**
	 * Gets the client handle for this connection
	 * 
	 * @return client Handle for this connection
	 */
	public String handle() {
		return clientHandle;
	}



	/**
	 * A string representing the state of the client this connection object
	 * represents
	 * 
	 * 
	 * @return A string representing the state of the client
	 */
	 @Override
	 public String toString() {
	 StringBuffer sb = new StringBuffer();
	 sb.append(clientId);
	 sb.append("\n ");
	
	 switch (connectionStatus) {
	
	 case CONNECTED:
	 sb.append(context.getString(R.string.connectedto));
	 break;
	 case DISCONNECTED:
	 sb.append(context.getString(R.string.disconnected));
	 break;
	 case NONE:
	 sb.append(context.getString(R.string.no_status));
	 break;
	 case CONNECTING:
	 sb.append(context.getString(R.string.connecting));
	 break;
	 case DISCONNECTING:
	 sb.append(context.getString(R.string.disconnecting));
	 break;
	 case ERROR:
	 sb.append(context.getString(R.string.connectionError));
	 }
	 sb.append(" ");
	 sb.append(host);
	
	 return sb.toString();
	 }

	/**
	 * Get the client Id for the client this object represents
	 * 
	 * @return the client id for the client this object represents
	 */
	public String getId() {
		return clientId;
	}

	/**
	 * Get the host name of the server that this connection object is associated
	 * with
	 * 
	 * @return the host name of the server this connection object is associated
	 *         with
	 */
	public String getHostName() {

		return host;
	}

	
	
	/**
	 * Gets the client which communicates with the android service.
	 * 
	 * @return the client which communicates with the android service
	 */
	public MqttAndroidClient getClient() {
		return client;
	}

	/**
	 * Add the connectOptions used to connect the client to the server
	 * 
	 * @param connectOptions
	 *            the connectOptions used to connect to the server
	 */
	public void addConnectionOptions(MqttConnectOptions connectOptions) {
		conOpt = connectOptions;

	}

	/**
	 * Get the connectOptions used to connect this client to the server
	 * 
	 * @return The connectOptions used to connect the client to the server
	 */
	public MqttConnectOptions getConnectionOptions() {
		return conOpt;
	}

	/**
	 * Register a {@link PropertyChangeListener} to this object
	 * 
	 * @param listener
	 *            the listener to register
	 */
	public void registerChangeListener(PropertyChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove a registered {@link PropertyChangeListener}
	 * 
	 * @param listener
	 *            A reference to the listener to remove
	 */
	public void removeChangeListener(PropertyChangeListener listener) {
		if (listener != null) {
			listeners.remove(listener);
		}
	}

	/**
	 * Notify {@link PropertyChangeListener} objects that the object has been
	 * updated
	 * 
	 * @param propertyChangeEvent
	 */
	private void notifyListeners(PropertyChangeEvent propertyChangeEvent) {
		for (PropertyChangeListener listener : listeners) {
			listener.propertyChange(propertyChangeEvent);
		}
	}


	/**
	 * Determines if the connection is secured using SSL, returning a C style
	 * integer value
	 * 
	 * @return 1 if SSL secured 0 if plain text
	 */
	public int isSSL() {
		return sslConnection ? 1 : 0;
	}

	/**
	 * Assign a persistence ID to this object
	 * 
	 * @param id
	 *            the persistence id to assign
	 */
	public void assignPersistenceId(long id) {
		persistenceId = id;
	}

	/**
	 * Returns the persistence ID assigned to this object
	 * 
	 * @return the persistence ID assigned to this object
	 */
	public long persistenceId() {
		return persistenceId;
	}
	
	//更改连接设置
	//start
	public static String getClientHandle() {
		return clientHandle;
	}

	public  static void setClientHandle(String clientHandle) {
		Connection.clientHandle = clientHandle;
	}

	public static String getHost() {
		return host;
	}

	public static void setHost(String host) {
		Connection.host = host;
	}

	public static int getPort() {
		return port;
	}

	public static void setPort(int port) {
		Connection.port = port;
	}

	public static String getUri(){
		String uri = null;
		if (sslConnection) {
			uri = "ssl://" + host + ":" + port;
		} else {
			uri = "tcp://" + host + ":" + port;
		}
		return uri;
	}

	public static String getClientId() {
		return clientId;
	}

	public static void setClient(String host,int post) {
		Connection.client = new MqttAndroidClient(context, getUri(), clientId);
	}
	//end
	
	
	
    /**
     * Changes the connection status of the client
     *
     * @param connectionStatus The connection status of this connection
     */
    public void changeAndSendbroadcastStatus(ConnectionStatus connectionStatus) {
        this.connectionStatus = connectionStatus;
        connectionStatusChangeTime = new Timestamp(new Date().getTime());
        rebroadcastStatus();
    }
    private String getConnectionChangeTimestamp() {
        return connectionStatusChangeTime.toString();
    }
	public void rebroadcastStatus() {
        String status = "";
        switch (connectionStatus) {
            case INITIAL:
                status = "Please wait";
                break;
            case CONNECTING:
                status = "Connecting @ " + getConnectionChangeTimestamp();
                break;
            case CONNECTED:
                status = "Connected @ " + getConnectionChangeTimestamp();
                break;
            case DISCONNECTING:
                status = "Disconnecting @ "
                        + getConnectionChangeTimestamp();
                break;
            case DISCONNECTED:
                status = "Disconnected @ " + getConnectionChangeTimestamp();
                break;
            case ERROR:
                status = "Client has encountered an Error @ "
                        + getConnectionChangeTimestamp();
                break;
            case NONE:
                status = "Status is unknown @ " + getConnectionChangeTimestamp();
                break;
        }
        // inform the app that the Service has successfully connected
        broadcastServiceStatus(status);
    }
    public void broadcastServiceStatus(String statusDescription) {
        // inform the app (for times when the Activity UI is running /
        // active) of the current MQTT connection status so that it
        // can update the UI accordingly
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MQTT_STATUS_INTENT);
        broadcastIntent.putExtra(MQTT_STATUS_CODE, connectionStatus.ordinal());
        broadcastIntent.putExtra(MQTT_STATUS_MSG, statusDescription);
        context.sendBroadcast(broadcastIntent);
    }
}
