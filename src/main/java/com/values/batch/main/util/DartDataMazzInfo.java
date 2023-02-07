package com.values.batch.main.util;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/*
 * 2023-02-04
 * https://opendart.fss.or.kr/guide/detail.do?apiGrpCd=DS005&apiId=2020033 외 
 * DART open api 를 통한 메짜닌 데이터를 조회 
 * 'DART_기업공시코드 및 공공데이터포털 발행채권정보를 통하여 다트에서 제공하는 메짜닌 정보를 가져오기 위함' 
 * 신청일    2023년 
 * URL - https://opendart.fss.or.kr/api/cvbdIsDecsn.xml 외 
 * 인증키 - 5077aca71b48161728f18cb794caad946134abbc
 */
public class DartDataMazzInfo {
	private String cbUrl = "https://opendart.fss.or.kr/api/cvbdIsDecsn.xml"; // 전환사채 : https://opendart.fss.or.kr/guide/detail.do?apiGrpCd=DS005&apiId=2020033
	private String ebUrl = "https://opendart.fss.or.kr/api/exbdIsDecsn.xml"; // 교환사채 : https://opendart.fss.or.kr/guide/detail.do?apiGrpCd=DS005&apiId=2020035
	private String bwUrl = "https://opendart.fss.or.kr/api/bdwtIsDecsn.xml"; // 신주인수권부사채권 : https://opendart.fss.or.kr/guide/detail.do?apiGrpCd=DS005&apiId=2020034
	private String mainUrl = "";
	private String apiKey = "5077aca71b48161728f18cb794caad946134abbc";
	
