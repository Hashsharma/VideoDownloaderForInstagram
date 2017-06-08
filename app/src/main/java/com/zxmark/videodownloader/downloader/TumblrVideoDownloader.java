package com.zxmark.videodownloader.downloader;

import android.util.Log;

import com.zxmark.videodownloader.spider.HttpRequestSpider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fanlitao on 17/6/8.
 */

public class TumblrVideoDownloader extends BaseDownloader {

    public static final String COOKIE = "tmgioct=58e973e1b0a6c10525214720; rxx=1lmvgrra48x.ohltstg&v=1; anon_id=GHLHRDONAIAMSOORBVJLJRTBUZMGSLSB; language=%2Czh_CN; pfp=XjQRFMRlG5DM5WTCG9pHFzc0DyQ7gnCYPshmMq2c; pfs=wG3OJtgdJFVUTCAXtXODFCZFg; pfe=1504665856; pfu=265348919; logged_in=1; nts=false; _ga=GA1.2.1711342421.1491694563; _gid=GA1.2.580200024.1496889785; __utma=189990958.1711342421.1491694563.1495239211.1496889785.12; __utmb=189990958.0.10.1496889785; __utmc=189990958; __utmz=189990958.1495239211.11.2.utmcsr=fanqianglu.tumblr.com|utmccn=(referral)|utmcmd=referral|utmcct=/; devicePixelRatio=2; documentWidth=1213; yx=qz2cu5yprvfi3%26o%3D4%26q%3DIStxrbEcLRWlmVpOZd83A2FHpgD-%26f%3Drg%26v%3DXeP52hzzxNSqlCz4bHgB; capture=b4zU9NL1ku36y3KjnVwRuG3Xs";

    @Override
    public String startRequest(String htmlUrl) {
        return HttpRequestSpider.getInstance().request(htmlUrl);
    }

    @Override
    public String getVideoUrl(String content) {
        String regex;
        String videoUrl = null;
        regex = "\"video_url\":\"(.*?)\",";
        Pattern pa = Pattern.compile(regex, Pattern.DOTALL);
        Matcher ma = pa.matcher(content);
        Log.v("fan2", "ma=" + ma);

        if (ma.find()) {
            Log.v("fan2", "" + ma.group());
            videoUrl = ma.group(1);
        }
        Log.v("fan2", "" + videoUrl);
        return videoUrl;
    }

    @Override
    public String getDownloadFileUrl(String htmlUrl) {

        String content = startRequest(htmlUrl);

        return getVideoUrl(content);


    }
}
