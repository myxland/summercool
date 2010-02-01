package com.taobao.selleradminlog.idexing;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;




/**
 * Ĭ�������ʽTextInputFormat,�õ���һ��token��Ϊkey��sellerId,����Ϊvalue
 *
 * 
 * @author wuhao
 * 
 *
 */
public class RecordMapper extends Mapper<Text, Text, LongWritable, Text>{
	
	private Text id = new Text();

	@Override
	protected void map(Text key, Text value,Context context)
			throws IOException, InterruptedException {
		//one row one key, default partition
		StringTokenizer itr = new StringTokenizer(value.toString());
		if(itr.hasMoreTokens()){
//			id.set(itr.nextToken());
		    id.set(itr.nextToken());
			System.out.println("mapper:"+id.toString());
//			context.write(id, value);
		}
	}

	

}
