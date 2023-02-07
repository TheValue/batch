package com.values.batch.main.persistence;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MainMapper {
	
	// 서버 날짜 조회 - 직전영업일 조회
	List<Map<String, Object>> findToday(Map<String, Object> param);
	
	// 배치 리스트 조회 
	List<Map<String, Object>> findBatchList(Map<String, Object> param);
	
	// 배치 로그 기록 
	void saveBatchLog(Map<String, Object> param);
	
	// 공공데이터포털 - 발행채권정보 기준일 날짜 기준으로 삭제 
	void saveOpenBondInfoRemove(Map<String, Object> param);
	
	// 공공데이터포털 - 발행채권정보 기준일 날짜 기준으로 추출데이터 저장 (1000건씩 일괄배치로 저장)
	void saveOpenBondInfo(Map<String, Object> param);
	
	// 공공데이터포털 - 주식정보 기준일 날짜 기준으로 삭제 
	void saveOpenStockInfoRemove(Map<String, Object> param);
	
	// 공공데이터포털 - 주식정보 기준일 날짜 기준으로 추출데이터 저장 (1000건씩 일괄배치로 저장)
	void saveOpenStockInfo(Map<String, Object> param);
	
	// 공공데이터포털 - 지수시세(코스피,코스닥) 기준일 날짜 기준으로 삭제 
	void saveOpenKosInfoRemove(Map<String, Object> param);
	
	// 공공데이터포털 - 지수시세(코스피,코스닥) 기준일 날짜 기준으로 추출데이터 저장 (1000건씩 일괄배치로 저장)
	void saveOpenKosInfo(Map<String, Object> param);
	
	// 크롤링 - KOFIA BOND 사이트 적용신용등급 커브 리스트 헤더 정보 추출 저장 
	void saveInsertKofiaHeaderList(Map<String, Object> params);
	
	// 크롤링 - KOFIA BOND 사이트 적용신용등급 커브 리스트 실 데이터 추출 저장
	void saveInsertKofiaBondList(Map<String, Object> params);
	
	// DART - 기업 공시코드 전체 삭제 
	void saveOpenDartMazzCodeRemove(Map<String, Object> param);
	
	// DART - 기업 공시코드 추출데이터 저장 
	void saveOpenDartMazzCode(Map<String, Object> param);
	
	// 공공데이터포털 - 발행채권정보 데이터 리스트 조회 - MAZZ_YN 이 ('비분리형BW', 'CB', '분리형BW', 'EB') 인 항목들만 조회
	List<Map<String, Object>> findOpenDataBondInfo(Map<String, Object> param);
	
	// 발행정보 ISSUER 이름으로 통해 기업 공시코드 조회 
	List<Map<String, Object>> findOpenDartMazzCode(Map<String, Object> param);
	
	// DART - 메짜닌 발행정보 전체 삭제
	void saveOpenDartMazzInfoRemove(Map<String, Object> param);
	
	// DART - 메짜닌 발행정보 추출데이터 저장
	void saveOpenDartMazzInfo(Map<String, Object> param);
	
	
}