	public List<Map<String, Object>> getDartDataMazzInfo(String mazzYn, String corpCode, String fromDate, String toDate) {
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		HttpURLConnection con = null;
        String s = null; // 에러 메시지
        Map<String, Object> map = null;
        
        System.out.println("== getDartDataMazzInfo ======================================================");
        System.out.println("mazzYn : " + mazzYn);
        System.out.println("corpCode : " + corpCode);
        System.out.println("fromDate : " + fromDate);
        System.out.println("toDate : " + toDate);
        
		if(mazzYn.equals("CB")) {
			mainUrl = cbUrl;
		} else if(mazzYn.equals("EB")) {
			mainUrl = ebUrl;
		} else {
			mainUrl = bwUrl;
		}
		
        try{
            URL url = new URL(mainUrl 
            		+ "?crtfc_key=" + apiKey
            		+ "&corp_code=" + corpCode
            		+ "&bgn_de=" + fromDate
            		+ "&end_de=" + toDate
            		);
            con = (HttpURLConnection)url.openConnection();
            con.setRequestProperty("Accept-language", "ko");
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(con.getInputStream());
            
            boolean ok = false; // <status>000</status> 획득 여부
            
            Element e;
            NodeList ns = doc.getElementsByTagName("result");
            if (ns.getLength() > 0)
            {
                e = (Element)ns.item(0);
                if (e.getElementsByTagName("status").item(0).getTextContent().equals("000")) {
                    ok = true; // 성공 여부
                	System.out.println("ok : " + ok);
                }
                else { // 에러 메시지 
                    s = e.getElementsByTagName("message").item(0).getTextContent();	
                	System.out.println("err : " + s);
            	}
            }
            
            if (ok)
            {
                ns = doc.getElementsByTagName("list");
                System.out.println("ns.getLength() : " + ns.getLength());
                for (int i = 0; i < ns.getLength(); i++)
                {
                	map = new HashMap<String, Object>();
                    e = (Element)ns.item(i);
                    // 공통 데이터 
                    String corpName = e.getElementsByTagName("corp_name").item(0).getTextContent(); // 회사이름
                    String bondKind = e.getElementsByTagName("bd_knd").item(0).getTextContent(); // 사채종류
                    String bondTm = e.getElementsByTagName("bd_tm").item(0).getTextContent(); // 회차
                    String bondAmount = e.getElementsByTagName("bd_fta").item(0).getTextContent(); // 발행금액
                    String dueDate = e.getElementsByTagName("bd_mtd").item(0).getTextContent(); // 만기일
                    dueDate = dueDate.replace("년 ", "").replace("월 ", "").replace("일", "");
                    String payDate = e.getElementsByTagName("pymd").item(0).getTextContent(); // 납입일
                    payDate = payDate.replace("년 ", "").replace("월 ", "").replace("일", "");
                    String coupon = e.getElementsByTagName("bd_intr_ex").item(0).getTextContent(); // 표면이자율
                    String ytm = e.getElementsByTagName("bd_intr_sf").item(0).getTextContent(); // 만기이자율
                    String bondMethod = e.getElementsByTagName("bdis_mthn").item(0).getTextContent(); // 발행방법(공모, 사모)
                    
                    map.put("CORP_NAME", corpName + "");
                    map.put("BOND_KIND", bondKind + "");
                    map.put("BOND_TM", bondTm + "");
                    map.put("BOND_AMOUNT", bondAmount + "");
                    map.put("DUE_DATE", dueDate + "");
                    map.put("PAY_DATE", payDate + "");
                    map.put("COUPON", coupon + "");
                    map.put("YTM", ytm + "");
                    map.put("BOND_METHOD", bondMethod + "");
                    
                    String ratio = "";
                    String price = "";
                    String startDate = "";
                    String endDate = "";
                    String refixFloor = "";
                    String refixFloorBs = "";
                    String cvKind = "";
                    // CB
                    if(mazzYn.equals("CB")) {
                        ratio = e.getElementsByTagName("cv_rt").item(0).getTextContent(); // 행사비율
                        price = e.getElementsByTagName("cv_prc").item(0).getTextContent(); // 행사가격
                        startDate = e.getElementsByTagName("cvrqpd_bgd").item(0).getTextContent(); // 행사시작일
                        endDate = e.getElementsByTagName("cvrqpd_edd").item(0).getTextContent(); // 행사종료일
                        refixFloor = e.getElementsByTagName("act_mktprcfl_cvprc_lwtrsprc").item(0).getTextContent(); // refixing floor
                        refixFloorBs = e.getElementsByTagName("act_mktprcfl_cvprc_lwtrsprc_bs").item(0).getTextContent(); // refixing floor 조정 근거
                        cvKind = e.getElementsByTagName("cvisstk_knd").item(0).getTextContent(); // 발행종류주식
                    } 
                    else if(mazzYn.equals("EB")) {
                        ratio = e.getElementsByTagName("ex_rt").item(0).getTextContent(); // 행사비율
                        price = e.getElementsByTagName("ex_prc").item(0).getTextContent(); // 행사가격
                        startDate = e.getElementsByTagName("exrqpd_bgd").item(0).getTextContent(); // 행사시작일
                        endDate = e.getElementsByTagName("exrqpd_edd").item(0).getTextContent(); // 행사종료일
                        refixFloor = "-"; // refixing floor
                        refixFloorBs = "-"; // refixing floor 조정 근거
                        cvKind = e.getElementsByTagName("extg").item(0).getTextContent(); // 교환주식
                    }
                    else {
                        ratio = e.getElementsByTagName("ex_rt").item(0).getTextContent(); // 행사비율
                        price = e.getElementsByTagName("ex_prc").item(0).getTextContent(); // 행사가격
                        startDate = e.getElementsByTagName("expd_bgd").item(0).getTextContent(); // 행사시작일
                        endDate = e.getElementsByTagName("expd_edd").item(0).getTextContent(); // 행사종료일
                        refixFloor = e.getElementsByTagName("act_mktprcfl_cvprc_lwtrsprc").item(0).getTextContent(); // refixing floor
                        refixFloorBs = e.getElementsByTagName("act_mktprcfl_cvprc_lwtrsprc_bs").item(0).getTextContent(); // refixing floor 조정 근거
                        cvKind = e.getElementsByTagName("nstk_isstk_knd").item(0).getTextContent(); // 발행종류주식
                    }
                    
                    map.put("RATIO", ratio + "");
                    map.put("PRICE", price + "");
                    map.put("START_DATE", startDate.replace("년 ", "").replace("월 ", "").replace("일", "") + "");
                    map.put("END_DATE", endDate.replace("년 ", "").replace("월 ", "").replace("일", "") + "");
                    map.put("REFIX_FLOOR", refixFloor + "");
                    map.put("REFIX_FLOOR_BS", refixFloorBs + "");
                    map.put("CV_KIND", cvKind + "");
                    
                    map.put("result", true);
                    resultList.add(map);
                }
            } else {
            	map = new HashMap<String, Object>();
            	map.put("result", false);
                resultList.add(map);
            }
            
            return resultList;
        }
        catch (Exception e)
        {
            s = e.getMessage();
            System.out.println("err DartDataMazzInfo Class : " + e.getStackTrace().toString());
            map.put("result", false);
            resultList.add(map);
        }
       
        if (con != null)
            con.disconnect();
        
		return resultList;
	}
	
}
