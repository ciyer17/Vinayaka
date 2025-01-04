package com.iyer.vinayaka.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

class SafeStoreTest {
	
	@Test
	void encryptCorrectPassword()
	throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException,
			NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
		String password = "password";
		byte[] salt = SafeStore.createSalt();
		IvParameterSpec iv = SafeStore.createIV();
		SecretKey key = SafeStore.generateKeyFromPassword(password, salt);
		String cipher = SafeStore.encrypt(password, key, iv);
		
		String password2 = "password";
		// Simulates fetching the salt from the database
		String saltConvert = Base64.getEncoder().encodeToString(salt);
		byte[] salt2 = Base64.getDecoder().decode(saltConvert);
		byte[] iv2 = iv.getIV();
		
		SecretKey key2 = SafeStore.generateKeyFromPassword(password2, salt2);
		String cipher2 = SafeStore.encrypt(password2, key2, new IvParameterSpec(iv2));
		
		Assertions.assertEquals(cipher, cipher2);
	}
	
	@Test
	void encryptWrongPassword()
	throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException,
			NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
		String password = "password";
		byte[] salt = SafeStore.createSalt();
		IvParameterSpec iv = SafeStore.createIV();
		SecretKey key = SafeStore.generateKeyFromPassword(password, salt);
		String cipher = SafeStore.encrypt(password, key, iv);
		
		String password2 = "wrongpassword";
		String saltConvert = Base64.getEncoder().encodeToString(salt);
		byte[] salt2 = Base64.getDecoder().decode(saltConvert);
		byte[] iv2 = iv.getIV();
		
		SecretKey key2 = SafeStore.generateKeyFromPassword(password2, salt2);
		String cipher2 = SafeStore.encrypt(password2, key2, new IvParameterSpec(iv2));
		
		Assertions.assertNotEquals(cipher, cipher2);
	}
}
