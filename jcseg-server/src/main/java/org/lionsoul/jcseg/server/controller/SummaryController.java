package org.lionsoul.jcseg.server.controller;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.lionsoul.jcseg.ISegment;
import org.lionsoul.jcseg.extractor.SummaryExtractor;
import org.lionsoul.jcseg.extractor.impl.TextRankSummaryExtractor;
import org.lionsoul.jcseg.sentence.SentenceSeg;
import org.lionsoul.jcseg.server.JcsegController;
import org.lionsoul.jcseg.server.JcsegGlobalResource;
import org.lionsoul.jcseg.server.JcsegTokenizerEntry;
import org.lionsoul.jcseg.server.core.GlobalResource;
import org.lionsoul.jcseg.server.core.ServerConfig;
import org.lionsoul.jcseg.server.core.UriEntry;

/**
 * summary extract service handler
 * 
 * @author chenxin<chenxin619315@gmail.com>
*/
public class SummaryController extends JcsegController
{

    public SummaryController(
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
        int length = getInt("length", 86);
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
		final SummaryExtractor extractor = new TextRankSummaryExtractor(seg, new SentenceSeg());
		
		long s_time = System.nanoTime();
		final String summary = extractor.getSummaryFromString(text, length);
		double c_time = (System.nanoTime() - s_time)/1E9;
		
		final Map<String, Object> map = new HashMap<>();
		final DecimalFormat df = new DecimalFormat("0.00000");
		map.put("took", Float.valueOf(df.format(c_time)));
		map.put("summary", summary);
		
		//response the request
		response(STATUS_OK, map);
    }

}
