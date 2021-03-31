package xxhewa.tv;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.net.HttpURLConnection.HTTP_OK;

public class VieraClient {

    private String ipAddress;

    public static final int OK          = 1;
    public static final int VOLUME_UP   = 2;
    public static final int VOLUME_DOWN = 3;

    public VieraClient(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void sendCommand(int command) throws IOException {

        String keyPress;

        switch (command){
            case OK:
                keyPress = "NRC_ENTER-ONOFF";
                break;
            case VOLUME_UP:
                keyPress = "NRC_VOLUP-ONOFF";
                break;
            case VOLUME_DOWN:
                keyPress = "NRC_VOLDOWN-ONOFF";
                break;
            default:
                throw new IllegalStateException("Unexpected command: " + command);
        }

        String urn = "panasonic-com:service:p00NetworkControl:1";
        String actionXmlFragment =
                "<u:X_SendKey xmlns:u=\"urn:"+urn+"\">\n"+
                "<X_KeyEvent>"+keyPress+"</X_KeyEvent>\n"+
                "</u:X_SendKey>";
        String stringURL = "http://"+this.ipAddress+":55000/nrc/control_0";
        URL url = new URL(stringURL);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setConnectTimeout(5000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
            connection.setRequestProperty("SOAPACTION", "\"urn:"+urn+"#X_SendKey\"");
            connection.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
              "  <s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n"+
                "    <s:Body>\n"+
                  actionXmlFragment+"\n"+
                "    </s:Body>\n"+
               "  </s:Envelope>");
            writer.close();
            int status = connection.getResponseCode();
            if (status != HTTP_OK) {
                throw new IOException("Unexpected HTTP status "+status);
            }
        } finally {
            connection.disconnect();
        }
    }

    public static void main(String[] args) {

        VieraClient vieraClient = new VieraClient("192.168.181.72");
        try {
            vieraClient.sendCommand(OK);
        } catch (IOException /*| InterruptedException*/ e) {
            System.err.println("IOException" + e.getMessage());
        }
    }

}
