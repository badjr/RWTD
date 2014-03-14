package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class GeocodingSample {

    private static float lat;
    private static float lng;

    public float getLat() {
        return lat;
    }

    public float getLng() {
        return lng;
    }
    // URL prefix to the geocoder
    private static final String GEOCODER_REQUEST_PREFIX_FOR_XML = "http://maps.google.com/maps/api/geocode/xml";

    public void performGeocode(String address) throws IOException,
            XPathExpressionException, ParserConfigurationException, SAXException {

        // query address
//    address = "3000 Main St";

        // prepare a URL to the geocoder
        URL url = new URL(GEOCODER_REQUEST_PREFIX_FOR_XML + "?address=" + URLEncoder.encode(address, "UTF-8") + "&sensor=false");

        // prepare an HTTP connection to the geocoder
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        Document geocoderResultDocument = null;
        try {
            // open the connection and get results as InputSource.
            conn.connect();
            InputSource geocoderResultInputSource = new InputSource(conn.getInputStream());

            // read result and parse into XML Document
            geocoderResultDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(geocoderResultInputSource);
        }
        finally {
            conn.disconnect();
        }

        // prepare XPath
        XPath xpath = XPathFactory.newInstance().newXPath();

        // extract the result
        NodeList resultNodeList = null;

        // a) obtain the formatted_address field for every result
        resultNodeList = (NodeList) xpath.evaluate("/GeocodeResponse/result/formatted_address", geocoderResultDocument, XPathConstants.NODESET);
        for (int i = 0; i < resultNodeList.getLength(); ++i) {
//            System.out.println(resultNodeList.item(i).getTextContent());
        }

        // b) extract the locality for the first result
        resultNodeList = (NodeList) xpath.evaluate("/GeocodeResponse/result[1]/address_component[type/text()='locality']/long_name", geocoderResultDocument, XPathConstants.NODESET);
        for (int i = 0; i < resultNodeList.getLength(); ++i) {
//            System.out.println(resultNodeList.item(i).getTextContent());
        }

        // c) extract the coordinates of the first result
        resultNodeList = (NodeList) xpath.evaluate("/GeocodeResponse/result[1]/geometry/location/*", geocoderResultDocument, XPathConstants.NODESET);
//        float lat = Float.NaN;
//        float lng = Float.NaN;
        lat = Float.NaN;
        lng = Float.NaN;
        for (int i = 0; i < resultNodeList.getLength(); ++i) {
            Node node = resultNodeList.item(i);
            if ("lat".equals(node.getNodeName())) {
                lat = Float.parseFloat(node.getTextContent());
            }
            if ("lng".equals(node.getNodeName())) {
                lng = Float.parseFloat(node.getTextContent());
            }
//            lat = Float.parseFloat(node.getTextContent());
//            lng = Float.parseFloat(node.getTextContent());

        }
//        System.out.println("address = " + address + "; " + "lat/lng=" + lat + "," + lng);

        // c) extract the coordinates of the first result
//    resultNodeList = (NodeList) xpath.evaluate("/GeocodeResponse/result[1]/address_component[type/text() = 'administrative_area_level_1']/country[short_name/text() = 'US']/*", geocoderResultDocument, XPathConstants.NODESET);
//    lat = Float.NaN;
//    lng = Float.NaN;
//    for(int i=0; i<resultNodeList.getLength(); ++i) {
//      Node node = resultNodeList.item(i);
//      if("lat".equals(node.getNodeName())) {
//            lat = Float.parseFloat(node.getTextContent());
//        }
//      if("lng".equals(node.getNodeName())) {
//            lng = Float.parseFloat(node.getTextContent());
//        }
//    }
//    System.out.println("lat/lng=" + lat + "," + lng);

    }

    public void fillLatLongCoords() {
        File f = new File("AdsTaxSaleProdFultonSep13GATest.csv");
        String cols[];
        String outString = "";
        try {
            Scanner s = new Scanner(f);
            int i = 2;
            outString += s.nextLine() + System.lineSeparator();
            while (s.hasNextLine()) {

                cols = s.nextLine().split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"); //Split by commas, skipping over quotes
//                System.out.println((i++) + ": " + cols[14]);
//                System.out.println(cols[14] + " " + cols[15]);
                this.performGeocode(cols[14] + " " + cols[15]);
                Thread.sleep(1000);
                for (int j = 0; j < cols.length; j++) {
                    if (j == 12) {
                        outString += this.getLat() + ",";
                    }
                    else if (j == 13) {
                        outString += this.getLng() + ",";
                    }
                    else {
                        outString += cols[j] + ((j <= cols.length - 2) ? "," : "");
                    }
                }
                outString += System.lineSeparator();
            }
//            System.out.println(outString);

            FileWriter fw = new FileWriter(new File("AdsTaxSaleProdFultonSep13GATestWithCoords.csv"));

            fw.write(outString);

            fw.close();

        }
        catch (IOException | XPathExpressionException | ParserConfigurationException | SAXException | InterruptedException ex) {
            Logger.getLogger(GeocodingSample.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}