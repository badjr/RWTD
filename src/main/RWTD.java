/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

/**
 *
 * @author Brett
 */
public class RWTD {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        TaxSalesSearchResultsURLParser searchResultParser = new TaxSalesSearchResultsURLParser();
        searchResultParser.grabSearchResultsURLs();
        
    }
}
