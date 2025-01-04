package com.iyer.vinayaka.util;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class SafeStore {
	private static final String encryptionAlgorithm = "AES";
	private static final String algorithmOperationMode = "CBC";
	private static final String paddingScheme = "PKCS5Padding";
	private static final String keyGenerationAlgorithm = "PBKDF2WithHmacSHA256";
	private static final int keySize = 256;
	private static final int iterationCount = 65536;
	private static final int saltSize = 16;
	private static final int ivSize = 16;
	private static final String encryptionTransformation = encryptionAlgorithm + "/" +
			algorithmOperationMode + "/" + paddingScheme;
	
	/**
	 * Generates an AES secret key from the given password and salt.
	 * The AES operation mode is CBC. The key derivation function is
	 * PBKDF2 with HMAC SHA-256, the key size is 256 bits, iteration
	 * count is 65536, and the padding scheme is PKCS5Padding.
	 *
	 * @param password The password to encrypt.
	 * @param salt The salt to salt the password with. Use the createSalt()
	 *             method to generate a salt.
	 *
	 * @return The generated secret key.
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public static SecretKey generateKeyFromPassword(String password, byte[] salt)
	throws NoSuchAlgorithmException, InvalidKeySpecException {
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(keyGenerationAlgorithm);
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterationCount, keySize);
		return new SecretKeySpec(keyFactory.generateSecret(spec).getEncoded(), encryptionAlgorithm);
	}
	
	/**
	 * Generates a random salt to be used with the password.
	 *
	 * @return The generated salt.
	 */
	public static byte[] createSalt() {
		byte[] salt = new byte[saltSize];
		SecureRandom rng = new SecureRandom();
		rng.nextBytes(salt);
		return salt;
	}
	
	/**
	 * Generates a random IV to be used with the encryption and
	 * decryption operations.
	 *
	 * @return The generated IV.
	 */
	public static IvParameterSpec createIV() {
		byte[] ivBytes = new byte[ivSize];
		SecureRandom rng = new SecureRandom();
		rng.nextBytes(ivBytes);
		return new IvParameterSpec(ivBytes);
	}
	
	/**
	 * Encrypts the given password using the AES encryption algorithm.
	 * Encodes the encrypted password in Base64.
	 *
	 * The AES operation mode is CBC. The key derivation function is
	 * PBKDF2 with HMAC SHA-256, the key size is 256 bits, iteration
	 * count is 65536, and the padding scheme is PKCS5Padding.
	 *
	 * @param password The password to encrypt.
	 * @return The Base64 encoded encrypted password.
	 *
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidAlgorithmParameterException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static String encrypt(String password, SecretKey key, IvParameterSpec iv)
	throws NoSuchPaddingException, NoSuchAlgorithmException,
			InvalidAlgorithmParameterException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException {
		
		Cipher cipher = Cipher.getInstance(encryptionTransformation);
		cipher.init(Cipher.ENCRYPT_MODE, key, iv);
		byte[] encryptedPassword = cipher.doFinal(password.getBytes());
		return Base64.getEncoder().encodeToString(encryptedPassword);
	}
}
