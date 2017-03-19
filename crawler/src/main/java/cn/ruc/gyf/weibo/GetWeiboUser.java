package cn.ruc.gyf.weibo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import cn.ruc.gyf.util.Const;
/*
 * 
 * 获得新浪微博某个页面下的用户名+id
 * 
 */
public class GetWeiboUser {
	static String cookie="";
	static Random random;
	
	static {
		try {
			getCookie();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		random=new Random();
	}
	
	public static void getCookie() throws Exception{
		String filepath = "C:\\Users\\GongYifan\\Desktop\\cookie.txt";
		String encoding = "utf8";
		File file = new File(filepath);
		InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
		BufferedReader br = new BufferedReader(read);
		String line;
		while((line=br.readLine())!=null){
			cookie=line;
		}
		br.close();
		read.close();
		System.out.println("init cookie success!");
	}
	
	public static void getUser(int page) throws IOException, InterruptedException{
		HashMap<String,String> userMap = new HashMap<String,String>();
		for(int i=1;i<=page;i++){
			System.out.println("cur page :"+i);
			int flag=0;
			
			String weiboUrl="http://d.weibo.com/1087030002_2975_7005_0?page="+i+"#Pl_Core_F4RightUserList__4";
			Document doc = Jsoup.connect(weiboUrl).
							header("Host", "weibo.com").
							header("Cookie",cookie).
							userAgent(Const.CrawlerUserAgent)
							.timeout(10000).get();
			
			Thread.sleep(1000);
			
			Document document = Jsoup.parse(doc.toString());
			Elements nodes = document.getElementsByTag("script");
			
			String str = nodes.get(nodes.size() - 1).toString();
			String user = "usercard=.*?strong>";
			Pattern p = Pattern.compile(user);
			Matcher m = p.matcher(str);
			String findid="id=([^&]+)";
			String findtitle="title=(.*)\">";
			
			Pattern pid = Pattern.compile(findid);
			Pattern ptitle = Pattern.compile(findtitle);
			String id="";
			String title="";
			while(m.find()){
				String temp=m.group();
				Matcher mid = pid.matcher(temp);
				if(mid.find()){
					id=mid.group(1);
				}
				Matcher mtitle = ptitle.matcher(temp);
				if(mtitle.find()){
					title=mtitle.group(1);
					title=title.replace("\\", "");
					title=title.replace("\"", "");
				}
				if(id!=""){
					flag++;
					userMap.put(id, title);
				}
			}
			if(flag==0){
				i--;
			}
		}
		
		String outputfile="C:\\Users\\GongYifan\\Desktop\\shiping.txt";
		FileWriter fw = new FileWriter(outputfile);
		for(String id : userMap.keySet()){
			fw.write(id+"\t"+userMap.get(id)+"\n");
		}
		fw.flush();
		fw.close();
	}
	
	public static void main(String[] args) throws Exception{
		getUser(88);
	}
}
