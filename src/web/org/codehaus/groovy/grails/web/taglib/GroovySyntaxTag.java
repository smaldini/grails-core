/* Copyright 2004-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.grails.web.taglib;

import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.grails.web.pages.GroovyPage;
import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.io.Writer;
import java.io.PrintWriter;

/**
 * A tag type that gets translated directly into Groovy syntax by the GSP parser
 *
 * @author Graeme Rocher
 * @since 11-Jan-2006
 */
public abstract class GroovySyntaxTag implements GrailsTag {
    protected static final String ATTRIBUTE_IN = "in";
	protected static final String ATTRIBUTE_VAR = "var";
	protected static final String ATTRIBUTES_STATUS = "status";
	protected Map tagContext;
    protected PrintWriter out;
    protected Map attributes = new HashMap();

    public void init(Map tagContext) {
        this.tagContext = tagContext;
        Object outObj = tagContext.get(GroovyPage.OUT);
        if(outObj instanceof PrintWriter) {
        	this.out = (PrintWriter)tagContext.get(GroovyPage.OUT);
        }
    }

    public void setWriter(Writer w) {
        if(w instanceof PrintWriter) {
            this.out = (PrintWriter)w;
        }
        else {
            throw new IllegalArgumentException("A GroovySynax tag requires a java.io.PrintWriter instance");
        }
    }

    public void setAttributes(Map attributes) {
        for (Iterator i = attributes.keySet().iterator(); i.hasNext();) {
            String attrName = (String) i.next();
            setAttribute(attrName,attributes.get(attrName));
        }
    }

    public void setAttribute(String name, Object value) {
        if(value instanceof String ) {
            String stringValue = ((String)value).trim();
            if(stringValue.startsWith("${") && stringValue.endsWith("}")) {
                stringValue = stringValue.substring(2,stringValue.length() -1);
            }

            this.attributes.put(name.substring(1,name.length()-1),stringValue);
        }
        else {
            throw new IllegalArgumentException("A GroovySynax tag requires only string valued attributes");
        }
    }

    public abstract boolean isBufferWhiteSpace();

    public abstract boolean hasPrecedingContent();

	protected String calculateExpression(String expr) {
		if(StringUtils.isBlank(expr )) {
			throw new IllegalArgumentException("Argument [expr] cannot be null or blank");
		}
		expr = expr.trim();
        if(expr.startsWith("\"") && expr.endsWith("\"")) {
            expr = expr.substring(1,expr.length()-1);
        }		
        expr = expr.trim();
        if(expr.startsWith("${") && expr.endsWith("}")) {
        	expr = expr.substring(2,expr.length()-1);
        }
        expr = expr.trim();
		return expr;
	}

	/**
	 * @param in
	 */
	protected void doEachMethod(String in) {
		String var = (String) attributes.get(ATTRIBUTE_VAR);
	    String status = (String)attributes.get(ATTRIBUTES_STATUS);
	
	
	    String methodName = StringUtils.isBlank(status) ? "each" : "eachWithIndex";
	    var = StringUtils.isBlank(var) ? "it" : var;
	    
	    if(var.equals(status))
	    	throw new GrailsTagException("Attribute ["+ATTRIBUTE_VAR+"] cannot have the same value as attribute ["+ATTRIBUTES_STATUS+"]");
	    out.print(in);  // object
	    out.print('.'); // dot de-reference
	    out.print(methodName); // method name                      
		out.print(" { "); // start closure
		out.print(var); // var name, normally it
		if(!StringUtils.isBlank(status)) { // if eachWithIndex add status
			out.print(",");
			out.print(status);
		}
		out.println(" ->"); // start closure body
	}
}
