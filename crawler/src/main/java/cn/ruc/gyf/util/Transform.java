package cn.ruc.gyf.util;

public class Transform {
	 public static String htmltrans(String content) {
		 if(content==null) return "";        
		     String html = content;

		     html = html.replaceAll( "&amp;", "&");
		     html = html.replace( "&quot;", "\"");  //"
		     html = html.replace( "&nbsp;", " ");// 替换空格
		     html = html.replace("&lt;", "<");
		     html = html.replaceAll( "&gt;", ">");
		   
		     return html;
		 }
	 public static void main(String[] args) {
		 String a="{&quot;author&quot;:{&quot;user_id&quot;:589635751,&quot;user_name&quot;:&quot;dearms\u4e36&quot;,&quot;name_u&quot;:&quot;dearms%E4%B8%B6&amp;ie=utf-8&quot;,&quot;user_sex&quot;:0,&quot;portrait&quot;:&quot;a720646561726d73e4b8b62523&quot;,&quot;is_like&quot;:1,&quot;level_id&quot;:12,&quot;level_name&quot;:&quot;\u7ff0\u6797\u7f16\u4fee&quot;,&quot;cur_score&quot;:6321,&quot;bawu&quot;:0,&quot;props&quot;:{&quot;1070001&quot;:{&quot;num&quot;:1,&quot;end_time&quot;:1437829446,&quot;notice&quot;:0}}},&quot;content&quot;:{&quot;post_id&quot;:94920300660,&quot;is_anonym&quot;:false,&quot;open_id&quot;:&quot;tbclient&quot;,&quot;open_type&quot;:&quot;android&quot;,&quot;date&quot;:&quot;2016-07-28 16:17&quot;,&quot;vote_crypt&quot;:&quot;&quot;,&quot;post_no&quot;:15,&quot;type&quot;:&quot;0&quot;,&quot;comment_num&quot;:3,&quot;ptype&quot;:&quot;0&quot;,&quot;is_saveface&quot;:false,&quot;props&quot;:null,&quot;post_index&quot;:9,&quot;pb_tpoint&quot;:null}}";
		 String re=htmltrans(a);
		 System.out.println(re);
	}
}
