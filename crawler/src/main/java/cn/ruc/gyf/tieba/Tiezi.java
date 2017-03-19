package cn.ruc.gyf.tieba;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSONObject;

import cn.ruc.gyf.db.Floor;
import cn.ruc.gyf.util.Const;

public class Tiezi {

	private static Date today;

	private static Document getHtmlFromUrl(String Url) {
		Document doc = null;

		try {
			doc = Jsoup.connect(Url).userAgent(Const.CrawlerUserAgent).timeout(3000).get();
		} catch (IOException e) {
			System.out.println("Cann't connect the url" + Url);
			e.printStackTrace();
		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return doc;
	}

	private static List<Floor> parseContentFromHtml(Document Html) {

		List<Floor> re=new ArrayList<Floor>();
		Date curdate = null;
		SimpleDateFormat tdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		// class=l_post 包含了用户的信息
		Elements elts = Html.getElementsByAttributeValueStarting("class", "l_post ");
		String time = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}";
		Pattern pt = Pattern.compile(time);

		for (Element elt : elts) {
			Matcher mt = pt.matcher(elt.toString());
			if (mt.find()) {
				try {
					curdate = tdf.parse(mt.group());
				} catch (ParseException e) {
					System.out.println("parse time err" + mt.group());
					continue;
				}
				if (curdate.compareTo(today) < 0) {
					continue;
				}
				Floor floor = new Floor();
				//得到每层楼的date
				floor.setDate(curdate);
				//得到每层楼的user_id和user_name
				JSONObject obj = JSONObject.parseObject(elt.attr("data-field"));
				for (String outer : obj.keySet()) {
					JSONObject innerobj = (JSONObject) obj.get(outer);
					if(innerobj.containsKey("user_id")){
						floor.setUser_id(Long.parseLong(innerobj.get("user_id").toString())); 
					}
					if(innerobj.containsKey("user_name")){
						floor.setUser_name((String)innerobj.get("user_name"));
					}
				}
				//TODO content中的图片需要解析
				//得到每层楼的content
				Elements contentelts=elt.getElementsByClass("d_post_content_main");
				for(Element contentelt : contentelts){
					Elements innercontent=contentelt.getElementsByAttributeValueStarting("id","post_content_");
					floor.setContent(innercontent.text());
				}
				re.add(floor);
				System.out.println(floor);
			}
		}
		
		return re;
	}

	// 获得帖子的最大页数，从最后一页开始爬
	private static int getMaxPageFromUrl(String Url) {
		Document doc = getHtmlFromUrl(Url);
		Elements elts = doc.getElementsByClass("l_reply_num");
		Element elt = elts.get(0);
		Elements red = elt.getElementsByClass("red");
		Element page = red.get(1);
		return Integer.parseInt(page.text());
	}

	// 爬取当前url并解析出每一层楼的信息插入数据库中
	private static void getContentFromUrl(String Url) {
		int maxpage = getMaxPageFromUrl(Url);
		for (int i = maxpage; i >= 1; i--) {
			String newUrl = Url + "?pn=" + i;
			Document doc = getHtmlFromUrl(newUrl);
			parseContentFromHtml(doc);
		}

	}

	public static void main(String[] args) throws Exception {

		Document doc = null;
		String folder = "404.txt";
		File html = new File(folder);
		try {
			doc = Jsoup.parse(html, Const.HtmlSaveEncode);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(doc.getElementsByTag("title").text());
		


		
		System.out.println("ok");

	}
}
