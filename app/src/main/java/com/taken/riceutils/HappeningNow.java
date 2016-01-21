package com.taken.riceutils;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HappeningNow extends Fragment {

    private static final String KEY_TITLE = "title";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_TIME = "time";
    private static final String KEY_DESCRIPTION = "description";
    private static int numDays = 0;

    final ArrayList<HashMap<String, String>> events = new ArrayList<HashMap<String, String>>();

    public HappeningNow() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static HappeningNow newInstance(int days) {
        numDays = days - 1; //0 = today, 1 = today+tomorrow, etc.
        HappeningNow fragment = new HappeningNow();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View myInflatedView = inflater.inflate(R.layout.fragment_happening_now, container, false);

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... url){
                try{
                    URL eventsURL = new URL(url[0]);
                    HttpURLConnection connection = (HttpURLConnection) eventsURL.openConnection();
                    connection.setRequestMethod("POST");
                    InputStream inputStream = new BufferedInputStream(connection.getInputStream());

                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder sBuilder = new StringBuilder();

                    String line;
                    while ((line = br.readLine()) != null) {
                        sBuilder.append(line).append("\n");
                    }

                    inputStream.close();

                    String eventsNow = sBuilder.toString();

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
                            String title = eElement.getElementsByTagName("title").item(0).getTextContent();
                            title = title.replaceAll("<br.*?>","\n");
                            title = title.replaceAll("<.*?>","");
                            map.put(KEY_TITLE, title);

                            Pattern timePattern = Pattern.compile("(?<=20\\d\\d<br>).*?(?=<br><br><br>)");
                            Matcher timeMatcher = timePattern.matcher(eElement.getElementsByTagName("description").item(0).getTextContent());
                            if(timeMatcher.find()) {
                                map.put(KEY_TIME, timeMatcher.group());
                            }

                            Pattern locationPattern = Pattern.compile("(?<=<br><br><br>).*?(?=<br>)");
                            Matcher locationMatcher = locationPattern.matcher(eElement.getElementsByTagName("description").item(0).getTextContent());
                            if(locationMatcher.find()) {
                                map.put(KEY_LOCATION, locationMatcher.group());
                            }

                            String description = eElement.getElementsByTagName("description").item(0).getTextContent();
                            description = description.replaceAll("<br.*?>","\n");
                            description = description.replaceAll("<.*?>","");
                            map.put(KEY_DESCRIPTION, description);

                            events.add(map);
                        }
                    }

                }catch(Exception e){
                    //Log.e("e",e.toString());
                }
                return null;
            }

            @Override
            protected void onPostExecute(String xmldata) {
                ListView eventsList = (ListView) myInflatedView.findViewById(R.id.eventsList);
                HappeningNowAdapter adapter = new HappeningNowAdapter(getActivity(), events);
                eventsList.setAdapter(adapter);

                eventsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage(events.get(position).get(KEY_DESCRIPTION))
                                .setTitle(events.get(position).get(KEY_TITLE));
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });
            }
        }.execute("http://services.rice.edu/events/dailyevents.cfm?days=" + numDays);

        return myInflatedView;
    }
}
