package com.values.batch.main.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.values.batch.main.persistence.MainMapper;
import com.values.batch.main.util.DartDataCorpCode;
import com.values.batch.main.util.DartDataMazzInfo;
import com.values.batch.main.util.OpenDataBondInfo;
import com.values.batch.main.util.OpenDataKosInfo;
import com.values.batch.main.util.OpenDataStockPrice;

@Service
@Transactional
public class MainServiceImpl implements MainService {
	
	@Autowired
	private MainMapper mainMapper;
	
	@Value("${spring.servlet.multipart.location}")
    private String uploadPath;
	
	// 결과 메시지 맵 생성
	private Map<String, Object> resultMsg(boolean result, String msg, int cnt) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("LOG_RESULT", (result ? "Y" : "N"));
		map.put("LOG_MSG", msg);
		map.put("LOG_CNT", cnt);
		return map;
	}
	
	// XML 파싱 
	private Document parseXML(InputStream stream) throws Exception{
        DocumentBuilderFactory objDocumentBuilderFactory = null;
        DocumentBuilder objDocumentBuilder = null;
        Document doc = null;
        try{
            objDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
            objDocumentBuilder = objDocumentBuilderFactory.newDocumentBuilder();
            doc = objDocumentBuilder.parse(stream);
        } catch(Exception ex){
            throw ex;
        }      
        return doc;
    }
	
	@Override
	public void saveBatchLog(Map<String, Object> params) {
		mainMapper.saveBatchLog(params);
	}

	@Override
	public Map<String, Object> saveOpenApiBondInfo(Map<String, Object> params) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<Map<String, Object>> date = mainMapper.findToday(params);
		String fromDate = "";
				
		if(date.size() > 0) {
			fromDate = (String) date.get(0).get("PREV_DATE");
			fromDate = fromDate.replaceAll("-", "");
			
			if(params.get("SET_DATE") != null) {
				fromDate = (String) params.get("SET_DATE");
			}
			
			OpenDataBondInfo openDataBondInfo = new OpenDataBondInfo();
			Map<String, Object> map = null;
			try {
				
				int totalCnt = openDataBondInfo.getOpenDataBondInfoCnt(fromDate);
				double dblCnt = Double.parseDouble(totalCnt + "") / 1000.0;
				int cnt = (int) Math.ceil(dblCnt);
				
				if(cnt > 1) {
					for(int i = 0; i < cnt; i++) {
						map = openDataBondInfo.getOpenDataBondInfoJson(fromDate, (i + 1) + "");
						boolean result = false;
						
						if(map.get("result") != null) {
							result = (boolean) map.get("result");
						}
						
						System.out.println("result : " + result + " i : " + i);
						if(result) {
							map.put("STD_DATE", fromDate);
							if(i == 0) {
								mainMapper.saveOpenBondInfoRemove(map);
							}
							mainMapper.saveOpenBondInfo(map);
						}
					}
				} else {
					map = openDataBondInfo.getOpenDataBondInfoJson(fromDate, "1");
					boolean result = false;
					
					if(map.get("result") != null) {
						result = (boolean) map.get("result");
					}
					
					System.out.println("result : " + result);
					if(result) {
						map.put("STD_DATE", fromDate);
						mainMapper.saveOpenBondInfoRemove(map);
						mainMapper.saveOpenBondInfo(map);
					}
				}
				
				String msg = fromDate + " 기준 : " + totalCnt + " 건 저장 완료";
				resultMap = resultMsg(true, msg, totalCnt);
			} catch (XPathExpressionException e) {
				e.printStackTrace();
				resultMap = resultMsg(false, "XPathExpressionException", 0);
			} catch (IOException e) {
				e.printStackTrace();
				resultMap = resultMsg(false, "IOException", 0);
			} catch (SAXException e) {
				e.printStackTrace();
				resultMap = resultMsg(false, "SAXException", 0);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
				resultMap = resultMsg(false, "ParserConfigurationException", 0);
			} catch (Exception e) {
				e.printStackTrace();
				resultMap = resultMsg(false, "Exception", 0);
			}
		} else {
			resultMap = resultMsg(false, "날짜 리스트 데이터 없음", 0);
		}
		
		return resultMap;
	}
	
	@Override
	public Map<String, Object> saveOpenApiStockInfo(Map<String, Object> params) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<Map<String, Object>> date = mainMapper.findToday(params);
		
		System.out.println("date : " + date.size());
		
		String fromDate = "";
		String toDate = "";
				
		if(date.size() > 0) {
			fromDate = (String) date.get(0).get("PREV_DATE");
			toDate = (String) date.get(0).get("STD_DATE");
			fromDate = fromDate.replaceAll("-", "");
			toDate = toDate.replaceAll("-", "");
			
			String orgDate = (String) params.get("orgDate");
			
			if(params.get("SET_FROM_DATE") != null) {
				if(!params.get("SET_FROM_DATE").equals("")) {
					fromDate = (String) params.get("SET_FROM_DATE");
					toDate = (String) params.get("SET_TO_DATE");
				}
			}
			
			OpenDataStockPrice openDataStockPrice = new OpenDataStockPrice();
			Map<String, Object> map = null;
			try {
				
				int totalCnt = openDataStockPrice.getOpenDataStockPriceCnt(fromDate, toDate, orgDate);
				double dblCnt = Double.parseDouble(totalCnt + "") / 1000.0;
				int cnt = (int) Math.ceil(dblCnt);
				
				System.out.println("saveOpenApiStockInfo parsing count is ...." + cnt);
				if(cnt > 1) {
					for(int i = 0; i < cnt; i++) {
						map = openDataStockPrice.getOpenDataStockPrice(fromDate, toDate, orgDate, (i + 1) + "");
						
						boolean result = false;
						
						if(map.get("result") != null) {
							result = (boolean) map.get("result");
						}
						if(result) {
							if(orgDate != null && !orgDate.equals("")) {
								map.put("STD_DATE", orgDate);
							} else {
								map.put("STD_DATE", fromDate);
							}
							
							if(i == 0) {
								mainMapper.saveOpenStockInfoRemove(map);
							}
							mainMapper.saveOpenStockInfo(map);
						}
					}
				} else {
					map = openDataStockPrice.getOpenDataStockPrice(fromDate, toDate, orgDate, "1");
					
					boolean result = false;
					
					if(map.get("result") != null) {
						result = (boolean) map.get("result");
					}
					if(result) {
						if(orgDate != null && !orgDate.equals("")) {
							map.put("STD_DATE", orgDate);
						} else {
							map.put("STD_DATE", fromDate);
						}
						mainMapper.saveOpenStockInfoRemove(map);
						mainMapper.saveOpenStockInfo(map);
					}
				}
				
				String msg = fromDate + " 기준 : " + totalCnt + " 건 저장 완료";
				resultMap = resultMsg(true, msg, totalCnt);
				
			} catch (XPathExpressionException e) {
				e.printStackTrace();
				resultMap = resultMsg(false, "XPathExpressionException", 0);
			} catch (IOException e) {
				e.printStackTrace();
				resultMap = resultMsg(false, "IOException", 0);
			} catch (SAXException e) {
				e.printStackTrace();
				resultMap = resultMsg(false, "SAXException", 0);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
				resultMap = resultMsg(false, "ParserConfigurationException", 0);
			} catch (Exception e) {
				e.printStackTrace();
				resultMap = resultMsg(false, "Exception", 0);
			}
		} else {
			resultMap = resultMsg(false, "날짜 리스트 데이터 없음", 0);
		}
		
		return resultMap;
	}
	
	@Override
	public Map<String, Object> saveOpenApiKosInfo(Map<String, Object> params) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<Map<String, Object>> date = mainMapper.findToday(params);
		String fromDate = "";
				
		if(date.size() > 0) {
			fromDate = (String) date.get(0).get("PREV_DATE");
			fromDate = fromDate.replaceAll("-", "");
			
			if(params.get("SET_DATE") != null) {
				fromDate = (String) params.get("SET_DATE");
			}
			
			OpenDataKosInfo openDataKosInfo = new OpenDataKosInfo();
			Map<String, Object> map = null;
			try {
				
				int totalCnt = openDataKosInfo.getOpenDataKosInfoCnt(fromDate);
				double dblCnt = Double.parseDouble(totalCnt + "") / 1000.0;
				int cnt = (int) Math.ceil(dblCnt);
				
				if(cnt > 1) {
					for(int i = 0; i < cnt; i++) {
						map = openDataKosInfo.getOpenDataKosInfoJson(fromDate, (i + 1) + "");
						boolean result = false;
						
						if(map.get("result") != null) {
							result = (boolean) map.get("result");
						}
						
						System.out.println("result : " + result + " i : " + i);
						if(result) {
							map.put("STD_DATE", fromDate);
							if(i == 0) {
								mainMapper.saveOpenKosInfoRemove(map);
							}
							mainMapper.saveOpenKosInfo(map);
						}
					}
				} else {
					map = openDataKosInfo.getOpenDataKosInfoJson(fromDate, "1");
					boolean result = false;
					
					if(map.get("result") != null) {
						result = (boolean) map.get("result");
					}
					
					System.out.println("result : " + result);
					if(result) {
						map.put("STD_DATE", fromDate);
						mainMapper.saveOpenKosInfoRemove(map);
						mainMapper.saveOpenKosInfo(map);
					}
				}
				
				String msg = fromDate + " 기준 : " + totalCnt + " 건 저장 완료";
				resultMap = resultMsg(true, msg, totalCnt);
			} catch (XPathExpressionException e) {
				e.printStackTrace();
				resultMap = resultMsg(false, "XPathExpressionException", 0);
			} catch (IOException e) {
				e.printStackTrace();
				resultMap = resultMsg(false, "IOException", 0);
			} catch (SAXException e) {
				e.printStackTrace();
				resultMap = resultMsg(false, "SAXException", 0);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
				resultMap = resultMsg(false, "ParserConfigurationException", 0);
			} catch (Exception e) {
				e.printStackTrace();
				resultMap = resultMsg(false, "Exception", 0);
			}
		} else {
			resultMap = resultMsg(false, "날짜 리스트 데이터 없음", 0);
		}
		
		return resultMap;
	}
	
	@Override
	public Map<String, Object> saveCrawlCurveInfo(Map<String, Object> params) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		// DB 어제 날짜 겟 
		String yesterDay = "";
		List<Map<String, Object>> list = mainMapper.findToday(params);
		if(list.size() > 0) {
			yesterDay = (String) list.get(0).get("YESTERDAY");
		}
		
		if(!yesterDay.equals("")) {
			String paramStr = "<message>";
			paramStr += "<proframeHeader>";
			paramStr += "<pfmAppName>BIS-KOFIABOND</pfmAppName>";
			paramStr += "<pfmSvcName>BISBndSrtPrcSrchSO</pfmSvcName>";
			paramStr += "<pfmFnName>selectDay</pfmFnName>";
			paramStr += "</proframeHeader>";
			paramStr += "<systemHeader></systemHeader>";
			paramStr += "<BISBndSrtPrcDayDTO>";
			paramStr += "<standardDt>" + yesterDay + "</standardDt>";
			paramStr += "<reportCompCd>A10000</reportCompCd>";
			paramStr += "<applyGbCd>C00</applyGbCd>";
			paramStr += "</BISBndSrtPrcDayDTO>";
			paramStr += "</message>";
			
			String xml_string_to_send = paramStr;
	        HttpURLConnection connection = null;

	        OutputStream os =null;
	        try{
	            //전송할 서버 url
	            URL searchUrl = new URL("https://www.kofiabond.or.kr/proframeWeb/XMLSERVICES/");
	            connection = (HttpURLConnection)searchUrl.openConnection();
	            connection.setDoOutput(true);
	            connection.setRequestMethod("POST");

	            connection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
	            connection.setRequestProperty( "Content-Length", Integer.toString(xml_string_to_send.length()) );
	            os = connection.getOutputStream();
	            os.write( xml_string_to_send.getBytes("utf-8") );
	            os.flush();
	            os.close();

	            //결과값 수신
	            int rc = connection.getResponseCode();
	            
	            System.out.println("rc : " + rc);
	            if(rc==200){
	                try {
						Document doc = parseXML(connection.getInputStream());
						NodeList cntNodes = doc.getElementsByTagName("dbio_total_count_");
						int cnt = Integer.parseInt(cntNodes.item(0).getTextContent());
						// 조회한 날짜의 실제 데이터가 있는 경우 
						// 1. 헤더정보 겟 하여 저장
						// 2. 실제 데이터 파싱하여 저장
						if(cnt > 0) {
							
							Map<String, Object> map = null;
							
							// 헤더 정보 저장
							saveHeaderInformation(yesterDay);
							
							NodeList descNodes = doc.getElementsByTagName("BISBndSrtPrcDayDTO");
							
							for(int i = 0; i < descNodes.getLength(); i++) {
								map = new HashMap<String, Object>();
								map.put("STD_DATE", yesterDay);
								
								for(Node node = descNodes.item(i).getFirstChild(); node!=null; node=node.getNextSibling()){ //첫번째 자식을 시작으로 마지막까지 다음 형제를 실행
									map.put("IDX", (i+1));
					                if(node.getNodeName().equals("reportCompCd")){
					                    map.put("REPORT_COMP_CD", node.getTextContent());
					                } else if(node.getNodeName().equals("sigaBrnCd")){
					                    map.put("SIGA_BRN_CD", node.getTextContent());
					                } else if(node.getNodeName().equals("largeCategoryMrk")){
					                    map.put("LARGE_CATEGORY_MRK", node.getTextContent());
					                } else if(node.getNodeName().equals("typeNmMrk")){
					                    map.put("TYPE_NM_MRK", node.getTextContent());
					                } else if(node.getNodeName().equals("creditRnkMrk")){
					                    map.put("CREDIT_RNK_MRK", node.getTextContent());
					                } else if(node.getNodeName().equals("val1")){
					                    map.put("VAL1", node.getTextContent());
					                } else if(node.getNodeName().equals("val2")){
					                    map.put("VAL2", node.getTextContent());
					                } else if(node.getNodeName().equals("val3")){
					                    map.put("VAL3", node.getTextContent());
					                } else if(node.getNodeName().equals("val4")){
					                    map.put("VAL4", node.getTextContent());
					                } else if(node.getNodeName().equals("val5")){
					                    map.put("VAL5", node.getTextContent());
					                } else if(node.getNodeName().equals("val6")){
					                    map.put("VAL6", node.getTextContent());
					                } else if(node.getNodeName().equals("val7")){
					                    map.put("VAL7", node.getTextContent());
					                } else if(node.getNodeName().equals("val8")){
					                    map.put("VAL8", node.getTextContent());
					                } else if(node.getNodeName().equals("val9")){
					                    map.put("VAL9", node.getTextContent());
					                } else if(node.getNodeName().equals("val10")){
					                    map.put("VAL10", node.getTextContent());
					                } else if(node.getNodeName().equals("val11")){
					                    map.put("VAL11", node.getTextContent());
					                } else if(node.getNodeName().equals("val12")){
					                    map.put("VAL12", node.getTextContent());
					                } else if(node.getNodeName().equals("val13")){
					                    map.put("VAL13", node.getTextContent());
					                } else if(node.getNodeName().equals("val14")){
					                    map.put("VAL14", node.getTextContent());
					                } else if(node.getNodeName().equals("val15")){
					                    map.put("VAL15", node.getTextContent());
					                } else if(node.getNodeName().equals("val16")){
					                    map.put("VAL16", node.getTextContent());
					                } else if(node.getNodeName().equals("val17")){
					                    map.put("VAL17", node.getTextContent());
					                } else if(node.getNodeName().equals("val18")){
					                    map.put("VAL18", node.getTextContent());
					                } else if(node.getNodeName().equals("val19")){
					                    map.put("VAL19", node.getTextContent());
					                } else if(node.getNodeName().equals("val20")){
					                    map.put("VAL20", node.getTextContent());
					                } 
					            }
								// KOFIA BOND 데이터 저장
								mainMapper.saveInsertKofiaBondList(map);
							}
							
							String msg = yesterDay + " 기준 : " + descNodes.getLength() + " 건 저장 완료";
							resultMap = resultMsg(true, msg, descNodes.getLength());
							
						}
						
					} catch (Exception e) {
						resultMap = resultMsg(false, "Exception", 0);
						e.printStackTrace();
					}
	            } else {
	                System.out.println("http response code error: "+rc+"\n");
	                resultMap = resultMsg(false, "http response code error", 0);
	            }
	        } catch( IOException e ){
	            System.out.println("search URL connect failed: " + e.getMessage());
	            e.printStackTrace();
	            resultMap = resultMsg(false, "IOException", 0);
	        } finally {
	        	if(os!=null)
					try {
						os.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	connection.disconnect();
	        }
		}
		return resultMap;
	}
	
	// KOFIA BOND 크롤링 데이터 헤더 정보 추출
	private void saveHeaderInformation(String yesterDay) {
		String hParamStr = "<message>";
		hParamStr += "<proframeHeader>";
		hParamStr += "<pfmAppName>BIS-KOFIABOND</pfmAppName>";
		hParamStr += "<pfmSvcName>BISBndSrtPrcSrchSO</pfmSvcName>";
		hParamStr += "<pfmFnName>getHeadList</pfmFnName>";
		hParamStr += "</proframeHeader>";
		hParamStr += "<systemHeader></systemHeader>";
		hParamStr += "<BISBndSrtPrcDayDTO>";
		hParamStr += "<standardDt>" + yesterDay + "</standardDt>";
		hParamStr += "<reportCompCd>A10000</reportCompCd>";
		hParamStr += "<applyGbCd>C00</applyGbCd>";
		hParamStr += "</BISBndSrtPrcDayDTO>";
		hParamStr += "</message>";
		
		String xml_string_to_send = hParamStr;
		
		String returnString = "";
        HttpURLConnection connection = null;

        OutputStream os =null;
        try{
            //전송할 서버 url
            URL searchUrl = new URL("https://www.kofiabond.or.kr/proframeWeb/XMLSERVICES/");
            connection = (HttpURLConnection)searchUrl.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            connection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
            connection.setRequestProperty( "Content-Length", Integer.toString(xml_string_to_send.length()) );
            os = connection.getOutputStream();
            os.write( xml_string_to_send.getBytes("utf-8") );
            os.flush();
            os.close();

            //결과값 수신
            int rc = connection.getResponseCode();
            if(rc==200){
                try {
					Document doc = parseXML(connection.getInputStream());
					NodeList cntNodes = doc.getElementsByTagName("dbio_total_count_");
					
					int cnt = Integer.parseInt(cntNodes.item(0).getTextContent());
					
					System.out.println("cnt : " + cnt);
					
					if(cnt > 0) {
						
						NodeList descNodes = doc.getElementsByTagName("BISBndSrtPrcDayDTO");
						
						for(int i = 0; i < descNodes.getLength(); i++) {
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("STD_DATE", yesterDay);
							
							for(Node node = descNodes.item(i).getFirstChild(); node!=null; node=node.getNextSibling()){ //첫번째 자식을 시작으로 마지막까지 다음 형제를 실행
								 
				                if(node.getNodeName().equals("attrCd")){
				                    map.put("ATTR_CD", node.getTextContent());
				                    
				                    String cd = node.getTextContent();
				                    String year = cd.substring(0, 2);
				                    String month = cd.substring(2);
				                    
				                    int intYear = Integer.parseInt(year);
				                    int intMonth = Integer.parseInt(month);
				                    
				                    double x = (100.0 * (double)intMonth) / 12.0 / 100.0;
				                    
				                    map.put("TERM", String.format("%.2f", (intYear + x)));
				                } else if(node.getNodeName().equals("remainTrmCtgy")){
				                    map.put("REMAIN_TRM_CTGY", node.getTextContent());
				                } else if(node.getNodeName().equals("remainEngTrmContContent")){
				                    map.put("REMAIN_TRM_ENG", node.getTextContent());
				                } 
				            }
							mainMapper.saveInsertKofiaHeaderList(map);
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            } else {
                System.out.println("http response code error: "+rc+"\n");
                return;
            }
        } catch( IOException e ){
            System.out.println("search URL connect failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
        	if(os!=null)
				try {
					os.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	connection.disconnect();
        }
	}
	
	@Override
	public Map<String, Object> saveOpenDartMazzCode(Map<String, Object> params) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		DartDataCorpCode dartDataCorpCode = new DartDataCorpCode();
		Map<String, Object> map = null;
		try {
			map = dartDataCorpCode.getDartDataCorpCode(uploadPath);
			boolean result = false;
			
			if(map.get("result") != null) {
				result = (boolean) map.get("result");
			}
			
			if(result) {
				mainMapper.saveOpenDartMazzCodeRemove(map);
				mainMapper.saveOpenDartMazzCode(map);
			}
			
			int totalCnt = (int) map.get("TOTAL_CNT");
			String msg = totalCnt + " 건 저장 완료";
			resultMap = resultMsg(true, msg, totalCnt);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap = resultMsg(false, "Exception", 0);
		}
		
		return resultMap;
	}
	
	@Override
	public Map<String, Object> saveOpenDartMazzInfo(Map<String, Object> params) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		// 직전일 조회 
		List<Map<String, Object>> date = mainMapper.findToday(params);
		String stdDate = "";
				
		if(date.size() > 0) {
			stdDate = (String) date.get(0).get("PREV_DATE");
			stdDate = stdDate.replaceAll("-", "");
		}
		
		// 채권발행정보 리스트 조회 
		params.put("STD_DATE", stdDate);
		List<Map<String, Object>> bondList = mainMapper.findOpenDataBondInfo(params);
		System.out.println("bondList : " + bondList.size());
		
		// 조회된 발행정보 리스트 만큼 
		// 공시 기업코드 조회 
		int totalCnt = 0;
		Map<String, Object> paramMap = new HashMap<String, Object>();
		List<Map<String, Object>> saveList = new ArrayList<Map<String, Object>>();
		for(Map<String, Object> item : bondList) {
			List<Map<String, Object>> list = mainMapper.findOpenDartMazzCode(item);
			if(list.size() > 0) {
				String corpCode = (String) list.get(0).get("CORP_CODE");
				if(!"".equals(corpCode) && corpCode != null) {
					String mazzYn = (String) item.get("MAZZ_YN");
					String fromDate = (String) item.get("YYYY") + "0101";
					String toDate = (String) item.get("YYYY") + "1231";
					DartDataMazzInfo dartDataMazzInfo = new DartDataMazzInfo();
					
					List<Map<String, Object>> resultList = dartDataMazzInfo.getDartDataMazzInfo(mazzYn, corpCode, fromDate, toDate);
					
					for(Map<String, Object> map : resultList) {
						boolean result = false;
						
						if(map.get("result") != null) {
							result = (boolean) map.get("result");
						}
						
						if(result) {
							saveList.add(map);
							totalCnt++;
						}
					}
				}
			}
		}
		
		paramMap.put("list", saveList);
		
		mainMapper.saveOpenDartMazzInfoRemove(paramMap);
		mainMapper.saveOpenDartMazzInfo(paramMap);
		
		resultMap = resultMsg(true, stdDate + " 기준 : " + totalCnt + " 건 저장 완료", totalCnt);
		resultMap.put("TOTAL_CNT", saveList.size());
		
		return resultMap;
	}

}
