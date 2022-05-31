package br.com.i2e.shop9;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class FormatDate {

	public static void main( String[] args ) throws ParseException {
		DateTimeFormatter df = DateTimeFormatter.ofPattern( "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX");

	    String toParse = "2021-07-17T00:40:37.9104172+00:00";
//	    LocalDateTime date = .;
	    df.parse(toParse);
	    LocalDateTime parsedDate = LocalDateTime.parse(toParse, df);
	    System.out.println( parsedDate  );
	    
	    System.out.println( LocalDateTime.now() + "==" + ZonedDateTime.now( ZoneId.of( "UTC" ) ).plusMinutes( 1 ) );
	}

}
