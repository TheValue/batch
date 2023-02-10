package com.values.batch.main.service;

import java.util.List;
import java.util.Map;

public interface MainService {
	/**
	 * NAME : saveBatchLog
	 * DESC : 배치 실행 시 성공 유무 및 메시지 로그 기록
	 * DATE : 2023-02-02
	 * TABLE : SPO_DATA_BATCH_LOG
	 * PARAM : 배치성공여부, 결과메시지, 성공 시 저장건수
	 */
	void saveBatchLog(Map<String, Object> params);
	
	/**
	 * NAME : saveOpenApiBondInfo
	 * DESC : 공공데이터포털 API 사용 - 발행채권정보 리스트 추출 및 저장 
	 * DATE : 2023-02-02
	 * TABLE : SPO_DATA_BOND_INFO
	 * PARAM : 직전영업일 (STD_DATE) 
	 * RETURN : 배치 결과
	 */
	Map<String, Object> saveOpenApiBondInfo(Map<String, Object> params);
	
	/**
	 * NAME : saveOpenApiStockInfo
	 * DESC : 공공데이터포털 API 사용 - 주식정보(주가 데이터) 리스트 추출 및 저장 
	 * DATE : 2023-02-02
	 * TABLE : SPO_DATA_STOCK_INFO
	 * PARAM : 직전영업일 (STD_DATE) 
	 * RETURN : 배치 결과
	 */
	Map<String, Object> saveOpenApiStockInfo(Map<String, Object> params);
	
	
	/**
	 * NAME : saveOpenApiKosInfo
	 * DESC : 공공데이터포털 API 사용 - 지수 시세 (KOSPI/KOSDAQ) 리스트 추출 및 저장 
	 * DATE : 2023-02-02
	 * TABLE : SPO_DATA_KOS_INFO
	 * PARAM : 직전영업일 (STD_DATE) 
	 * RETURN : 배치 결과
	 */
	Map<String, Object> saveOpenApiKosInfo(Map<String, Object> params);
	
	/**
	 * NAME : saveCrawlCurveInfo
	 * DESC : 크롤링 API 사용 - 적용신용등급 커브 등급 리스트 추출 및 저장 / kofiabond 사이트에서 데이터 추출
	 * DATE : 2023-02-02
	 * TABLE : SPO_KOFIA_BOND 
	 * REPORT_COMP_CD 컬럼이 KISNET 이 아닌 종목들 (기존 크롤링) 
	 * PARAM : 직전영업일 (STD_DATE) 
	 * RETURN : 배치 결과
	 */
	Map<String, Object> saveCrawlCurveInfo(Map<String, Object> params);
	
	/**
	 * NAME : saveOpenDartMazzCode
	 * DESC : DART API 사용 - 기업 공시코드 리스트 추출 및 저장 
	 * DATE : 2023-02-04
	 * TABLE : SPO_DATA_MAZZ_CODE
	 * PARAM : 직전영업일 (STD_DATE) 
	 * RETURN : 배치 결과
	 */
	Map<String, Object> saveOpenDartMazzCode(Map<String, Object> params);
	
	/**
	 * NAME : saveOpenDartMazzInfo
	 * DESC : DART API 사용 - 기업 공시코드 및 공공데이터포털 채권발행정보를 통하여 메짜닌 리스트 추출 및 저장 
	 * DATE : 2023-02-04
	 * TABLE : SPO_DATA_MAZZ_INFO
	 * PARAM : 직전영업일, 기준일, 기업코드, MAZZ_YN 
	 * RETURN : 배치 결과
	 */
	Map<String, Object> saveOpenDartMazzInfo(Map<String, Object> params);
	
	/**
	 * NAME : saveOpenSeibroMazzInfo
	 * DESC : SEIBRO API 사용 - BOND 발행정보의 ISIN (KR코드)를 통해 상세 정보를 추출 및 저장
	 * DATE : 2023-02-08
	 * TABLE : 
	 * PARAM : 직전영업일, ISIN 코드
	 * RETURN : 배치 결과
	 */
	Map<String, Object> saveOpenSeibroMazzInfo(Map<String, Object> params);
	
	
}
