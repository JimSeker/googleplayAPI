package edu.cs4730.mapdemov2;

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*
 * This parser is ignoring a large about of data in the kml file.
 * But otherwise, it create a dataset to placed on a map.
 *
 * Bases of this code comes from http://codemagician.wordpress.com/2010/05/06/android-google-mapview-tutorial-done-right/
 * There was more then a bit of modification though.
 */

public class saxHandler extends DefaultHandler {

    // ===========================================================
    // Fields
    // ===========================================================

    private boolean in_kmltag = false;
    private boolean in_placemarktag = false;
    private boolean in_nametag = false;
    private boolean in_descriptiontag = false;
    private boolean in_geometrycollectiontag = false;
    private boolean in_linestringtag = false;
    private boolean in_pointtag = false;
    private boolean in_coordinatestag = false;

    private StringBuffer buffer;

    private dataSet DataSet = new dataSet();

    private Placemark tmp = null;

    // ===========================================================
    // Getter
    // ===========================================================

    public dataSet getParsedData() {
        return DataSet;
    }

    // ===========================================================
    // Methods
    // ===========================================================
    @Override
    public void startDocument() throws SAXException {
        this.DataSet = new dataSet();
    }

    @Override
    public void endDocument() throws SAXException {
        // Nothing to do
    }

    /**
     * Gets be called on opening tags like:
     * <tag>
     * Can provide attribute(s), when xml was like:
     * <tag attribute="attributeValue">
     */
    @Override
    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes atts) throws SAXException {
        Log.d("SaxHandler", "startElement is " + localName);
        switch (localName) {
            case "kml":
                this.in_kmltag = true;
                break;
            case "Placemark":
                this.in_placemarktag = true;
                tmp = new Placemark();  //begin building the placemark to be added to the dataset.
                break;
            case "name":
                this.in_nametag = true;
                break;
            case "description":
                this.in_descriptiontag = true;
                break;
            case "GeometryCollection":    //don't care...
                this.in_geometrycollectiontag = true;
                break;
            case "LineString":  //don't care...
                this.in_linestringtag = true;
                break;
            case "point":  //don't care...
                this.in_pointtag = true;
                break;
            case "coordinates":
                buffer = new StringBuffer();
                this.in_coordinatestag = true;
                break;
        }
    }

    /**
     * Gets be called on closing tags like:
     * </tag>
     */
    @Override
    public void endElement(String namespaceURI, String localName, String qName)
        throws SAXException {
        switch (localName) {
            case "kml":
                this.in_kmltag = false;  //we are done!
                break;
            case "Placemark":
                this.in_placemarktag = false;
                //end of a group, start over
                DataSet.addPlacemark(tmp);
                tmp = null;
                break;
            case "name":
                this.in_nametag = false;
                break;
            case "description":
                this.in_descriptiontag = false;
                break;
            case "GeometryCollection":   // don't have any
                this.in_geometrycollectiontag = false;
                break;
            case "LineString":  //should not have any...
                this.in_linestringtag = false;
                break;
            case "point":   //should not have any...
                this.in_pointtag = false;
                break;
            case "coordinates":
                tmp.setCoordinates(buffer.toString().trim());
                Log.d("SaxHandler", "Coor: " + tmp.getCoordinates());
                this.in_coordinatestag = false;
                buffer = null;
                break;
        }
    }

    /**
     * Gets be called on the following structure:
     * <tag>characters</tag>
     */
    @Override
    public void characters(char[] ch, int start, int length) {
        if (this.in_nametag) {  //attempt to deal with bad xml?
            if (tmp == null)
                tmp = new Placemark();
            tmp.setTitle(new String(ch, start, length));
            Log.d("SaxHandler", "name is " + tmp.getTitle());
        } else if (this.in_descriptiontag) {
            if (tmp == null)
                tmp = new Placemark();
            tmp.setDescription(new String(ch, start, length));
            //Log.d("SaxHandler","name is "+tmp.getDescription());  Not sure I care actually...
        } else if (this.in_coordinatestag) {
            if (tmp == null)
                tmp = new Placemark();
            //maybe on several lines, so add to the buffer, when ends, then place it in tmp.
            buffer.append(ch, start, length);
            //Log.d("SaxHandler","buffer:"+buffer);
        }
    }
}
