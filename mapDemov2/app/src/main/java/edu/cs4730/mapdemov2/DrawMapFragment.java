package edu.cs4730.mapdemov2;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import edu.cs4730.mapdemov2.databinding.DrawmapFragmentBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class DrawMapFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap map;
    DrawmapFragmentBinding binding;
    dataSet ds = null;

    public DrawMapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = DrawmapFragmentBinding.inflate(inflater, container, false);

        //in a fragment
        ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map3)).getMapAsync(this);
        //in an activity
        //((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);

        loadkml(R.raw.cats);
        return binding.getRoot();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        //now that we have the map, lets add things to it.
        if (ds != null) {
            Log.d("OnCreateView", "ds has been created.");
            Log.d("OnCreateView", "items in ds is " + ds.getSize());
            // now process the dataset for display.
            ArrayList<Placemark> pm = ds.getPlacemarks();
            for (Iterator<Placemark> iter = pm.iterator(); iter.hasNext(); ) {
                Placemark p = (Placemark) iter.next();
                PolygonOptions rectOptions = getCorr(p.coordinates);
                //In maps v2, we can't store information say a title or description in the here anymore or in polygon either.

                //rectOptions.strokeColor(Color.RED);  //line around area if needed.
                //rectOptions.strokeWidth(3); //default is 10
                rectOptions.strokeColor(Color.TRANSPARENT);
                rectOptions.clickable(true);  //so we can use a listener on the map.
                //rectOptions.fillColor(Color.BLUE)	;
                //Want a blue, but half transparent (so see map below), so alpha needs to be half of 256
                rectOptions.fillColor(Color.argb(128, 0, 0, 255));
                Polygon polygon = map.addPolygon(rectOptions);
                //we can use the settag to store information about the this area, which can be retreived
                // in the onPolygonClick listener.
                polygon.setTag(p);
            }
        }

        //As a mote we can add clickable to lines as well and then add a listener too.

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL); //normal map
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(MainActivity.LARAMIE, 10));
        map.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(@NonNull Polygon polygon) {
                //main problem with a polygon is it stores no information other then how to draw it
                //so the app now has to figure out where the polygon is and what to do with it.
                Log.v("MapDrawn", "click listener called.");
                Placemark p = (Placemark) polygon.getTag();
                Toast.makeText(getActivity(), "You clicked area: " + p.title, Toast.LENGTH_LONG).show();
            }
        });
    }

    void loadkml(int resource) {

        //first open the local file
        //getResources().openRawResource(resource);
        String logstring = "loadkml";

        Log.d(logstring, "before toString");
        try {
            Log.d(logstring, "before toString");
            // setup the url
            //URL url = new URL(urlString.toString());
            //URL url = new URL("http://www.cs.uwyo.edu/~seker/courses/4755/example/cats.kml");
            // create the factory
            Log.d(logstring, "before factory");
            SAXParserFactory factory = SAXParserFactory.newInstance();
            // create a parser
            Log.d(logstring, "before parser");
            SAXParser parser = factory.newSAXParser();
            // create the reader (scanner)
            Log.d(logstring, "before xmlreader");
            XMLReader xmlreader = parser.getXMLReader();
            //xmlreader.setFeature("http://xml.org/sax/features/namespaces", false);

            // instantiate our handler
            Log.d(logstring, "before navSaxHandler");
            saxHandler navSaxHandler = new saxHandler();
            // assign our handler
            Log.d(logstring, "setContent");
            xmlreader.setContentHandler(navSaxHandler);

            Log.d(logstring, "Opening stream");
            // get our data via the url class
            //InputSource is = new InputSource(url.openStream());
            InputSource is = new InputSource(getResources().openRawResource(resource));
            Log.d(logstring, "About to preform parse!");
            // perform the synchronous parse
            xmlreader.parse(is);
            Log.d(logstring, "parse done!");
            // get the results - should be a fully populated RSSFeed instance, or null on error
            ds = navSaxHandler.getParsedData();
            Log.d(logstring, "ds parse done!");
            Log.d(logstring, "No error!");
        } catch (Exception e) {
            Log.d(logstring, "Exception parsing kml." + e.getMessage());
        }
    }

    public PolygonOptions getCorr(String coordinates) {
        PolygonOptions myPath = new PolygonOptions();
        int i = 0;
        double x, y;
        Log.d("getCorr", "coor :" + coordinates + ":");
        String[] sl = coordinates.split(" ");
        String[] sl2;
        Log.d("getCorr", "sl length is " + sl.length);
        for (i = 0; i < (sl.length); i++) {
            Log.d("getCorr", "split :" + sl[i] + ":");
            if (sl[i].compareTo("") != 0) {
                sl2 = sl[i].split(",");
                x = Double.parseDouble(sl2[0]);  //long
                y = Double.parseDouble(sl2[1]);  //lat
                myPath.add(new LatLng(y, x));
                Log.d("getCorr", "Added to myPath");
            }
        }
        return myPath;
    }

    public PolygonOptions setupDemo() {
        PolygonOptions myPath = new PolygonOptions();
        int i = 0;
        double x, y;
        String firstlist = "-109.821859,44.700408,0 -109.81483,44.697809,0 -109.806039,44.694877,0 -109.793905,44.693171,0 -109.784293,44.688807,0 -109.775574,44.68603,0 -109.762393,44.686783,0 -109.756264,44.689996,0 -109.752983,44.696818,0 -109.746189,44.700403,0 -109.73945,44.703187,0 -109.733115,44.711883,0 -109.730273,44.719664,0 -109.725292,44.727421,0 -109.718727,44.734207,0 -109.711113,44.740029,0 -109.703399,44.750231,0 -109.700317,44.756484,0 -109.689809,44.759984,0 -109.672677,44.763706,0 -109.663246,44.765653,0 -109.647084,44.767178,0 -109.643848,44.771769,0 -109.641245,44.78014,0 -109.634219,44.792231,0 -109.624977,44.796063,0 -109.613442,44.795067,0 -109.603577,44.794775,0 -109.597247,44.797612,0 -109.589866,44.804037,0 -109.580362,44.808549,0 -109.566303,44.812146,0 -109.556632,44.814717,0 -109.547991,44.815099,0 -109.536312,44.819619,0 -109.525967,44.819117,0 -109.516724,44.816049,0 -109.5114,44.814996,0 -109.503322,44.813662,0 -109.490495,44.815705,0 -109.484772,44.816735,0 -109.472165,44.816936,0 -109.45938,44.817378,0 -109.445161,44.819766,0 -109.441782,44.817016,0 -109.438533,44.815866,0 -109.436516,44.813873,0 -109.433772,44.813221,0 -109.425962,44.808444,0 -109.423258,44.806318,0 -109.418665,44.803769,0 -109.415454,44.799738,0 -109.412921,44.798689,0 -109.412677,44.797199,0 -109.410993,44.796297,0 -109.410075,44.795203,0 -109.406864,44.793914,0 -109.405884,44.791701,0 -109.402306,44.790054,0 -109.400071,44.789777,0 -109.396621,44.789728,0 -109.393541,44.788825,0 -109.39204,44.786961,0 -109.389639,44.786435,0 -109.387252,44.785419,0 -109.385372,44.784778,0 -109.381388,44.785336,0 -109.379005,44.784196,0 -109.375492,44.785008,0 -109.372297,44.786266,0 -109.369085,44.788115,0 -109.364874,44.78995,0 -109.359819,44.792247,0 -109.358748,44.794837,0 -109.357323,44.798134,0 -109.354127,44.799391,0 -109.354243,44.801169,0 -109.351881,44.80232,0 -109.350995,44.804202,0 -109.348969,44.805239,0 -109.348403,44.8076,0 -109.348172,44.809847,0 -109.346621,44.81172,0 -109.346523,44.815154,0 -109.346408,44.819179,0 -109.346367,44.8206,0 -109.345987,44.822253,0 -109.343753,44.824708,0 -109.34339,44.825769,0 -109.343509,44.827429,0 -109.341128,44.829171,0 -109.339393,44.831634,0 -109.335848,44.833359,0 -109.334961,44.835241,0 -109.333243,44.837111,0 -109.330359,44.838964,0 -109.328311,44.840711,0 -109.325482,44.84067,0 -109.321464,44.84144,0 -109.318961,44.84164,0 -109.316135,44.84148,0 -109.313628,44.841798,0 -109.310612,44.842464,0 -109.308099,44.843019,0 -109.305907,44.843935,0 -109.303051,44.84484,0 -109.300544,44.845158,0 -109.300311,44.847405,0 -109.297433,44.849021,0 -109.295061,44.850407,0 -109.294506,44.852294,0 -109.292813,44.853216,0 -109.288982,44.853277,0 -109.286353,44.852053,0 -109.281937,44.849381,0 -109.280003,44.847219,0 -109.280102,44.843904,0 -109.279655,44.842121,0 -109.277722,44.839959,0 -109.274288,44.837894,0 -109.271172,44.836307,0 -109.26771,44.835188,0 -109.266269,44.833271,0 -109.262807,44.832152,0 -109.260177,44.831046,0 -109.256722,44.829691,0 -109.255416,44.828841,0 -109.253654,44.826564,0 -109.251682,44.825704,0 -109.249389,44.824485,0 -109.246561,44.824441,0 -109.243729,44.824516,0 -109.239748,44.8241,0 -109.23722,44.825127,0 -109.235014,44.826514,0 -109.233574,44.830045,0 -109.232174,44.832274,0 -109.231288,44.834037,0 -109.229725,44.836145,0 -109.225547,44.836673,0 -109.22053,44.837424,0 -109.21603,44.837591,0 -109.211682,44.838233,0 -109.205818,44.839444,0 -109.203107,44.840942,0 -109.2012,44.843399,0 -109.2008,44.845525,0 -109.204099,44.846524,0 -109.20535,44.84915,0 -109.206882,44.853438,0 -109.205502,44.854957,0 -109.20311,44.856933,0 -109.199071,44.858291,0 -109.195745,44.85812,0 -109.191747,44.858175,0 -109.187608,44.857398,0 -109.184013,44.855209,0 -109.180237,44.853491,0 -109.177448,44.852143,0 -109.175504,44.850454,0 -109.173185,44.850061,0 -109.170454,44.85215,0 -109.167758,44.853173,0 -109.164244,44.853708,0 -109.163684,44.855595,0 -109.16264,44.856999,0 -109.161044,44.860053,0 -109.158188,44.860837,0 -109.154303,44.862551,0 -109.151447,44.863334,0 -109.149385,44.865314,0 -109.146067,44.864905,0 -109.143554,44.865338,0 -109.14086,44.866241,0 -109.138616,44.868692,0 -109.139307,44.873086,0 -109.140184,44.876891,0 -109.141092,44.879749,0 -109.141701,44.881535,0 -109.139421,44.885052,0 -109.13483,44.887938,0 -109.129927,44.890109,0 -109.125276,44.889677,0 -109.122626,44.88916,0 -109.118467,44.888973,0 -109.114173,44.88899,0 -109.111511,44.889198,0 -109.10866,44.88978,0 -109.108067,44.891658,0 -109.108174,44.893798,0 -109.110248,44.895343,0 -109.110161,44.897983,0 -109.108297,44.900847,0 -109.10749,44.903853,0 -109.11079,44.905795,0 -109.11463,44.907494,0 -109.115605,44.910153,0 -109.118688,44.913349,0 -109.121792,44.915917,0 -109.126344,44.917502,0 -109.132239,44.921373,0 -109.135629,44.926084,0 -109.136726,44.930506,0 -109.136221,44.935153,0 -109.134046,44.942164,0 -109.131025,44.947902,0 -109.128435,44.951257,0 -109.129853,44.956691,0 -109.130484,44.959092,0 -109.132544,44.961138,0 -109.133147,44.96442,0 -109.131925,44.969307,0 -109.127966,44.971129,0 -109.1239,44.970811,0 -109.113868,44.974043,0 -109.109183,44.976356,0 -109.106801,44.978707,0 -109.107423,44.98136,0 -109.105585,44.983343,0 -109.101122,44.984275,0 -109.098587,44.985869,0 -109.093284,44.985403,0 -109.088606,44.987463,0 -109.085725,44.988799,0 -109.082088,44.991506,0 -109.081637,44.994393,0 -109.081752,44.996282,0 -109.081313,44.998791,0 -109.084115,44.999845,0 -109.087791,45.00129,0 -109.096549,45.004582,0 -109.102332,45.006692,0 -109.122198,45.006643,0 -109.133184,45.00694900000001,0 -109.161035,45.006772,0 -109.190659,45.006616,0 -109.206097,45.006356,0 -109.226849,45.006303,0 -109.24902,45.006268,0 -109.264451,45.006252,0 -109.281658,45.006135,0 -109.306676,45.005753,0 -109.325656,45.005656,0 -109.345522,45.005568,0 -109.366094,45.005613,0 -109.380638,45.005568,0 -109.393059,45.005366,0 -109.419321,45.004852,0 -109.439362,45.004876,0 -109.467045,45.004245,0 -109.494187,45.003977,0 -109.517605,45.003654,0 -109.537297,45.00340499999999,0 -109.547409,45.003282,0 -109.556993,45.00302600000001,0 -109.563913,45.002862,0 -109.572607,45.002719,0 -109.580579,45.00306999999999,0 -109.589627,45.002931,0 -109.598855,45.002667,0 -109.608429,45.00278399999999,0 -109.621558,45.002566,0 -109.639113,45.00265000000001,0 -109.6533,45.002693,0 -109.664472,45.002698,0 -109.67529,45.002698,0 -109.68664,45.002704,0 -109.696575,45.002566,0 -109.706326,45.00267700000001,0 -109.722286,45.002731,0 -109.732046,45.002462,0 -109.744105,45.002469,0 -109.759184,45.00225700000001,0 -109.773019,45.002155,0 -109.789686,45.002333,0 -109.793941,45.00237800000001,0 -109.805098,45.003124,0 -109.812718,45.00345500000001,0 -109.822647,45.003558,0 -109.835588,45.003816,0 -109.851551,45.003726,0 -109.87443,45.003703,0 -109.888617,45.003716,0 -109.92001,45.003639,0 -109.953177,45.003445,0 -109.974458,45.00351199999999,0 -109.999988,45.00399,0 -110.000266,45.000082,0 -110.0001,44.986505,0 -109.999712,44.974068,0 -109.993514,44.971584,0 -109.990093,44.971554,0 -109.983453,44.971352,0 -109.977807,44.971874,0 -109.972797,44.970542,0 -109.968341,44.972074,0 -109.966186,44.968768,0 -109.963022,44.965595,0 -109.960055,44.96271,0 -109.958714,44.958982,0 -109.955357,44.955379,0 -109.952791,44.95264,0 -109.9488,44.950746,0 -109.94665,44.947296,0 -109.943096,44.943548,0 -109.939959,44.939088,0 -109.937582,44.937066,0 -109.931171,44.935577,0 -109.926574,44.933961,0 -109.926836,44.930677,0 -109.928091,44.928116,0 -109.927334,44.925537,0 -109.92901,44.92198,0 -109.927676,44.917966,0 -109.927542,44.914392,0 -109.928415,44.910685,0 -109.929296,44.906549,0 -109.930587,44.901988,0 -109.929508,44.895118,0 -109.929987,44.890978,0 -109.93126,44.887417,0 -109.934348,44.883444,0 -109.936233,44.879318,0 -109.9359,44.875599,0 -109.935175,44.871305,0 -109.935452,44.867163,0 -109.936718,44.863888,0 -109.936789,44.86003,0 -109.939044,44.857621,0 -109.938713,44.85376,0 -109.936752,44.851169,0 -109.934814,44.847292,0 -109.932858,44.844416,0 -109.930892,44.842111,0 -109.929138,44.839236,0 -109.927576,44.836792,0 -109.933233,44.834987,0 -109.936095,44.832299,0 -109.940343,44.830623,0 -109.941993,44.828209,0 -109.946262,44.82539,0 -109.950108,44.823711,0 -109.954594,44.819893,0 -109.956648,44.81734,0 -109.956697,44.814625,0 -109.95618,44.809904,0 -109.952874,44.804014,0 -109.956533,44.801476,0 -109.949568,44.798125,0 -109.944611,44.794792,0 -109.938,44.794159,0 -109.930802,44.792663,0 -109.927661,44.788918,0 -109.922304,44.78558,0 -109.918562,44.781829,0 -109.915427,44.777798,0 -109.910947,44.77061,0 -109.906027,44.765561,0 -109.899671,44.762213,0 -109.895303,44.759884,0 -109.891509,44.75899,0 -109.888531,44.757389,0 -109.885375,44.754642,0 -109.885452,44.750642,0 -109.884309,44.747486,0 -109.883384,44.743476,0 -109.880248,44.739729,0 -109.875673,44.737826,0 -109.871132,44.734209,0 -109.864909,44.734576,0 -109.863158,44.731843,0 -109.860837,44.727533,0 -109.858071,44.725504,0 -109.855293,44.724047,0 -109.853484,44.724315,0 -109.848453,44.725265,0 -109.843053,44.724638,0 -109.837439,44.724724,0 -109.834258,44.723406,0 -109.833153,44.718535,0 -109.831567,44.717662,0 -109.830274,44.712218,0 -109.826095,44.710746,0 -109.823333,44.708573,0 -109.825981,44.706457,0 -109.825424,44.704308,0 -109.825258,44.702591,0 -109.824477,44.701582,0 -109.821859,44.700408,0";
        String[] sl = firstlist.split(" ");
        String[] sl2;
        for (i = 0; i < sl.length; i++) {
            sl2 = sl[i].split(",");
            x = Double.parseDouble(sl2[0]);
            y = Double.parseDouble(sl2[1]);
            myPath.add(new LatLng(y, x));
        }
        return myPath;
    }
}
