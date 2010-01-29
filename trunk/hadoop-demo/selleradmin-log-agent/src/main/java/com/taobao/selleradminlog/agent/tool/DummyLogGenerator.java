/**
 * 
 */
package com.taobao.selleradminlog.agent.tool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.IOUtils;

/**
 * ģ��log4j�����ɲ����õ���־�ļ���
 * @author guoyou
 */
public class DummyLogGenerator {
	
	private static Log log = LogFactory.getLog(DummyLogGenerator.class);

	/**
	 * ����ģ�����־�ļ���
	 * ��ʽ�� lineNum	yyyyMMddHHmmss.SSS	mainId	subId	operationType	object1 object2 memo
	 * <br>�������ļ���С��ϵ�ľ���ֵ��
	 * <br>����		��С
	 * <br>5000		1M
	 * <br>10000	2M
	 * <br>50000	10M
	 * <br>100000	20M
	 * <br>500000	100MM
	 * <br>5000000	1G
	 * @param filePathName �ļ���
	 * @param lineCount  �ļ�����
	 * @param mainIdCount ���˺�����
	 * @param subIdCount ÿ�������ҵ����˺�����
	 * @throws IOException
	 */
	public static void createLogFile(String filePathName, int lineCount, int mainIdCount, int subIdCount) throws IOException{
//		lineNum	yyyyMMddHHmmss.SSS	mainId	subId	operationType	object1 object2 memo
//		e.g. 1	20100127174514.235	10144	4	operation_type_4	object1-96	object2-6	Following is the testing memo: ���������Ĳ��ԡ� Tab sign: $$tt, change line sign: $$nn, Following is another line $$nnNewLineStart...
		
		long start = System.currentTimeMillis();
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss.SSS");
		
		File f = new File(filePathName);
		PrintWriter wr = null;
		try {
			wr = new PrintWriter( new BufferedWriter(new FileWriter(f)) );
			for (int i = 0; i < lineCount; i++ ) {
				wr.print( String.valueOf(i) );  // lineNum
				wr.print( '\t' );
				
				wr.print( dateFormat.format(new Date(System.currentTimeMillis())) );  // time
				wr.print( '\t' );
				
				wr.print( getRandomNum(10000, 10000 + mainIdCount) );  // mainId����С ID��10000��
				wr.print( '\t' );
				
				wr.print( getRandomNum(1, 1 + subIdCount) );  // subId
				wr.print( '\t' );
				
				wr.print( "operation_type_" + getRandomNum(0, 10) );  // operationType
				wr.print( '\t' );
				
				wr.print( "object1-" + getRandomNum(0, 100) );
				wr.print( '\t' );
				
				wr.print( "object2-" + getRandomNum(0, 100) );
				wr.print( '\t' );
				
				wr.println( "Following is the testing memo: ���������Ĳ��ԡ� Tab sign: $$tt, change line sign: $$nn, Following is another line $$nnNewLineStart..." );
			}
			
			wr.flush();
		} catch (IOException e) {
			throw e;
		}finally {
			IOUtils.closeStream(wr);
		}
		
		log.debug("Generated file " + filePathName + ", size=" + f.length()/1024 + "KB, lineCount=" + lineCount + ", time cost=" + (System.currentTimeMillis() - start));
		
	}
	
	/**
	 * ����start, end��Χ�ڵ����������
	 * @param start
	 * @param end
	 * @return
	 */
	private static long getRandomNum(long start, long end) {
		return (long)(Math.random() * (end - start)) + start;
	}
	
	
	public static void main(String[] args) {
//		test
		String file = "d:\\temp\\tt11.log";
		log.info("Create file " + file);
		try {
			createLogFile(file, 1000, 1000, 10);
		} catch (IOException e) {
			log.error("IOException.", e);
		}
	}
	
}
