package main;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
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

/*
 * The original Geocode which prints out all the results of the addresses found.
 */
public class GeocoderOrig {

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

    public static void performGeocode(String address) throws IOException,
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
            System.out.println(resultNodeList.item(i).getTextContent());
        }

        // b) extract the locality for the first result
        resultNodeList = (NodeList) xpath.evaluate("/GeocodeResponse/result[1]/address_component[type/text()='locality']/long_name", geocoderResultDocument, XPathConstants.NODESET);
        for (int i = 0; i < resultNodeList.getLength(); ++i) {
            System.out.println(resultNodeList.item(i).getTextContent());
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
        System.out.println("address = " + address + "; " + "lat/lng=" + lat + "," + lng);

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
}