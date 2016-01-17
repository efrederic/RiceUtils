package com.taken.riceutils;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by andrew on 1/17/16.
 */
public class GetShoutoutsTask extends AsyncTask<String, Void, String> {

    private GoogleMap mMap;
    private final String KEY_TEXT = "text";
    private final String KEY_LAT = "latatitute";
    private final String KEY_LNG = "longitude";

    public GetShoutoutsTask(GoogleMap map) {
        mMap = map;
    }

    @Override
    protected String doInBackground(String... urlStr) {
        String xmldata = "";
        try {
            URL url = new URL(urlStr[0]);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                byte[] contents = new byte[1024];

                int bytesRead = 0;
                while ((bytesRead = in.read(contents)) != -1) {
                    xmldata += new String(contents, 0, bytesRead);
                }
                Log.d("RICEUTILS", xmldata);
            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            Log.e("RICEUTILS", e.toString());
        }
        return xmldata;
    }

    @Override
    protected void onPostExecute(String xmldata) {
        if (!xmldata.equals("")) {
            // parse xml, and do stuff on mMap...
            // clear pins here...
            // for now, we'll just hard-code stuff in like a boss
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                InputSource is = new InputSource();
                is.setCharacterStream(new StringReader(xmldata));
                Document doc = db.parse(is);
                NodeList nodes = doc.getElementsByTagName("post");

                for (int i = 0; i < nodes.getLength(); i++) {
                    Node nNode = nodes.item(i);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        String text = eElement.getElementsByTagName("text").item(0).getTextContent();
                        String lat = eElement.getElementsByTagName("latitude").item(0).getTextContent();
                        String lng = eElement.getElementsByTagName("longitude").item(0).getTextContent();

                        LatLng latLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                        MarkerOptions mOptions = new MarkerOptions()
                                .position(latLng)
                                .title(text)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_person_pin_black_24dp));
                        MainActivity.shoutoutMarkers.add(mMap.addMarker(mOptions));
                    }
                }
            }catch (Exception e){
                //Handling exceptions is for wusses
            }

//            String[] shoutoutData = {"Party over here","Party over there","Shoutout to pears","Shoutout to pears again","So many flavors"};
//            String[] latitudes = {"29.713845", "29.714167", "29.715122", "29.715332", "29.716301"};
//            String[] longitudes = {"-95.406353", "-95.406224", "-95.405237", "-95.404684", "-95.402195"};
//            for (int i=0; i<latitudes.length; i++){
//                LatLng latLng = new LatLng(Double.parseDouble(latitudes[i]), Double.parseDouble(longitudes[i]));
//                mMap.addMarker(new MarkerOptions()
//                        .position(latLng)
//                        .title(shoutoutData[i]))
//                        .showInfoWindow();
//            }
        }
    }
}
