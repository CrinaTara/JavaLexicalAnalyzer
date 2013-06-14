import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author 石夏星（1006840532）
 * 如无法正常编译、运行，请联系：starsasumi@gmail.com
 *
 */
public class SaxParserApp extends DefaultHandler{
	private String preTag = null;	// 记录解析时的上一个节点名称
	private HashMap<String,Integer> stocks;	// 存储<股票代号,订购总数>键值对
	private String parsingStock = null;	// 记录正在解释的股票代号
	
	public SaxParserApp(){
		stocks = new HashMap<String,Integer>();
	}
	
	public void parseDocument(File xmlDoc) throws ParserConfigurationException, SAXException, IOException {
		SAXPrserFactory factory = SAXParserFactory.newInstance();  
        SAXParser parser = factory.newSAXParser();  
        SaxParserApp handler = new SaxParserApp();  
        parser.parse(xmlDoc, handler);
	}
	
	@Override
	public void startDocument() throws SAXException {
	}
	
	@Override
	public void endDocument() throws SAXException {
		// 整个文档解释结束后，输出结果
		Set<Entry<String, Integer>> stockSet = this.stocks.entrySet();
		Iterator<Entry<String, Integer>> i = stockSet.iterator();
		while (i.hasNext()) {
			Entry<String, Integer> stock = (Map.Entry<String, Integer>) i.next();
			System.out.println("股票代号=" + stock.getKey() + ";订购总数=" + stock.getValue());
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		preTag = qName;//将正在解析的节点名称赋给preTag
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		
		preTag = null;
		/* 当解析结束时置为空。这里很重要，否则可能解释时前后节点关系错误 */
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if(preTag!=null){
			String content = new String(ch,start,length);
			content = content.trim();
			
			if (this.preTag.equals("StockSymbol")) {
				this.parsingStock = content;	// 正在解释一支股票
				if (!this.stocks.containsKey(parsingStock)) {
					this.stocks.put(parsingStock, 0);	// 若发现新的股票代号，将它加入 stocks
				}
			} else if (this.preTag.equals("Quantity")) {
				if	(this.parsingStock == null)
					throw new SAXException("parsingStock is null.");	// 无法获知正在解释的股票，异常
				Integer value = this.stocks.get(parsingStock);
				this.stocks.put(parsingStock, Integer.parseInt(content) + value.intValue());	// 将这笔订购数加入到总数中
				this.parsingStock = null;
			}
		}
	}
	
	public static void main(String args[]) {
		File xmlFile = new File("Orders.xml");
		SaxParserApp parser = new SaxParserApp();
		try {
			parser.parseDocument(xmlFile);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}