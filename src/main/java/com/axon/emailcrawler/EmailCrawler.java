package com.axon.emailcrawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@SuppressWarnings("deprecation")
public class EmailCrawler {
	private static Log logger = LogFactory.getLog(EmailCrawler.class);
	private final static int DEFAULT_VALUE = 2<<29;
	private BitSet bs = new BitSet(DEFAULT_VALUE);
	private int[] seeds = { 3, 11, 19, 29, 37, 43, 61, 83 };
	private BloomFiler[] bfFuncs = new BloomFiler[seeds.length];
	private HashSet<String> gongSiSet = null;
	private HashSet<String> jobsSet = null;

	public EmailCrawler() {
		init();
		
	}

	// 已经入库的biz号把他们映射到bitset中
	public void init() {
		logger.info("开始获取bloomfilter的位数据");
		setBitset();
		BufferedReader bufr = null;
		FileReader fr = null;
		try {
			fr = new FileReader(new File("2.txt"));
			bufr = new BufferedReader(fr);
			String line = null;
			while ((line = bufr.readLine()) != null) {
				add(line);
			}
		} catch (Exception e) {
			logger.error("出错", e);
		} finally {
			try {
				bufr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			fr = new FileReader(new File("3.txt"));
			bufr = new BufferedReader(fr);
			String line = null;
			while ((line = bufr.readLine()) != null) {
				add(line);
			}
		} catch (Exception e) {
			logger.error("出错", e);
		} finally {
			try {
				bufr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	
	public static void main(String[] args) {
		EmailCrawler emailCrawler = new EmailCrawler();
				emailCrawler.accessUrl();
			
		
	}

	/**
	 * 通过url，访问页面，并把该页面以doc形式返回
	 * 
	 * @param li
	 */
	@SuppressWarnings("unused")
	public void accessUrl() {
		Document doc = null;
		Stack<String> stack1 = null;
		Stack<String> stack2 = null;
		logger.info("开始获取页面信息");
		int count = 0;
		try {
			stack1 = getUrl1("http://www.lagou.com/?utm_source=m_cf_cpt_baidu_pc");
			while (!stack1.isEmpty()) {
				gongSiSet = new HashSet<String>();
				jobsSet = new HashSet<String>();
				String url = stack1.pop();
				if (url.equals("www.lagou.com/zhaopin/"))
					continue;
				System.out.println(url);
				if(isExist(url)) continue;
				getUrl2("http://" + url);
				parseGongSiUrl(gongSiSet);
				writeToFileRoot(url);
			
			}
		} catch (Exception e) {
			logger.error("fangwen", e);
		}
		logger.info("结束获取页面信息，开始解析页面信息");
		// parserHtml(set);
	}

	private void writeToFileRoot(String url) {
		BufferedWriter bufw = null;
		FileWriter fw = null;
		File file = new File("3.txt");
		try {
			fw = new FileWriter(file, true);
			bufw = new BufferedWriter(fw);
			bufw.write(url);
			bufw.newLine();
			bufw.flush();

		} catch (Exception e) {
			logger.info("写入文件出错", e);
		} finally {
			try {
				bufw.close();
			} catch (IOException e) {
				logger.error("关闭出错", e);
			}
		}

		
	}

	// 访问拉钩上公司的主页面
	private void parseGongSiUrl(Set<String> gongSiSet) {
		for (String url : gongSiSet) {
			if (isExist(url)) {
				continue;
			}
			System.out.println("开始爬具体的公司");
			writeFanToFile(url);
			String htmlString = getHtmlByUrl(url);
			if (null == htmlString)
				return;
			Document doc = Jsoup.parse(htmlString);
			List<Element> elementList = doc.getElementsByClass("hovertips");
			String email = null;
			for (Element element : elementList) {
				String gongsiURl = element.attr("href");
				String gongSiName = element.attr("title");
				if (gongsiURl.contains("pincai360")||gongsiURl.contains("maoyan")
						|| !gongsiURl.contains("www"))
					continue;
				System.out.println(gongsiURl + "---------------------"
						+ gongSiName);
				email = parseSpecificGongSiUrl(gongsiURl);
				if (null != email) {
					System.out.println(email);
					writeToFile(gongSiName, email);
					break;
				}
			}
		}

	}

	private void writeFanToFile(String url) {
		BufferedWriter bufw = null;
		FileWriter fw = null;
		File file = new File("2.txt");
		try {
			fw = new FileWriter(file, true);
			bufw = new BufferedWriter(fw);
			bufw.write(url);
			bufw.newLine();
			bufw.flush();

		} catch (Exception e) {
			logger.info("写入文件出错", e);
		} finally {
			try {
				bufw.close();
			} catch (IOException e) {
				logger.error("关闭出错", e);
			}
		}

	}

	private void writeToFile(String gongSiName, String email) {
		BufferedWriter bufw = null;
		FileWriter fw = null;
		File file = new File("1.txt");
		try {
			fw = new FileWriter(file, true);
			bufw = new BufferedWriter(fw);
			bufw.write(gongSiName + ":" + email);
			bufw.newLine();
			bufw.flush();

		} catch (Exception e) {
			logger.info("写入文件出错", e);
		} finally {
			try {
				bufw.close();
			} catch (IOException e) {
				logger.error("关闭出错", e);
			}
		}

	}
   //每个企业官网每个子页面的解析
	private String parseSpecificGongSiUrl(String gongsiURl) {
		String htmlString = getHtmlByUrl(gongsiURl);
		if (null == htmlString)
			return null;
		Document doc = Jsoup.parse(htmlString);
		List<Element> list = doc.select("a[href]");
		String email = null;
		for (Element element : list) {
			String hreix = element.attr("href");
			if (hreix.startsWith("www") || hreix.startsWith("javascript"))
				continue;
			if (hreix.contains("http")) {
				if ((hreix.equals("http://")||hreix.contains("###")||hreix.contains("play") || hreix.contains("micv")
						|| hreix.contains("edu") || hreix.contains("tv")
						|| hreix.contains("movie") || hreix.contains("car")
						|| hreix.contains("enc") || hreix.contains("comic")
						|| hreix.contains("{"))) {
					continue;
				}
				
				email = getEmail(hreix.trim());
			} else {
				if ((hreix.equals("http://")||hreix.contains("###")||hreix.contains("play") || hreix.contains("micv")
						|| hreix.contains("edu") || hreix.contains("tv")
						|| hreix.contains("movie") || hreix.contains("car")
						|| hreix.contains("enc") || hreix.contains("comic")
						|| hreix.contains("{"))) {
					continue;
				}
				email = getEmail(gongsiURl + element.attr("href").trim());
			}
			if (null == email) {
				continue;
			} else {
				break;
			}
		}
		return email;
	}

	private String getEmail(String url) {
		logger.info("开始访问页面！！！！！！！！！！！！！！");
		String html = null;
		String eamil = null;
		@SuppressWarnings("resource")
		HttpClient httpClient = new DefaultHttpClient();// 创建httpClient对象
		HttpGet httpget = new HttpGet(url);// 以get方式请求该URL
		System.out.println(url);
		try {
			HttpResponse responce = httpClient.execute(httpget);// 得到responce对象
			int resStatu = responce.getStatusLine().getStatusCode();// 返回码
			if (resStatu == HttpStatus.SC_OK) {// 200正常 其他就不对
				// 获得相应实体
				HttpEntity entity = responce.getEntity();
				if (entity != null) {
					html = EntityUtils.toString(entity);// 获得html源代码
					Document doc = Jsoup.parse(html);
					for (Element element : doc.getElementsByTag("div")) {
						String str = element.toString();
						if (str.contains("@")) {
							if (str.length() > 1000) {
								List<String> list = Arrays.asList(str
										.split("\\s+"));
								for (String line : list) {
									System.out.println("list长度  :"
											+ list.size());
									if (line.contains("@")
											&& !line.startsWith("src")) {
										System.out.println(line);
										eamil = getRealEmail(line);
									}
									if (null != eamil)
										break;
								}
							} else {
								eamil = getRealEmail(element.toString());
								if (null != eamil)
									break;
							}
						}
						if (null != eamil)
							break;
					}
				}
			} else {
				return null;
			}
		} catch (Exception e) {
			logger.error("出错" + e);
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return eamil;
	}

	private String getRealEmail(String html) {
		String eamil = null;
		try {
			String check = "([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}";
			Pattern regex = Pattern.compile(check);
			Matcher matcher = regex.matcher(html);
			if (matcher.find()) {
				eamil = matcher.group();
			}
		} catch (Exception e) {
			logger.info("解析出错", e);
		}
		System.out.println("邮箱地址为：" + eamil);
		return eamil;

	}

	/**
	 * 用于解析每层的url，并获取下一层url，然后在解析每个官方网站的url
	 * 
	 * @param url
	 * @return
	 */
	public Stack<String> getUrl1(String url) {
		String htmlString = getHtmlByUrl(url);
		if (null == htmlString)
			return null;
		Document doc = Jsoup.parse(htmlString);
		Stack<String> stack = new Stack<String>();
		List<Element> list = doc.select("a[href]");
		for (Element element : list) {
			String urlHref = element.attr("href");
			// System.out.println(urlHref);
			if (urlHref.contains("www.lagou.com/zhaopin/")
					&& !urlHref.equals("//www.lagou.com/zhaopin/")) {
				stack.add((String) urlHref.subSequence(2, urlHref.length()));
				// System.out.println(urlHref.subSequence(2, urlHref.length()));
			}
		}
		return stack;

	}

	public void getUrl2(String url) {
		
		String htmlString = getHtmlByUrl(url);
		if (null == htmlString)
			return;
		Document doc = Jsoup.parse(htmlString);
		Set<String> set = new HashSet<String>();

		List<Element> list = doc.select("a[href]");
		HashSet<String> yeMianSet = new HashSet<String>();
		TreeMap<Integer, String> bianHaoTM = new TreeMap<Integer, String>();
		for (Element element : list) {
			String urlHref = element.attr("href");
			// System.out.println(urlHref);
			if (urlHref.startsWith("//www.lagou.com/")) {
				set.add(urlHref);
			}
		}
		for (String str : set) {
			if (str.contains("gongsi") && str.endsWith(".html")) {
				gongSiSet.add("http://" + str.subSequence(2, str.length()));
			}
			if (str.contains("jobs") && str.endsWith(".html")) {
				jobsSet.add("http://" + str.subSequence(2, str.length()));
			}
			if (str.contains("zhaopin")) {
				String urlSub = (String) str.subSequence(2, str.length());
				String[] urlArr = urlSub.split("/");
				if (urlArr.length < 4)
					continue;
				bianHaoTM.put(Integer.parseInt(urlArr[urlArr.length - 1]),
						urlSub);

			}
		}
		if (bianHaoTM.size() < 1)
			return;
		int bianHao = bianHaoTM.lastKey();
		String urlZhaoPin = bianHaoTM.get(bianHao);
		String[] urlZhaoPinArr = urlZhaoPin.split("/");
		StringBuilder sBuilder = new StringBuilder();
		for (int i = 0; i < urlZhaoPinArr.length - 1; i++) {
			sBuilder.append(urlZhaoPinArr[i]).append("/");
		}
		String prifx = sBuilder.toString();
		for (int i = 2; i <= bianHao; i++) {
			yeMianSet.add("http://" + prifx + i + "/");
			System.out.println(prifx + i + "/");
		}
		getSubUrl(yeMianSet);

	}

	// www.lagou.com/zhaopin/Java/2/
	public void getSubUrl(Set<String> yeMianSet) {
		Set<String> set = new HashSet<String>();
		for (String subUrl : yeMianSet) {
			String htmlString = getHtmlByUrl(subUrl);
			if (null == htmlString)
				return;
			Document doc = Jsoup.parse(htmlString);
			List<Element> list = doc.select("a[href]");
			for (Element element : list) {
				String urlHref = element.attr("href");
				// System.out.println(urlHref);
				if (urlHref.startsWith("//www.lagou.com/")) {
					set.add(urlHref);
				}
			}
			for (String str : set) {
				if (str.contains("gongsi") && str.endsWith(".html")) {
					gongSiSet.add("http://" + str.subSequence(2, str.length()));
				}
				if (str.contains("jobs") && str.endsWith(".html")) {
					jobsSet.add("http://" + str.subSequence(2, str.length()));
				}
			}
			System.out.println("gongSiSet:" + gongSiSet.size() + "------------"
					+ gongSiSet.toString());
			System.out
					.println("---------------------------------------------------------------------");
			System.out.println("jobsSet:" + jobsSet.size() + "------------"
					+ jobsSet.toString());
			parseJobsUrl(jobsSet);
		}
	}

	// gongSiSet
	public void parseJobsUrl(Set<String> jobsSet) {
		Set<String> set = new HashSet<String>();
		for (String subUrl : jobsSet) {
			String htmlString = getHtmlByUrl(subUrl);
			if (null == htmlString)
				return;
			Document doc = Jsoup.parse(htmlString);
			List<Element> list = doc.select("a[href]");
			for (Element element : list) {
				String urlHref = element.attr("href");
				// System.out.println(urlHref);
				if (urlHref.startsWith("//www.lagou.com/")) {
					set.add(urlHref);
				}
			}
			for (String str : set) {
				if (str.contains("gongsi") && str.endsWith(".html")) {
					gongSiSet.add("http://" + str.subSequence(2, str.length()));
				}

			}
			System.out
					.println("---------------------------------------------------------------------");
			System.out.println("gongSiSet:" + gongSiSet.size() + "------------"
					+ gongSiSet.toString());
		}

	}

	// httpclient进行网页爬虫
	public String getHtmlByUrl(String url) {
		logger.info("开始访问页面！！！！！！！！！！！！！！");
		String html = null;
		@SuppressWarnings("resource")
		HttpClient httpClient = new DefaultHttpClient();// 创建httpClient对象
		HttpGet httpget = new HttpGet(url);// 以get方式请求该URL
		try {
			HttpResponse responce = httpClient.execute(httpget);// 得到responce对象
			int resStatu = responce.getStatusLine().getStatusCode();// 返回码
			if (resStatu == HttpStatus.SC_OK) {// 200正常 其他就不对
				// 获得相应实体
				HttpEntity entity = responce.getEntity();
				if (entity != null) {
					html = EntityUtils.toString(entity);// 获得html源代码
				}
			}
		} catch (Exception e) {
			logger.error("出错" + e);
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return html;
	}

	// 只用于计算hash值
	public static class BloomFiler {
		private int cap;
		// seed为计算hash值的一个key值，具体对应上文中的seeds数组
		private int seed;

		public BloomFiler(int cap, int seed) {
			this.cap = cap;
			this.seed = seed;
		}

		public BloomFiler() {
		}

		public int getCap() {
			return cap;
		}

		public void setCap(int cap) {
			this.cap = cap;
		}

		public int getSeed() {
			return seed;
		}

		public void setSeed(int seed) {
			this.seed = seed;
		}

		public int hash(String value) {
			int result = 0;
			int length = value.length();
			for (int i = 0; i < length; i++) {
				result = seed * result + value.charAt(i);
			}
			return (cap - 1) & result;

		}
	}

	// 已经入库的biz号把他们映射到bitset中
	/*
	 * public void init() { logger.info("开始获取bloomfilter的位数据"); setBitset();
	 * String sql = "SELECT DISTINCT biz from weixintext"; /MysqlUtils mysqlUtil
	 * = new MysqlUtils(); Connection conn = mysqlUtil.connectionDB();
	 * PreparedStatement preparedStatement = null; // List<String> list = new
	 * ArrayList<String>();\ResultSet rs ResultSet rs = null; try {
	 * preparedStatement = conn.prepareStatement(sql); rs =
	 * preparedStatement.executeQuery(); if (null == rs) { return; } else {
	 * while (rs.next()) { add(rs.getString(1)); } } } catch (SQLException e) {
	 * logger.error(e); } finally { try { rs.close(); conn.close(); } catch
	 * (SQLException e) { logger.info("关闭异常", e); }
	 * 
	 * }
	 * 
	 * }
	 */
	// 给bitset设置值
	public void setBitset() {
		for (int i = 0; i < seeds.length; i++) {
			bfFuncs[i] = new BloomFiler(DEFAULT_VALUE, seeds[i]);
		}
	}

	// 给某些string设置其bitset8个位置的值
	public void add(String value) {
		for (BloomFiler bf : bfFuncs) {
			bs.set(bf.hash(value), true);
		}
	}

	// 判断该字符串是否已经爬过
	public boolean isExist(String str) {
		if (null == str)
			return false;
		// 如果判断8个hash函数值中有一个位置不存在即可判断为不存在Bloofilter中
		for (BloomFiler bf : bfFuncs) {
			if (!bs.get(bf.hash(str))) {
				return false;
			}
		}
		return true;

	}
}
