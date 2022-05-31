package br.com.i2e.shop9.client;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import br.com.i2e.shop9.model.Signature;

@Component
public class SignatureUtil {
	private static final Logger logger = LoggerFactory.getLogger( SignatureUtil.class );
	
	@Autowired
	private Environment env;

	public Signature assign( String method, Optional<String> payload ) {

		var sig = new Signature();
		String passwd = env.getProperty( "i2e.shop9.password" );
		String info = method.toLowerCase() + sig.getTimestamp() + payload.orElse( "" );
		logger.info( "sig info: {}", info );
		try {

			var sha256_hmac = Mac.getInstance( "HmacSHA256" );
			var secret_key = new SecretKeySpec( passwd.getBytes( "UTF-8" ), "HmacSHA256" );
			sha256_hmac.init( secret_key );
			sig.setToken( Base64.getEncoder().encodeToString( sha256_hmac.doFinal( info.getBytes( "UTF-8" ) ) ) );
		} catch ( NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException e ) {

			e.printStackTrace();
		}

		return sig;
	}
}