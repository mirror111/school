package cn.ruc.gyf.tieba;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSONObject;
import com.mysql.jdbc.Statement;

import cn.ruc.gyf.db.Floor;
import cn.ruc.gyf.util.Const;
import cn.ruc.gyf.util.JdbcUtil;

public class Crawler {

	public int curid;
	public Date lastDate;
	public String lastTime;

	private List<String> tiebaroot;
	private List<Floor> allFloors;
	
	private Random random;
	// 初始化
	public Crawler() {
		// 初始化maxid
		curid = getMaxId() + 1;
		// 初始化tiebaroot
		getTiebaRoot();
		// 初始化allFloors
		allFloors = new ArrayList<Floor>();
		// 初始化当前日期
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat tdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String s = df.format(new Date()) + " 00:00";
		try {
			lastDate = tdf.parse(s);
		} catch (ParseException e) {
			System.out.println("get system time err");
			e.printStackTrace();
		}
		
		lastTime = "00:00";
		System.out.println("init lastDate:" + lastDate+" lastTime:"+lastTime);
		
		random=new Random();
	}

	private void getTiebaRoot() {
		tiebaroot = new ArrayList<String>();
		String filepath = Const.TiebaRootPath;
		String encoding = Const.FileSaveEncode;
		File file = new File(filepath);
		String line;
		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
			BufferedReader br = new BufferedReader(read);
			while ((line = br.readLine()) != null) {
				tiebaroot.add(line);
			}
			br.close();
			read.close();
		} catch (Exception e) {
			System.out.println("init tieba root err");
			System.exit(-1);
		}

