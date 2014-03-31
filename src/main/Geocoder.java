package main;

import java.io.File;
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

/*
 * This one for performing silent Geocoding without printing out all the results.
 */
public class Geocoder {

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
    
    /*
     * Read ad tax sales file and create a new file with lat long coordinates.
     */
    public void fillLatLongCoords() {
        System.out.println("Enter the file name that you want to fill in the lat and long coordinates for: ");
        String fileNameToFillLatLongCoords = RWTD.userScan.next();
        File f = new File(fileNameToFillLatLongCoords);
        String colLabels[]; //Holds only the column names at the top of the .csv
        String cols[]; //Holds the actual data separated by commas of the .csv
        String outString = ""; //outString will be written to the new file with the lat long coordinates included.

        int addressColumnIndex = -1;
        int zipColumnIndex = -1;
        
        int latColIdx = -1;
        int lngColIdx = -1;

        try {
            Scanner s = new Scanner(f);
            
            //First we put the first line with the column labels on the output String.
            outString += s.nextLine();
            
            colLabels = outString.split(",");
            
            //Search for the address and zip column indexes, because it could
            //change.
            for (int i = 0; i < colLabels.length; i++) {
                switch (colLabels[i]) {
                    case "AddressSite":
                        addressColumnIndex = i;
                        break;
                    case "ZipSite":
                        zipColumnIndex = i;
                        break;
                    case "Latitude":
                        latColIdx = i;
                        break;
                    case "Longitude":
                        lngColIdx = i;
                        break;
                }
            }
            
            //If we didn't find either one of the columns, we abort geocoding.
            if (addressColumnIndex == -1 || zipColumnIndex == -1 || latColIdx == -1 || lngColIdx == -1) {
                System.out.println("Error, aborting Geocode. Address, zip, lat, or long column was not found.");
                return;
            }
            
            //After we've added all the column labels,
            //add a new line to complete the first row of the out string.
            outString += System.lineSeparator();
            
            //Next, we go through the rest of the .csv.
            while (s.hasNextLine()) { //For each row

                cols = s.nextLine().split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"); //This regex splits by commas, skipping over quotes
                
                //Perform geocode with the street address and zip, which is apparantly all that's needed.
                this.performGeocode(cols[addressColumnIndex] + " " + cols[zipColumnIndex]);
                Thread.sleep(500); //Add a delay because Google limits how fast we can make geocoding requests.
                
                //Filling the columns of each row with all the old data,
                //but including the lat long coordinates as well.
                for (int i = 0; i < cols.length; i++) {
                    //If i matches the lat or long column index found when we
                    //performed the search, then write their values
                    if (i == latColIdx) {
                        outString += this.getLat() + ",";
                    }
                    else if (i == lngColIdx) {
                        outString += this.getLng() + ",";
                    }
                    else { //Else write the old data
                        //The conditional checks if it's before or equal to the
                        //2nd to last column. If it is, add a comma.
                        //If not, no need to add a comma after the last column.
                        outString += cols[i] + ((i <= cols.length - 2) ? "," : "");
                    }
                }
                //New line starting the next row
                outString += System.lineSeparator();
            }

            //Cut off the .csv and concantenate WithCoords.csv.
            String outputFileName = fileNameToFillLatLongCoords.substring(0, fileNameToFillLatLongCoords.length() - 4) + "WithCoords.csv";
            FileWriter fw = new FileWriter(new File(outputFileName));
            //The outString contains the new .csv contents with the coordinates included.
            fw.write(outString);

            fw.close();
            
            System.out.println("Wrote lat long coordinates to " + outputFileName);
            
        }
        catch (IOException | XPathExpressionException | ParserConfigurationException | SAXException | InterruptedException ex) {
            Logger.getLogger(GeocoderOrig.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}