package org.lionsoul.jcseg.server.controller;

import java.io.IOException;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.lionsoul.jcseg.ISegment;
import org.lionsoul.jcseg.IWord;
import org.lionsoul.jcseg.server.JcsegController;
import org.lionsoul.jcseg.server.JcsegGlobalResource;
import org.lionsoul.jcseg.server.JcsegTokenizerEntry;
import org.lionsoul.jcseg.server.core.GlobalResource;
import org.lionsoul.jcseg.server.core.ServerConfig;
import org.lionsoul.jcseg.server.core.UriEntry;

/**
 * tokenizer service handler
 * 
 * @author  dongyado<dongyado@gmail.com>
 * @author  chenxin<chenxin619315@gmail.com>
*/
public class TokenizerController extends JcsegController
{

    public TokenizerController(
            ServerConfig config,
            GlobalResource resourcePool, 
            UriEntry uriEntry,
            Request baseRequest, 
            HttpServletRequest request,
            HttpServletResponse response) throws IOException
    {
        super(config, resourcePool, uriEntry, baseRequest, request, response);
    }

    @Override
    protected void run(String method) throws IOException
    {
        String text = getString("text");
        if ( text == null || "".equals(text) ) {
            response(STATUS_INVALID_ARGS, "Invalid Arguments");
            return;
        }
        
        final JcsegGlobalResource resourcePool = (JcsegGlobalResource)globalResource;
        final JcsegTokenizerEntry tokenizerEntry = resourcePool.getTokenizerEntry(method);
        if ( tokenizerEntry == null ) {
            response(STATUS_INVALID_ARGS, "can't find tokenizer instance [" + method + "]");
            return;
        }
        
        final ISegment seg = ISegment.Type.fromIndex(tokenizerEntry.getAlgorithm())
				.factory.create(tokenizerEntry.getConfig(), tokenizerEntry.getDict());
		final List<IWord> list = new ArrayList<>();
		seg.reset(new StringReader(text));
		
		long s_time = System.nanoTime();
        IWord word = null;
		while ( (word = seg.next()) != null ) {
		    list.add(word);
		}
		
		double c_time = (System.nanoTime() - s_time)/1E9;
		final Map<String, Object> map = new HashMap<>();
		final DecimalFormat df = new DecimalFormat("0.00000");
		map.put("took", Float.valueOf(df.format(c_time)));
		map.put("list", list);
		
		//response the request
		response(STATUS_OK, map);
    }
}
