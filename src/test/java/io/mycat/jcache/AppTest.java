package io.mycat.jcache;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import com.whalin.MemCached.MemCachedClient;
import com.whalin.MemCached.SockIOPool;

import io.mycat.jcache.net.JcacheMain;
import junit.framework.Assert;

/**
 * Unit test for simple App.
 */
public class AppTest {
	
	MemCachedClient mcc = new MemCachedClient(true);  //true 代表 二进制协议，false 代表 文本协议
	
	@BeforeClass
    public static void setup() throws Exception{
		String[] args = {"-m 1024"};  //启动时 指定启动参数为  1024m
		
//		JcacheMain.main(args);
		
//		Thread.sleep(3000);
		
		
		// 设置缓存服务器列表，当使用分布式缓存的时，可以指定多个缓存服务器。这里应该设置为多个不同的服务，我这里将两个服务设置为一样的，大家不要向我学习，呵呵。
        String[] servers =
                {
                        "127.0.0.1:11211"
                };

        // 设置服务器权重
        Integer[] weights = {3};

        // 创建一个Socked连接池实例
        SockIOPool pool = SockIOPool.getInstance();

      // 向连接池设置服务器和权重
        pool.setServers(servers);
        pool.setWeights(weights);

        // set some TCP settings
        // disable nagle
        // set the read timeout to 3 secs
        // and don't set a connect timeout
        pool.setNagle(false);
        pool.setSocketTO(3000);
        pool.setSocketConnectTO(0);

       // initialize the connection pool
        pool.initialize();
    }
	
	
	/**
	 * 测试lru 需要设置  tailRepairTime参数大于零，或者 item 过期时间,使memcached 可以删掉掉 item.
	 * 否则,达到内存上限时,将不能够再保存新的item
	 */
	@Test
	public void testsetCommand1(){
		String value = "123";
//		for(int i=0;i<3000;i++){
//			value += "p0-['This is a test String1qazxsw23edcvfr45tgbhy6ujm,ki89ol./";
//		}
		String key = "foo0";
		boolean result;
		int j;
		for(j=0;j<10;j++){
			result = mcc.set("foo"+j, value);
	        System.out.println(result+":"+j);
	        Assert.assertEquals(result, true);
		}
		
		Assert.assertEquals(mcc.get(key), value);
		Assert.assertEquals(mcc.append(key, "234"), true);
		Assert.assertEquals(mcc.prepend(key, "34"), true);
		Assert.assertEquals(mcc.incr(key,2l), 34123236);
		Assert.assertEquals(mcc.decr(key,2l), 34123234);
		Assert.assertEquals(mcc.addOrIncr(key,1l), 34123235);
		Assert.assertEquals(mcc.addOrDecr(key,2l), 34123233);
		Assert.assertEquals(mcc.getCounter(key), 34123233);
		Assert.assertEquals(mcc.getMulti(new String[]{key}).get(key), "34123233");
		Assert.assertEquals(mcc.get(key), "34123233");
		Assert.assertEquals(mcc.keyExists(key), true);
		Assert.assertEquals(mcc.delete(key), true);
		Assert.assertNull(mcc.get(key));
		Assert.assertEquals(mcc.keyExists(key), false);
		
	}
	
	@Test
	public void testsetCommand(){
		Random ran = new Random();
		List<Thread> threads = new ArrayList<>();
		int teamnum = 10000;
		String value = "";
		for(int k=0;k<10;k++){
			value += "1qazxsw23edcvfr45tgbnhy6ujm,ki89ol./;p0-['";
		}
		final String value1 = value;
		for(int j = 1;j<=10;j++){
			final int k = j;
			Thread thread = new Thread(new Runnable(){

				@Override
				public void run() {
					
					for(int i=teamnum*(k-1);i<teamnum*k;i++){
						boolean result = mcc.set("foo"+i, value1+i);
				        System.out.println(result+":"+i);
				        Assert.assertEquals(result, true);
					}
				}
				
			});
			
			thread.start();
			threads.add(thread);
		}
		
		for(Thread thread:threads){
			try {
				thread.join();
			} catch (InterruptedException e) {
			}
		}
	}
}
