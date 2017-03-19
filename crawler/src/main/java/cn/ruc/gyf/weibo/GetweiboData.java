package cn.ruc.gyf.weibo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.mysql.jdbc.Statement;

import cn.ruc.gyf.db.WeiboInfo;
import cn.ruc.gyf.util.JdbcUtil;

public class GetweiboData {
	private String prefix = "http://m.weibo.cn/container/getIndex?containerid=230413";
	private String suffix = "_-_WEIBO_SECOND_PROFILE_MORE_WEIBO&page=";
	private String userIdPath = "allUser";
	private String cookie = "";
	public String lastday = "";
	private String year = "2017-";
	int id = 0;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private ArrayList<String> userlist = new ArrayList<String>();
	private ArrayList<WeiboInfo> weibos = new ArrayList<WeiboInfo>();
	private Random random = new Random();
	private SimpleDateFormat warndf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private HashMap<String, String> tpmap = new HashMap<String, String>();
	private ArrayList<WeiboInfo> tplist = new ArrayList<WeiboInfo>();
	private int tpcount;

	public GetweiboData() {
		id = getMaxId() + 1;
		System.out.println("init max id:" + id);
		tpcount = 0;
		tpmap.put("1752825395", "光明网");
		tpmap.put("3183107112", "检察日报");
		tpmap.put("2127460165", "中国日报网");
		tpmap.put("2803301701", "人民日报");
		tpmap.put("1880922220", "金华晚报");
		tpmap.put("6023189492", "每日人物");
		tpmap.put("1314608344", "新闻晨报");
		tpmap.put("1644948230", "法制晚报");
		tpmap.put("1880087643", "北京新闻广播");
		tpmap.put("1853923717", "罗辑思维");
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
			String sql = "select max(id) from Weibo";
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

	// public void getCookie() throws Exception {
	// String filepath = "C:\\Users\\GongYifan\\Desktop\\weibo\\cookie.txt";
	// String encoding = "utf8";
	// File file = new File(filepath);
	// InputStreamReader read = new InputStreamReader(new FileInputStream(file),
	// encoding);
	// BufferedReader br = new BufferedReader(read);
	// String line;
	// while ((line = br.readLine()) != null) {
	// cookie = line;
	// }
	// br.close();
	// read.close();
	// System.out.println("init cookie success!");
	// }

	public void getUserId() throws Exception {
		String filepath = userIdPath;
		String encoding = "utf8";
		File file = new File(filepath);
		InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
		BufferedReader br = new BufferedReader(read);
		String line;
		while ((line = br.readLine()) != null) {
			String[] re = line.split("\t");
			userlist.add(re[0]);
		}
		br.close();
		read.close();
		System.out.println("init user id success!");
	}

	public String getReturnData(String urlString) throws UnsupportedEncodingException, FileNotFoundException {

		String res = "";
		try {
			URL url = new URL(urlString);
			java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Cookie", cookie);
			conn.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
			conn.setReadTimeout(5000);
			java.io.BufferedReader in = new java.io.BufferedReader(
					new java.io.InputStreamReader(conn.getInputStream(), "UTF-8"));
			String line;
			while ((line = in.readLine()) != null) {
				res += line;
			}
			in.close();
			Thread.sleep(random.nextInt(5000));
		} catch (Exception e) {
			// 抓不到页面，休息15min
			System.out.println("Error in getReturnData(),and e is " + e.getMessage());
			System.out.println("Thread sleep 10 min  " + warndf.format(new Date()));
			try {
				Thread.sleep(10 * 60 * 1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		return res;
	}

	public int parserJson(String uid, String jsonStr) throws IOException {
		int re = 0;
		JSONObject json = JSONObject.parseObject(jsonStr);
		JSONArray dataArr = json.getJSONArray("cards");

		// 去掉微博中的 <链接，图片url等>
		String regex = "<[^<]*>";
		Pattern p = Pattern.compile(regex);
		Matcher m;
		String temptext;
		for (int i = 1; i < dataArr.size(); i++) {
			WeiboInfo wb = new WeiboInfo();
			JSONObject dataObj = dataArr.getJSONObject(i);
			if (dataObj.getJSONObject("mblog") == null) {
				continue;
			}
			JSONObject mblog = dataObj.getJSONObject("mblog");
			// find time
			String time = mblog.getString("created_at");
			if (time.indexOf("今天") >= 0) {
				re = 1;
				continue;
			}
			if (time.indexOf(lastday) < 0) {
				continue;
			}
			// 有用
			re = 1;
			time = year + time;
			try {
				Date date = sdf.parse(time);
				int wbtime = (int) (date.getTime() / 1000);
				wb.setTime(wbtime);
			} catch (ParseException e1) {
				wb.setTime(0);
			}

			wb.setId(id++);
			wb.setUid(uid);
			wb.setWbid(mblog.getString("id"));
			wb.setOritime(time);
			wb.setReposts_count(mblog.getInteger("reposts_count"));
			wb.setComments_count(mblog.getInteger("comments_count"));
			wb.setAttitudes_count(mblog.getInteger("attitudes_count"));

			temptext = mblog.getString("text");
			m = p.matcher(temptext);
			temptext = m.replaceAll("");
			wb.setContent(temptext);

			JSONObject retweeted_status = mblog.getJSONObject("retweeted_status");
			if (retweeted_status != null) {
				try {
					JSONObject retweeted_status_user = retweeted_status.getJSONObject("user");
					wb.setRepostUid(retweeted_status_user.getString("id"));
					wb.setRepostWbid(retweeted_status.getString("id"));

					temptext = retweeted_status.getString("text");
					m = p.matcher(temptext);
					temptext = m.replaceAll("");
					wb.setRepostContent(temptext);

					String repostTime = retweeted_status.getString("created_at");
					// 当年的时间格式为MM-dd HH:mm 非当年时间格式为yyyy-MM-dd HH:mm
					if (repostTime.length() == 11) {
						repostTime = year + repostTime;
					}
					wb.setOrirepostTime(repostTime);
					try {
						Date date = sdf.parse(repostTime);
						int repostWbTime = (int) (date.getTime() / 1000);
						wb.setRepostTime(repostWbTime);
					} catch (Exception e) {
						wb.setRepostTime(0);
					}
				} catch (NullPointerException e) {

				}
			}
			if (tpmap.containsKey(uid)) {
				tplist.add(wb);
			}
			weibos.add(wb);
		}
		return re;
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
			String sql = "insert into Weibo values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

			conn.setAutoCommit(false);
			ps = conn.prepareStatement(sql);

			for (WeiboInfo weibo : weibos) {
				ps.setInt(1, weibo.getId());
				ps.setString(2, weibo.getUid());
				ps.setString(3, weibo.getWbid());
				ps.setInt(4, weibo.getTime());
				ps.setString(5, weibo.getOritime());
				ps.setString(6, weibo.getContent());
				ps.setString(7, weibo.getRepostUid());
				ps.setString(8, weibo.getRepostWbid());
				ps.setInt(9, weibo.getRepostTime());
				ps.setString(10, weibo.getOrirepostTime());
				ps.setString(11, weibo.getRepostContent());
				ps.setInt(12, weibo.getReposts_count());
				ps.setInt(13, weibo.getComments_count());
				ps.setInt(14, weibo.getAttitudes_count());
				ps.addBatch();
			}
			ps.executeBatch();
			conn.commit();
			System.out.println("update db ok " + weibos.size());
			weibos.clear();
			ps.close();
			jdbcUtil.releaseConn();

		} catch (SQLException e) {
			System.out.println("update db err");
			e.printStackTrace();
			try {
				conn.rollback();
				System.out.println("rollback success");
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			jdbcUtil.releaseConn();
			weibos.clear();
		}

	}

	public String getCookie(String userName, String passWord)
			throws FailingHttpStatusCodeException, MalformedURLException, IOException, InterruptedException {
		// 新浪微博登录页面
		String baseUrl = "https://passport.weibo.cn/signin/login?entry=mweibo&res=wel&wm=3349&r=http%3A%2F%2Fm.weibo.cn%2F";
		// 打开
		WebClient webClient = new WebClient(BrowserVersion.CHROME);
		webClient.addRequestHeader("User-Agent",
				"Mozilla/5.0 (iPad; CPU OS 7_0_2 like Mac OS X) AppleWebKit/537.51.1 (KHTML, like Gecko) Version/7.0 Mobile/11A501 Safari/9537.53");
		HtmlPage page = webClient.getPage(baseUrl);
		// 等待页面加载
		Thread.sleep(1000);
		// 获取输入帐号的控件
		HtmlInput usr = (HtmlInput) page.getElementById("loginName");
		usr.setValueAttribute(userName);
		// 获取输入密码的控件
		HtmlInput pwd = (HtmlInput) page.getElementById("loginPassword");
		pwd.setValueAttribute(passWord);
		// 点击登录
		DomElement button = page.getElementById("loginAction");
		page = (HtmlPage) button.click();
		// 等待页面加载
		Thread.sleep(1000);
		// 获取到“写微博”这个按钮，因为这个按钮没有name和id,获取所有<a>标签
		DomNodeList<DomElement> button2 = page.getElementsByTagName("a");
		// 跳转到发送微博页面
		page = (HtmlPage) button2.get(4).click();
		// 等待页面加载
		Thread.sleep(1000);
		// 得到cookie
		Set<Cookie> cookieSet = page.getWebClient().getCookies(page.getUrl());
		String cookie = cookieSet.toString();
		return cookie.substring(1, cookie.length() - 1);
	}

	public String getLastday() {
		SimpleDateFormat df = new SimpleDateFormat("MM-dd");
		Date date = new Date(new Date().getTime() - 24 * 60 * 60 * 1000);
		return df.format(date);
	}

	public void task() throws Exception {
		// 读取cookie,存到cookie中
		cookie = getCookie("userName", "password");
		// 读取userid,存到userlist中
		getUserId();
		// 失败次数
		int err = 0;
		for (String user : userlist) {
			int pagenum = 1;
			System.out.println("cur user id : " + user);
			String preUrl = prefix + user + suffix;
			int flag = 1;
			while (flag == 1) {
				String curUrl = preUrl + pagenum;
				pagenum++;
				// 随机sleep线程1s之内的时间
				String jsonDoc = getReturnData(curUrl);
				if (jsonDoc.length() < 1000 || jsonDoc.contains("\"msg\":\"\u6ca1\u6709\u5185\u5bb9\"}")
						|| jsonDoc.contains("\"msg\":\"没有内容\"")) {
					System.out.println("Error: cann't get the page , result = " + jsonDoc);
					err++;
					if (err > 1000) {
						System.out.println("max err:" + err);
						updateDB();
						System.exit(0);
					}
					break;
				}
				flag = parserJson(user, jsonDoc);
				if (weibos.size() > 100) {
					updateDB();
				}
			}
			if (tpmap.containsKey(user)) {
				tpcount++;
				if (tpcount == 10) {
					tpwrite();
					System.out.println("write tp file success");
				}
			}
		}
		updateDB();
	}

	public void tpwrite() {
		String outputfile = "tpnews";
		FileWriter fw;
		try {
			fw = new FileWriter(outputfile);
			String s = "";
			for (WeiboInfo w : tplist) {
				s = w.getId() + "\t" + w.getOritime() + "\t" + w.getContent() + "\t"
						+ (w.getReposts_count() + w.getComments_count() + w.getAttitudes_count()) + "\t"
						+ tpmap.get(w.getUid());
				fw.write(s + "\n");
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tplist.clear();
		tpcount = 0;
	}

	public static void main(String[] args) throws Exception {
		GetweiboData crawler = new GetweiboData();
		String lastday = crawler.getLastday();
		while (true) {
			if (!crawler.lastday.equals(lastday)) {
				crawler.lastday = lastday;
				System.out.println("init lastday succ:" + crawler.lastday);
				crawler.task();
			} else {
				System.out.println("Thread sleep 1 hours");
				Thread.sleep(1000 * 60 * 60);
			}
			lastday = crawler.getLastday();
		}
	}
}
