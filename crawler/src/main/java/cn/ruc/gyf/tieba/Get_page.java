package cn.ruc.gyf.tieba;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import cn.ruc.gyf.util.Const;

public class Get_page {
	
	
	
	public static void main(String[] args) throws IOException {
		String outputfile="output.txt";
		FileWriter fw = new FileWriter(outputfile);
		String url="https://www.taobao.com/";
		Document doc = Jsoup.connect(url).userAgent(Const.CrawlerUserAgent).timeout(3000).get();
		System.out.println(doc.toString());
//		Elements els = null;
//		els = doc.getElementsByTag("a");
//		String name = "";
//		String url_name = "";
//		Iterator<org.jsoup.nodes.Element> it_el = els.iterator();
//		while(it_el.hasNext()){
//			org.jsoup.nodes.Element el = it_el.next();
//			name = el.text();
//			url_name = el.attr("href");
//			System.out.println(name+" "+url_name);
//		}	
//		fw.write(doc.toString());
//		fw.flush();
//		fw.close();
		System.out.println("ok");
	}
}
