package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Providers.Chapter.ChaptersList;
import com.example.myapplication.Providers.Chapter.MangaChapter;
import com.example.myapplication.Providers.Chapter.chapterLink;
import com.example.myapplication.Providers.MangaSummary;
import com.example.myapplication.Providers.Page.MangaInfo;
import com.example.myapplication.Providers.Page.MangaList;
import com.example.myapplication.Providers.ProviderDm5;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

public class Mange_chapter extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mange_chapter);
        String url = getIntent().getExtras().getString("aaaaaa");
        url=url.replace("/","");
        Log.e("/mXXXXXX/", url);
        //From https://stackoverflow.com/questions/2642777/trusting-all-certificates-using-httpclient-over-https/6378872#6378872
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }});
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager(){
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }}}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    context.getSocketFactory());
        } catch (Exception e) { // should never happen
            e.printStackTrace();
        }

        runAsyncTask(url);
    }

    private void runAsyncTask(String url){
        new AsyncTask<String,Integer, ArrayList<Bitmap>>(){
            @Override
            protected ArrayList<Bitmap> doInBackground(String... data){
                ProviderDm5 dm5=new ProviderDm5();
                //抓到bitmap 存成 arraylist
                ArrayList<Bitmap> bitmaps = new ArrayList<>();
                chapterLink chapterLink =new chapterLink();
                try{
                    for(int i =1;i<dm5.getChapterPage(data[0])+1;i++){
                        //chapterLink.add(dm5.getChapterImageUrl(data[0],String.valueOf(i)));
                        chapterLink = dm5.getChapterImageUrl(data[0],String.valueOf(i));
                        //chapterLink.Referer = dm5.getChapterImageUrl(data[0],String.valueOf(i)).Referer;
                        //chapterLink.imUrl = dm5.getChapterImageUrl(data[0],String.valueOf(i)).imUrl;

                        String imgUrl = chapterLink.imUrl;
                        URL url = new URL(imgUrl);
                        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.setRequestMethod("GET");
                        //connection.setRequestProperty("Host","manhua1025-104-250-150-12.cdndm5.com");
                        connection.setRequestProperty("Referer",chapterLink.Referer);
                        //connection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.1; rv:38.0) Gecko/20100101 Firefox/38.0");
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(input);
                        bitmaps.add(bitmap);
                    }
                    return bitmaps;
                }catch (Exception e){
                    return null;
                }
            }
            @Override
            protected void onProgressUpdate(Integer... values){
                super.onProgressUpdate();
            }
            @Override
            protected void  onPostExecute(final ArrayList<Bitmap> Bitmap){
                super.onPostExecute(Bitmap);
                //將ArrayList<bitmap>放入
                MyAdapter cubeeAdapter = new MyAdapter(Bitmap);
                GridView gridView = findViewById(R.id.gv_manga_chapter);
                gridView.setAdapter(cubeeAdapter);

            }

        }.execute(url);
    }

    private class MyAdapter extends BaseAdapter {
        private ArrayList<Bitmap> m;
        public MyAdapter(ArrayList<Bitmap> bitmaps){
            m=bitmaps;
        }
        @Override
        public  int getCount(){
            return  this.m.size();
        }
        @Override
        public Object getItem(int position){
            return m.get(position);
        }
        @Override
        public  long getItemId(int position){
            return 0;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            convertView = getLayoutInflater().inflate(R.layout.manga_chapter_item,parent,false);
            /*TextView name= convertView.findViewById(R.id.ImageLink);
            name.setText(m.get(position).url);*/
            final ImageView imageView =convertView.findViewById(R.id.imageView2);
                imageView.setImageBitmap(m.get(position));
            return convertView;
        }
    }
}
