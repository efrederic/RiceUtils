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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by andrew on 1/17/16.
 */
public class GetShoutoutsTask extends AsyncTask<String, Void, String> {

    private GoogleMap mMap;
    private static final String KEY_TEXT = "text";
    private static final String KEY_LAT = "latitude";
    private static final String KEY_LNG = "longitude";
    private MainActivity mainActivity;

    public GetShoutoutsTask(GoogleMap map, MainActivity mainActivity) {
        mMap = map;
        this.mainActivity = mainActivity;
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
            MainActivity.shoutoutMarkers.clear();
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
                        String text = eElement.getElementsByTagName(KEY_TEXT).item(0).getTextContent();
                        String lat = eElement.getElementsByTagName(KEY_LAT).item(0).getTextContent();
                        String lng = eElement.getElementsByTagName(KEY_LNG).item(0).getTextContent();

                        LatLng latLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                        MarkerOptions mOptions = new MarkerOptions()
                                .position(latLng)
                                .title(text)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_person_pin_black_24dp));
                        MainActivity.shoutoutMarkers.add(mMap.addMarker(mOptions));
                    }
                }
            } catch (Exception e) {
                //Handling exceptions is for wusses
            }
            mainActivity.showShoutoutMarkers();
        }
    }
}
