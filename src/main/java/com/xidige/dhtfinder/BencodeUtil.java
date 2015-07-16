package com.xidige.dhtfinder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.googlecode.jbencode.Parser;
import com.googlecode.jbencode.Value;
import com.googlecode.jbencode.composite.DictionaryValue;

public class BencodeUtil {
	private static Parser parser=new Parser();
	public static Value<?> decode(InputStream in)throws IOException{
		return parser.parse(in);
	}
	public static Value<?> decode(byte[]input) throws IOException{
		ByteArrayInputStream is=new ByteArrayInputStream(input);
		return decode(is);
	}
	public static DictionaryValue decodeDict(byte[]input )throws IOException{		
		Value<?>value=decode(input);
		if (value instanceof DictionaryValue) {
			return (DictionaryValue) value;
		}
		throw new IOException("Can't be cast to Dictionary");
	}
	
}
