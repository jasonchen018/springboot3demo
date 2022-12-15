package org.cytobank.springboot3demo;

import org.cytobank.springboot3demo.constants.CaffeineCacheConstants;
import org.cytobank.springboot3demo.model.Input;
import org.cytobank.springboot3demo.utils.FileHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;

import java.util.Set;

@SpringBootTest
@EnableCaching
class Springboot3demoApplicationTests {
	@Autowired
	private CacheManager cacheManager;

	@Test
	void contextLoads() {
		FileHandler fileHandler = new FileHandler();
//    String path = "/Users/jasonchen/Downloads/cis-spike/efs-channel-stripes/9/9/7/99716c36d7a67ed00e94b7636992738bed33e70c4e4f17878561c4280cc22eb540d6ede0c1a290d453b2d113e5f47bc321565b92656574923a03ca5e38dd1a05:17";
//		String path = "/Users/jasonchen/cytobank-data/cytobank/Temp/Cache/cytobank_development_experiments/832/experiment_832_cache/cellsByGenes_VarianceOrdered.fcs/channel_0";
		// 200M
//		String path = "/Users/jasonchen/cytobank-data/cytobank/Temp/Cache/cytobank_development_experiments/834/experiment_834_cache/cytoflex_25m_tube4.fcs/channel_0";
//		Input input = new Input();
//		input.setPath(path);
//		fileHandler.handleRequest(input, null);

		CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache(CaffeineCacheConstants.CACHE_NAME);
		com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
		nativeCache.put("s", "s");
		nativeCache.put("sd", "sd");
		for (int i = 0; i < nativeCache.asMap().keySet().size(); i++) {
			System.out.println(nativeCache.asMap().keySet().toArray()[i]);
		}
	}
}
