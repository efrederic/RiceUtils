package com.taken.riceutils;

import android.app.ListFragment;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HappeningNow extends Fragment {

    static String eventsNow = null;

    static final String KEY_TITLE = "title";
    static final String KEY_LOCATION = "location";
    static final String KEY_TIME = "time";
    static final String KEY_DESCRIPTION = "description";

    final ArrayList<HashMap<String, String>> events = new ArrayList<HashMap<String, String>>();

    public HappeningNow() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static HappeningNow newInstance() {
        HappeningNow fragment = new HappeningNow();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View myInflatedView = inflater.inflate(R.layout.fragment_happening_now, container, false);

        AsyncTask<String, Void, String> eventsAsync = new Events();
        eventsAsync.execute("http://services.rice.edu/events/dailyevents.cfm");
        try {
            eventsNow = eventsAsync.get();
        } catch (Exception ayy){
            //lmao
        }

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(eventsNow));
            Document doc = db.parse(is);
            NodeList nodes = doc.getElementsByTagName("item");


            for (int i = 0; i < nodes.getLength(); i++) {
                Node nNode = nodes.item(i);
                HashMap<String, String> map = new HashMap<String, String>();
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    map.put(KEY_TITLE, eElement.getElementsByTagName("title").item(0).getTextContent());
                    map.put(KEY_TIME, eElement.getElementsByTagName("pubDate").item(0).getTextContent());

                    Pattern locationPattern = Pattern.compile("(?<=<br><br><br>).*?(?=<br>)");
                    Matcher locationMatcher = locationPattern.matcher(eElement.getElementsByTagName("description").item(0).getTextContent());
                    if(locationMatcher.find()) {
                        map.put(KEY_LOCATION, locationMatcher.group());
                    }

                    String description = eElement.getElementsByTagName("description").item(0).getTextContent();
                    String[] descriptionParts = description.split("<br>");
                    map.put(KEY_DESCRIPTION, descriptionParts[descriptionParts.length-1]);

                    events.add(map);
                }
            }
        }catch (Exception e){
            //Handling exceptions is for wusses
        }

        ListView eventsList = (ListView) myInflatedView.findViewById(R.id.eventsList);
        HappeningNowAdapter adapter = new HappeningNowAdapter(getActivity(), events);
        eventsList.setAdapter(adapter);

        eventsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?>parent, View view, int position, long id) {
                Log.e("PRESS", "I WAS");
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(events.get(position).get(KEY_DESCRIPTION))
                        .setTitle(events.get(position).get(KEY_TITLE));
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        return myInflatedView;
    }

}