		System.out.println("get tieba root success " + tiebaroot.size());
	}

	private int getMaxId() {
		JdbcUtil jdbcUtil = new JdbcUtil();
		Connection conn = jdbcUtil.getConnection();
		Statement st = null;
		ResultSet re = null;
		int maxid = 0;
		if (conn == null) {
			System.out.println("cann't connect DB");
			System.exit(-1);
		}

		try {
			String sql = "select max(id) from tieba";
			st = (Statement) conn.createStatement();
			re = st.executeQuery(sql);
			if (re.next()) {
				maxid = re.getInt(1);
			}
			st.close();
			jdbcUtil.releaseConn();
			System.out.println("get max id success " + maxid);
		} catch (SQLException e) {
			System.out.println("get max id err");
			System.exit(-1);
		}

		return maxid;
	}

	// 从Url中爬取Html页面
	private Document getHtmlFromUrl(String Url) {
		Document doc = null;

		try {
			doc = Jsoup.connect(Url).userAgent(Const.CrawlerUserAgent).timeout(3000).get();
			Thread.sleep(random.nextInt(1000));
		} catch (Exception e) {
			System.out.println("Cann't connect the url" + Url);
			e.printStackTrace();
		}

		String title = doc.getElementsByTag("title").text();
		if (title.equals("贴吧404")) {
			// some err
			doc = null;
			System.out.println("url 404：" + Url);
		}
		System.out.println("get the url:" + Url);

		return doc;
	}

	// 从贴吧主页的html源码中抽取出需要爬取的帖子的url
	private List<String> getUrlFromRoot(String Html) {
		// 匹配贴吧内的每一个帖子
		String head = "<a href=\"/p/.+</a>";
		// 最后回帖时间在当天 时间格式为 00:00 最后回帖时间不在当天 时间格式 00-00
		String timeregex = "\\d{2}:\\d{2}";
		List<String> re = new ArrayList<String>();

		Pattern p = Pattern.compile(head);
		Pattern ptime = Pattern.compile(timeregex);
		Matcher m = p.matcher(Html);

		while (m.find()) {
			int timebegin = Html.indexOf("title=\"最后回复时间\">", m.end());
			int nextdoc = Html.indexOf("<a href=\"/p/", m.end());
			if (timebegin > nextdoc) {
				continue;
			}
			int timeend = Html.indexOf("</span>", timebegin);
			Matcher mtime = ptime.matcher(Html.substring(timebegin, timeend));
			if (mtime.find()) {
				// <a href="/p/3368985818"
				if(mtime.group().compareTo(lastTime)<0){
					break;
				}
				String url = Const.TiebaBaseUrl + m.group().substring(9, 22);
				re.add(url);
			}
		}
		System.out.println("get urls from root ok  " + re.size());
		return re;
	}

	// 获得帖子的最大页数，从最后一页开始爬
	private int getMaxPageFromHtml(Document Html) {

		Elements elts = Html.getElementsByClass("l_reply_num");
		Element elt = elts.get(0);
		Elements red = elt.getElementsByClass("red");
		Element page = red.get(1);

		return Integer.parseInt(page.text());
	}

	// 从Html页面中爬取每一层的信息
	private List<Floor> parseContentFromHtml(Document Html) {

		List<Floor> re = new ArrayList<Floor>();
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
				if (curdate.compareTo(lastDate) < 0) {
					continue;
				}
				Floor floor = new Floor();
				// 得到每层楼的date
				floor.setDate(curdate);
				// 得到每层楼的user_id和user_name
				JSONObject obj = JSONObject.parseObject(elt.attr("data-field"));
				for (String outer : obj.keySet()) {
					JSONObject innerobj = (JSONObject) obj.get(outer);
					if (innerobj.containsKey("user_id")) {
						floor.setUser_id(Long.parseLong(innerobj.get("user_id").toString()));
					}
					if (innerobj.containsKey("user_name")) {
						floor.setUser_name((String) innerobj.get("user_name"));
					}
				}
				// TODO content中的图片需要解析
				// 得到每层楼的content
				Elements contentelts = elt.getElementsByClass("d_post_content_main");
				for (Element contentelt : contentelts) {
					Elements innercontent = contentelt.getElementsByAttributeValueStarting("id", "post_content_");
					floor.setContent(innercontent.text());
				}
				re.add(floor);
			}
		}
		return re;
	}

	// 爬取当前url并解析出每一层楼的信息插入数据库中
	private void getContentFromUrl(String Url, String tiebaname) {

		Document doc = getHtmlFromUrl(Url);
		if (doc == null) {
			return;
		}
		int maxpage = getMaxPageFromHtml(doc);
		String theme = doc.getElementsByClass("core_title_txt").text();
		long theme_id = Long.parseLong(Url.substring(25));
		List<Floor> floors = null;
		for (int i = maxpage; i >= 1; i--) {
			String newUrl = Url + "?pn=" + i;
			Document newdoc = getHtmlFromUrl(newUrl);
			if (newdoc == null) {
				continue;
			}
			floors = parseContentFromHtml(newdoc);
			if (floors.size() == 0) {
				break;
			}
			for (Floor floor : floors) {
				floor.setId(curid++);
				floor.setTheme(theme);
				floor.setTheme_id(theme_id);
				floor.setBa_name(tiebaname);
				allFloors.add(floor);
			}
		}

		System.out.println("get content ok from url " + Url);

	}

	private void updateDB() {
		JdbcUtil jdbcUtil = new JdbcUtil();
		Connection conn = jdbcUtil.getConnection();
		PreparedStatement ps = null;
		if (conn == null) {
			System.out.println("cann't connect DB");
			System.exit(-1);
		}
		try {

			// id,user_id,user_name,date,content,theme,theme_id.ba_name
			String sql = "insert into tieba values(?,?,?,?,?,?,?,?)";

			conn.setAutoCommit(false);
			ps = conn.prepareStatement(sql);

			for (Floor floor : allFloors) {
				ps.setInt(1, floor.getId());
				ps.setLong(2, floor.getUser_id());
				ps.setString(3, floor.getUser_name());
				ps.setTimestamp(4, new java.sql.Timestamp(floor.getDate().getTime()));
				ps.setString(5, floor.getContent());
				ps.setString(6, floor.getTheme());
				ps.setLong(7, floor.getTheme_id());
				ps.setString(8, floor.getBa_name());
				ps.addBatch();
			}
			ps.executeBatch();
			conn.commit();
			System.out.println("update db ok " + allFloors.size());
			allFloors.clear();
			ps.close();
			jdbcUtil.releaseConn();

		} catch (SQLException e) {
			System.out.println("update db err");
			e.printStackTrace();
		}

	}

	private void beginTask() {
		System.out.println("lastDate info:" + lastDate + " lastTime info:" + lastTime);
		for (String tiebaname : tiebaroot) {
			System.out.println("cur tieba:" + tiebaname);
			String root = "http://tieba.baidu.com/f?ie=utf-8&kw=" + tiebaname + "&fr=search";
			Document doc = getHtmlFromUrl(root);
			if (doc == null) {
				continue;
			}
			List<String> urls = getUrlFromRoot(doc.toString());
			for (String url : urls) {
				getContentFromUrl(url, tiebaname);
				if (allFloors.size() > 100) {
					updateDB();
				}
			}
			updateDB();
		}
	}

	public static void main(String[] args) {
		Crawler crawler = new Crawler();
		while (true) {
			System.out.println("begin the task " + new Date());
			System.out.println("cur id:"+crawler.curid);
			crawler.beginTask();
			System.out.println("end of the task " + new Date());
			SimpleDateFormat df = new SimpleDateFormat("HH:mm");
			crawler.lastDate = new Date();
			crawler.lastTime = df.format(crawler.lastDate);
			System.out.println("sleep 10 minutes");
			try {
				Thread.sleep(Const.ThreadSleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
