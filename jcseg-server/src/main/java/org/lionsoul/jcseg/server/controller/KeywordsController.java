package org.lionsoul.jcseg.server.controller;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.lionsoul.jcseg.ISegment;
import org.lionsoul.jcseg.extractor.impl.TextRankKeywordsExtractor;
import org.lionsoul.jcseg.server.JcsegController;
import org.lionsoul.jcseg.server.JcsegGlobalResource;
import org.lionsoul.jcseg.server.JcsegTokenizerEntry;
import org.lionsoul.jcseg.server.core.GlobalResource;
import org.lionsoul.jcseg.server.core.ServerConfig;
import org.lionsoul.jcseg.server.core.UriEntry;

/**
 * keywords extractor handler
 * 
 * @author  chenxin<chenxin619315@gmail.com>
*/
public class KeywordsController extends JcsegController
{

    public KeywordsController(
            ServerConfig config,
            GlobalResource globalResource, 
            UriEntry uriEntry,
            Request baseRequest, 
            HttpServletRequest request,
            HttpServletResponse response) throws IOException
    {
        super(config, globalResource, uriEntry, baseRequest, request, response);
    }

    @Override
    protected void run(String method) throws IOException
    {
        String text = getString("text");
        int number = getInt("number", 10);
        boolean autoFilter = getBoolean("autoFilter", false);
        if ( text == null || "".equals(text) ) {
            response(STATUS_INVALID_ARGS, "Invalid Arguments");
            return;
        }
        
        final JcsegGlobalResource resourcePool = (JcsegGlobalResource)globalResource;
        final JcsegTokenizerEntry tokenizerEntry = resourcePool.getTokenizerEntry("extractor");
        if ( tokenizerEntry == null ) {
            response(STATUS_INVALID_ARGS, "can't find tokenizer instance \"extractor\"");
            return;
        }
        
        final ISegment seg = ISegment.COMPLEX.factory.create(tokenizerEntry.getConfig(), tokenizerEntry.getDict());
		final TextRankKeywordsExtractor extractor = new TextRankKeywordsExtractor(seg);
		extractor.setKeywordsNum(number);
		extractor.setAutoFilter(autoFilter);
		
		long s_time = System.nanoTime();
		final List<String> keywords = extractor.getKeywordsFromString(text);
		double c_time = (System.nanoTime() - s_time)/1E9;
		
		final Map<String, Object> map = new HashMap<>();
		final DecimalFormat df = new DecimalFormat("0.00000");
		map.put("took", Float.valueOf(df.format(c_time)));
		map.put("keywords", keywords);
		
		//response the request
		response(STATUS_OK, map);
    }

}
