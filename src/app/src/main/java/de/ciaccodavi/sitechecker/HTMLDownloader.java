package de.ciaccodavi.sitechecker;

import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;


public class HTMLDownloader {

   static String line = "";
   static String content = "";

    public static List<String> savedUrls;
    public static List<String> savedWebsitesData;

    public HTMLDownloader() {
        savedUrls = new ArrayList<>();
        savedWebsitesData = new ArrayList<>();
    }

    public void download(String urlString) {
        new DownloadOperation().execute(urlString);
    }

    public static void check(int id) {
        new CheckOperation().execute(id);
    }

    private class DownloadOperation extends AsyncTask<String, Void, String> {

        String urlString;
        Boolean success;

        @Override
        protected String doInBackground(String... params) {
            /*for (int i = 0; i < 5; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }*/
            success = true;
            URL url=null;
            try {
                urlString = params[0];
                url = new URL(urlString);
                Log.d("azz", "input string: " + params[0]);
                /*HttpResponseCache cache = HttpResponseCache.getInstalled();
                if (cache != null) {
                    Log.d("cacheUrl"," > "+cache.toString());
                    cache.flush();
                }*/

                // Read all the text returned by the server
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                line = "";
                content = "";
                while ((line = in.readLine()) != null) {
                    content += line;
                }
                in.close();
            } catch (MalformedURLException e) {
                Log.d("asd", "malformed URL!!! " + e.toString());
                content = e.toString();
                success = false;
            } catch (IOException e) {
                Log.d("asd", "io exception!!! " + e.toString());
                content = e.toString();
                success = false;
            }
            if(success){
                savedUrls.add(url.toString());
                // MainActivity.addMenuVoice(MainActivity.num_urls, urlString);
                // MainActivity.hasToReloadMenu.setBoo(true);
                MainActivity.num_urls++;
                return SHASH(content);
            }
            return content;
        }

        @Override
        protected void onPostExecute(String result) {
            //TextView txt = (TextView) findViewById(R.id.output);
            //txt.setText("Executed"); // txt.setText(result);
            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
            MainActivity.loadingBar.setVisibility(View.INVISIBLE);
            if (success) {
                MainActivity.outputText.setText(urlString + "\n\n" + result + "\n\nNew website data downloaded!");
                Snackbar.make(MainActivity.mainView, urlString + " added!", Snackbar.LENGTH_LONG).setAction("Action", null).show();

                savedWebsitesData.add(result);
                saveWebsites();
            }else{
                MainActivity.outputText.setText("ERROR!\n\n" + result + "\n\nfor: "+urlString);
            }
        }

        @Override
        protected void onPreExecute() {
            MainActivity.loadingBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }


    private static class CheckOperation extends AsyncTask<Integer, Void, String> {

        String output = "";
        int id;

        @Override
        protected String doInBackground(Integer... params) {
            Log.d("CHECKING", " id " + params[0]);
            id = params[0];
            try {
                URL url = new URL(savedUrls.toArray()[id].toString());
                // Read all the text returned by the server
                /*HttpResponseCache cache = HttpResponseCache.getInstalled();
                if (cache != null) {
                    Log.d("cacheUrlcheck"," > "+cache.toString());
                    cache.flush();
                }*/

                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                line = "";
                content = "";
                while ((line = in.readLine()) != null) {
                    content += line;
                }
                in.close();
            } catch (MalformedURLException e) {
                Log.d("asd", "malformed URL!!! " + e.toString());
                content = e.toString();
            } catch (IOException e) {
                Log.d("asd", "io exception!!! " + e.toString());
                content = e.toString();
            }
            Log.d("CHECKING", " content " + SHASH(content));
            return SHASH(content);
        }

        @Override
        protected void onPostExecute(String result) {

            output += "Checking: " + savedUrls.toArray()[id] + "\n\n." + savedWebsitesData.toArray()[id] + "\n\n:" + result;

            MainActivity.loadingBar.setVisibility(View.INVISIBLE);
            if (!result.equals(savedWebsitesData.toArray()[id].toString())) {
                savedWebsitesData.set(id, result);
                saveWebsites();
                output += "\n\nCHANGED!";
                MainActivity.siteHasChangedForNotification=true;
                MainActivity.idThatHasChanged=id;
            } else {
                output += "\n\nnot changed...";
            }

            MainActivity.outputText.setText(output);

        }

        @Override
        protected void onPreExecute() {
            MainActivity.loadingBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }


    public String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            Log.e("exception", e.toString());
        }
        return null;
    }

    public static String SHASH(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            md.update(input.getBytes("UTF-8")); // Change this to "UTF-16" if needed
            byte[] digest = md.digest();

            return String.format("%064x", new java.math.BigInteger(1, digest));
        } catch (java.security.NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            Log.e("exception", e.toString());
            return e.toString();
        }
    }


    public static void saveWebsites() {
        MainActivity.sharedPreferencesEditor = MainActivity.sharedPreferences.edit();
        StringBuilder csvList = new StringBuilder();
        for (String s : savedUrls) {
            csvList.append(s);
            csvList.append(",");
        }
        MainActivity.sharedPreferencesEditor.putString("urls", csvList.toString());
        MainActivity.sharedPreferencesEditor.apply();

        MainActivity.sharedPreferencesEditor = MainActivity.sharedPreferencesData.edit();
        csvList = new StringBuilder();
        for (String s : savedWebsitesData) {
            csvList.append(s);
            csvList.append(",");
        }
        MainActivity.sharedPreferencesEditor.putString("data", csvList.toString());
        MainActivity.sharedPreferencesEditor.apply();
    }

    public void loadSavedWebsites() {
        String csvList = MainActivity.sharedPreferences.getString("urls", null);
        String[] items;
        if (csvList != null) {
            items = csvList.split(",");
            savedUrls = new ArrayList<>();
            for (String s : items) {
                savedUrls.add(s);
                Log.d("LOADING", "urls: " + s);
            }
        }
        csvList = MainActivity.sharedPreferencesData.getString("data", null);
        if (csvList != null) {
            items = csvList.split(",");
            savedWebsitesData = new ArrayList<>();
            for (String s : items) {
                savedWebsitesData.add(s);
                Log.d("LOADING", "data: " + s);
            }
        }
    }
}
