/**
 * 
 */
package com.taobao.selleradminlog.query;

import java.util.Date;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;

/**
 * ��QueryCondition����洢��conf�У����ߴ�conf�ж�ȡ��QueryCondition����
 * @author guoyou
 */
public class DistributedQueryCondition {

	public static void setQueryCondition(QueryCondition condition, Configuration conf) {
	    conf.set("taobao.query.condition", conditionToString(condition));
	}
	
	public static QueryCondition getQueryCondition(Configuration conf) {
		return stringToCondition(conf.get("taobao.query.condition"));
	}
	
	/**
	 * ��QueryConditionת���ַ�������������":;"�ָ���key��value֮����'='�ָ���
	 * @param c
	 * @return
	 */
	public static String conditionToString(QueryCondition c) {
		if (c == null) {
			return null;
		}
		
		StringBuilder sb = new StringBuilder();
		
		if (c.getMainId() != null) {
			sb.append("mainId=").append(Long.valueOf(c.getMainId()));
			sb.append(":;");
		}
		if (c.getSubIds() != null) {
			sb.append("subIds=");
			int i = 0;
			for (Long subId : c.getSubIds()) {
				if (i > 0) {
					sb.append(",");
				}
				sb.append(Long.valueOf(subId));
				i++;
			}
			sb.append(":;");
		}
		if (c.getStartTime()!= null) {
			sb.append("startTime=").append( c.getStartTime().getTime());
			sb.append(":;");
		}
		if (c.getEndTime() != null) {
			sb.append("endTime=").append(c.getEndTime().getTime());
			sb.append(":;");
		}
		if (c.getOperationType() != null) {
			sb.append("operationType=").append(c.getOperationType());
			sb.append(":;");
		}
		if (c.getSortField() != null) {
			sb.append("sortField=").append(c.getSortField());
			sb.append(":;");
		}
		if (c.getSortField() != null) {
			sb.append("descending=").append(c.isDescending());
			sb.append(":;");
		}
		
		return sb.toString();
		
	}
	
	/**
	 * ���ַ���ת��QueryCondition. ��������":;"�ָ���key��value֮����'='�ָ���
	 * @param str
	 * @return
	 */
	public static QueryCondition stringToCondition(String str) {
		if (str == null) {
			return null;
		}
		
		QueryCondition c = new QueryCondition();
		StringTokenizer st = new StringTokenizer(str, ":;");
		while (st.hasMoreTokens()) {
	         String kv = st.nextToken();
	         int index = kv.indexOf('=');
	         String key = kv.substring(0, index);
	         String value = (index == (kv.length()-1))? "" : kv.substring(index + 1);
	         if ("mainId".equals(key)) {
	        	 c.setMainId(Long.valueOf(value));
	         }else if ("subIds".equals(key)) {
	        	 StringTokenizer subIdsSt = new StringTokenizer(value, ",");
	        	 Long[] subIds = new Long[subIdsSt.countTokens()];
	        	 for (int i = 0; i < subIds.length; i++) {
	        		 subIds[i] = Long.valueOf( subIdsSt.nextToken() );
	        	 }
	        	 c.setSubIds(subIds);
	         }else if ("startTime".equals(key)) {
	        	 c.setStartTime(new Date(Long.valueOf(value)));
	         }else if ("endTime".equals(key)) {
	        	 c.setEndTime(new Date(Long.valueOf(value)));
	         }else if ("operationType".equals(key)) {
	        	 c.setOperationType(value);
	         }else if ("sortField".equals(key)){
	        	 c.setSortField(value);
	        	 c.setDescending( Boolean.valueOf(value) );
	         }
	     }
		return c;
	}
	
}
