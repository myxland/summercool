/**
 * 
 */
package com.taobao.selleradminlog.query.tool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

import com.taobao.selleradminlog.query.MapReduceDriver;


/**
 * ģ��log4j�����ɲ����õ�base��־�ļ���
 * @author guoyou
 */
public class DummyBaseLogGenerator {
	
	private static Log log = LogFactory.getLog(DummyBaseLogGenerator.class);

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
	public static void createLogFile(String filePathName, int lineCount, long[] mainIds, int subIdCount, Date date) throws IOException{
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
				
				Calendar c = Calendar.getInstance();
				c.setTimeInMillis(System.currentTimeMillis());
				
//				�������ò������date�滻��
				Calendar c1 = Calendar.getInstance();
				c1.setTime(date);
				c.set(Calendar.YEAR, c1.get(Calendar.YEAR));
				c.set(Calendar.MONTH, c1.get(Calendar.MONTH));
				c.set(Calendar.DAY_OF_MONTH, c1.get(Calendar.DAY_OF_MONTH));
				wr.print( dateFormat.format(c.getTime()) );  // time
				wr.print( '\t' );
				
				wr.print( mainIds[(int)getRandomNum(0, mainIds.length)] );  // ���ѡһ��mainId
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
	 * ����start, end��Χ�ڵ��������������start��������end��
	 * @param start
	 * @param end
	 * @return
	 */
	private static long getRandomNum(long start, long end) {
		return (long)(Math.random() * (end - start)) + start;
	}
	
	
	public static void main(String[] args) throws IOException {
//		Ϊһ����Χ�ڵ�mainId������һ��ʱ�䷶Χ��һ���ļ���С��base�ļ������ϴ���
		for (long mainId = 1026L; mainId<=1026L ; mainId++ ) {
			double sizeInMb = 0.01;
			for (int date = 20100202;date<=20100202; date++) {
				String dateStr= String.valueOf(date);
				createAndUploadBaseLog(mainId, dateStr, sizeInMb);
			}
		}
		
//		createLogFile(mainId, dateStr, sizeInMb);
		
	}
	
	public static Path createLogFile(long mainId, String dateStr, double sizeInMb) {
		Date date = null;
		try {
			date = new SimpleDateFormat("yyyyMMdd").parse(dateStr);
		} catch (ParseException e1) {
		}
		String name = mainId + "-" + dateStr + ".log";
		String file = "d:\\temp\\" + name;
//		log.info("Create file " + file);
		try {
			int lineCount = (int) Math.round( sizeInMb * 5000 );
			createLogFile(file, lineCount, new long[]{mainId}, 10, date);
		} catch (IOException e) {
			log.error("IOException.", e);
		}
		return new Path(file);
	}
	
	public static void createAndUploadBaseLog(long mainId, String dateStr, double sizeInMb) throws IOException {
		Path filePath = createLogFile(mainId, dateStr, sizeInMb);
		
		HadoopConfiguration conf = HadoopConfiguration.getHadoopConfiguration();
		
//		e.g. "hdfs://ubuntu:9000/user/hadoop/selleradmin/base/20100128/2/1026-20100128.log";
		String dstUri = "hdfs://" + conf.getNamenodeHost() + ":" + conf.getNamenodePort() 
		+ "/user/" + conf.getUserName() + "/" + conf.getBaseFileRelativePath()
		+ dateStr + "/" + MapReduceDriver.hash(mainId)%128 + "/" + filePath.getName();
		
        FileSystem fs = FileSystem.get(URI.create(dstUri),conf.getConfiguration() );
        fs.copyFromLocalFile(filePath , new Path(dstUri) );
        log.info("Uploaded file " + dstUri);
	}
	
}
