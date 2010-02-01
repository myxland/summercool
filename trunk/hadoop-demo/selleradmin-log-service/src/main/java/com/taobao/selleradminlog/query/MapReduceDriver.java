/**
 * 
 */
package com.taobao.selleradminlog.query;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import com.taobao.selleradminlog.Constants;

/**
 * ִ�в�ѯ��map-reduce����
 * @author guoyou
 */
public class MapReduceDriver {

	private static org.apache.commons.logging.Log log = LogFactory.getLog(MapReduceDriver.class);
	
	/**
	 * ������������ѯ��־�����ز�ѯ����ļ���PathName��
	 * @param condition
	 * @return output path and name.
	 * @throws Exception
	 */
	public static String query(QueryCondition condition) throws QueryException {
		try {

			Configuration conf = new Configuration();
			conf.set("hadoop.job.ugi", "hadoop,hadoop");
			conf.set("mapred.system.dir", "/home/hadoop/hadoop-datastore/mapred/system");
			conf.setInt("dfs.datanode.socket.write.timeout", 0);
			conf.set("mapred.child.java.opts", "-Xmx200m -Xdebug -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=n");
			
			//		���ò�ѯ������
			DistributedQueryCondition.setQueryCondition(condition, conf);

			Job job = new Job(conf, "log query");
			job.setJarByClass(MapReduceDriver.class);

			//		 ���ݲ�ѯ�������ˡ�
			job.setMapperClass(FilterMapper.class);
			job.setReducerClass(NullValueReducer.class);
			job.setNumReduceTasks(1);
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(IntWritable.class);  // null

			job.setInputFormatClass(InputFormat.class);
			FileInputFormat.setInputPathFilter(job, FileFilter.class );  // 
			FileInputFormat.setInputPaths(job, getInputPaths(condition) );

			job.setOutputFormatClass(OutputFormat.class);
			Path outputPath =  getOutputPath(condition);
			FileOutputFormat.setOutputPath(job, outputPath );

			//		 ���ݲ�ѯ��������
			job.setSortComparatorClass(Comparator.class); 

			job.waitForCompletion(true);

			return new Path(outputPath, OutputFormat.getFileName()).toString();
		}catch (IOException e) {
			throw new QueryException("IOException", e);
		} catch (InterruptedException e) {
			throw new QueryException("InterruptedException", e);
		} catch (ClassNotFoundException e) {
			throw new QueryException("ClassNotFoundException", e);
		}
	}
	
	private static Path getOutputPath(QueryCondition condition) {
		return new Path("selleradmin/workspace/query/result/" + 
				condition.getMainId() + 
				"-" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format( new Date(System.currentTimeMillis())) + 
				"-" + (int)(Math.random() * 1000) );
	}


	/**
	 * Get input paths for the the map-reduce job by QueryCondition. 
	 * @param condition
	 * @return
	 */
	private static Path[] getInputPaths(QueryCondition condition) {
		Long mainId = condition.getMainId();
		Date startTime = condition.getStartTime();
		Date endTime = condition.getEndTime();
		
		if (endTime == null) {
//			Ĭ�Ͻ�ֹ����ǰʱ�䡣
			endTime = new Date( System.currentTimeMillis() );
		}
		
		long oneday = 24*60*60*1000;
		
//		Ĭ���ṩ30��ġ�
		int days = startTime == null ? 30 : (int)( ( endTime.getTime() - startTime.getTime() )/oneday + 1);
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		
		Path[] paths = new Path[days];
		
		int hash = hash(mainId) % 128;
		
		long time = endTime.getTime();
		for (int i = 0; i < days; i++) {
			paths[i] = new Path("selleradmin/base/" + dateFormat.format(new Date(time )) + "/" + hash);
			time -= oneday;
		}
		
		return paths;
	}
	
	public static int hash(Long id) {
		return id.hashCode();
	}


	/**
	 * ����<hash(id)%128>/������ļ���ֻ���ܲ�ѯ������ָ����mainId����־��
	 * ���filter��FileInputFormat�Ļ�ʵ���У���ͬʱ����file��file��parent dir��
	 * ������Ŀ���Զ����InputFormatֻ�������filter����dir�����file��������ʵ�ֲ����dir���˵���
	 * @author guoyou
	 */
	public static class FileFilter extends Configured implements PathFilter {
		@Override
		public boolean accept(Path path) {
			QueryCondition c = DistributedQueryCondition.getQueryCondition( getConf() );
			return path.getName().contains( String.valueOf( c.getMainId() ) );
		}
	}
	
	public static void main(String[] args) throws Exception {
		QueryCondition c = new QueryCondition();
		c.setMainId(1026L);
		try {
			c.setStartTime(new SimpleDateFormat("yyyyMMddHHmmss").parse("20100120183059"));
			c.setEndTime(new SimpleDateFormat("yyyyMMddHHmmss").parse("20100130183059"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		c.setSortField(Constants.COLUMN_NAME_TIME);
		c.setDescending(true);
		String result = query(c);
		log.info("Query success, result=" + result);
	}
	
}
