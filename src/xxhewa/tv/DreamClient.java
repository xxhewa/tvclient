package xxhewa.tv;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DreamClient {

    private String ipAddress;
    private String stringUrl;

    public static final int POWER_ON       = 1;
    public static final int STANDBY        = 2;
    public static final int FAST_FORWARD   = 3;
    public static final int FAST_BACKWARD  = 4;
    public static final int OK             = 5;
    public static final int PAUSE          = 6;

    public DreamClient(String ipAddress){
        this.ipAddress = ipAddress;
        this.stringUrl = "http://";
        this.stringUrl = this.stringUrl + this.ipAddress;
        this.stringUrl = this.stringUrl + "/web/";
    }

    public String sendCommand(int command) throws IOException {

        String currentUrl;

        switch (command){
            case POWER_ON:
                currentUrl = stringUrl + "powerstate?newstate=4";
                break;
            case STANDBY:
                currentUrl= stringUrl + "powerstate?newstate=5";
                break;
            default:
                throw new IllegalStateException("Unexpected command: " + command);
        }

        URL url = new URL(currentUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        con.connect();

        int status = con.getResponseCode();

        con.disconnect();

        return String.valueOf(status);
    }

    public String getMovieList() throws IOException {

        String currentUrl = stringUrl + "movielist";

        URL url = new URL(currentUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        con.connect();

        int status = con.getResponseCode();
        System.out.println("responseCode: "+status);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();

        con.disconnect();

        return content.toString();
    }

    public String getMostRecentMovieElement(String xmlDoc){
        String movieElement = "";
        InputStream stream = new ByteArrayInputStream(xmlDoc.getBytes(StandardCharsets.UTF_8));

        try {

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(stream);

            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            // System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

            NodeList nList = doc.getElementsByTagName("e2movie");

            // System.out.println("----------------------------");

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);

                // System.out.println("\nCurrent Element :" + nNode.getNodeName());

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;

                    String currentElement = eElement.getElementsByTagName("e2servicereference").item(0).getTextContent();

                    // System.out.println("MovieElement : " + currentElement);
                    if (movieElement.length()==0){
                        movieElement = currentElement;
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return movieElement;
    }

    public static void main(String[] args) {

        DreamClient dreamClient = new DreamClient("192.168.181.33");
        try {
            // dreamClient.sendCommand(POWER_ON);
            // Thread.sleep(5000);
            // dreamClient.sendCommand(STANDBY);

            String movieList = dreamClient.getMovieList();
            // System.out.println(movieList);
            System.out.println(dreamClient.getMostRecentMovieElement(movieList));
        }
        catch (IOException /*| InterruptedException*/ e){
            System.err.println("IOException"+e.getMessage());
        }

    }
}
