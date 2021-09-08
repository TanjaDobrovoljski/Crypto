package projektni_2020;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.*;

import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

public class EnkripcijaDekripcija {

	private static Cipher cipher;
	private static CertificateFactory cf;
	private static X509Certificate certificate;
	private static Signature sign;
	
	
	public static byte[] simetricanKljuc;
	
	// kriptovanje simetricnim algoritmom
	public static byte[] kriptujPoruku(String poruka) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
	{
		cipher=Cipher.getInstance("AES");			
		KeyGenerator generator = KeyGenerator.getInstance("AES");		
		Key k = generator.generateKey();
		simetricanKljuc = k.getEncoded(); // kljuc koristen za kriptovanje simetricnim algoritmom, 
		//potreban mi je kasnije za njegovu enkripciju
		
		cipher.init(Cipher.ENCRYPT_MODE, k);
		String vrijeme = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	//	String finalnaPoruka = poruka +"@@@@@" +Korisnik.trenutniPrijavljeni.getIme() + "@@@@@" + vrijeme; // ime posiljalaca cuvam kao dio poruke
		byte[] kriptovanaPoruka = cipher.doFinal(poruka.getBytes());
		return kriptovanaPoruka;	
	}
	
	// kriptovanje simetricnim algoritmom
		public static byte[] kriptujPorukuStego(String poruka) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
		{
			cipher=Cipher.getInstance("AES");			
			KeyGenerator generator = KeyGenerator.getInstance("AES");		
			Key k = generator.generateKey();
			simetricanKljuc = k.getEncoded(); // kljuc koristen za kriptovanje simetricnim algoritmom, 
			//potreban mi je kasnije za njegovu enkripciju
			
			cipher.init(Cipher.ENCRYPT_MODE, k);
			String vrijeme = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			System.out.println("ovdeeeeeeeeeeeeeeeeeeeee"+poruka);
			String finalnaPoruka = poruka +"@@@@@" +Korisnik.trenutniPrijavljeni.getIme() + "@@@@@" + vrijeme; // ime posiljalaca cuvam kao dio poruke
			byte[] kriptovanaPoruka = cipher.doFinal(finalnaPoruka.getBytes());
			return kriptovanaPoruka;	
		}
	
	// dekriptovanje poruke simetricnim kljucem, koji je prethodno dekriptovan iz slike
	public static byte[] dekriptujPoruku(byte[] kriptovanaPoruka) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException
	{
	//	System.out.println("fafafaUsaoUDekrippt "+kriptovanaPoruka);
		cipher = Cipher.getInstance("AES");	
		SecretKeySpec secretKeySpec = new SecretKeySpec(simetricanKljuc, "AES");
		cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
		byte[] dekriptovanaPoruka = cipher.doFinal(kriptovanaPoruka);
	//	 System.out.println("dekPoruka "+new String(dekriptovanaPoruka));

		return dekriptovanaPoruka; 
	
	}
	
	//kriptovanje kljuca asimetricnim algoritmom javnim kljucem primaoca poruke
	public static byte[] kriptujKljuc(File sertifikatPosiljoca) throws FileNotFoundException, CertificateException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException
	{
		FileInputStream fin = new FileInputStream(sertifikatPosiljoca);
		cf = CertificateFactory.getInstance("X.509");
		certificate = (X509Certificate)cf.generateCertificate(fin);
		
		PublicKey pk = certificate.getPublicKey(); // javni kljuc posiljaoca
		
		cipher = Cipher.getInstance("RSA","BC");	// asimetrican algoritam
		cipher.init(Cipher.ENCRYPT_MODE , pk);
		byte[] kriptovanKljuc = cipher.doFinal(simetricanKljuc);

		return kriptovanKljuc;		
	}
	
	public static byte[] dekriptujKljuc(byte[] kriptovanKljuc) throws InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException
	{
		

		//PEMParser pem=new PEMParser(new FileReader(Korisnik.trenutniPrijavljeni.privatniKljuc));	
		File privatni = new File("kljucevi"+File.separator+Korisnik.trenutniPrijavljeni.ime+".key");
		PEMParser pem=new PEMParser(new FileReader(privatni));
		Object o = pem.readObject();		
		 pem.close();		
		JcaPEMKeyConverter converter=new JcaPEMKeyConverter().setProvider("BC");		
		KeyPair keys=converter.getKeyPair((PEMKeyPair) o);	
		
		cipher=Cipher.getInstance("RSA","BC");	
		cipher.init(Cipher.DECRYPT_MODE,keys.getPrivate());
		byte[] kljuc=cipher.doFinal(kriptovanKljuc); // dekriptovan kljuc ///////////////////////////////////////
		simetricanKljuc = kljuc; // potreban kasnije za dekripciju poruke

		return kljuc; ////////////////////////
	}
	
	public static byte[] digitalanPotpis(byte[] poruka,byte[] kljuc) throws NoSuchAlgorithmException, IOException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, SignatureException
	{
		byte[] zaHash = new byte[poruka.length + kljuc.length];	
		for(int i = 0; i< poruka.length;i++ )
		{
			zaHash[i] = poruka[i];
		}	
		for(int i = 0; i < kljuc.length ; i++)
		{
			zaHash[ poruka.length + i] = kljuc[i];
		}
		
		sign = Signature.getInstance("Sha256withRSA");		
	//	PEMParser pem = new PEMParser(new FileReader(""));
		PEMParser pem = new PEMParser(new FileReader(Korisnik.trenutniPrijavljeni.privatniKljuc));			
		Object o = pem.readObject();		
		pem.close();		
		JcaPEMKeyConverter converter=new JcaPEMKeyConverter().setProvider("BC");		
		KeyPair keys=converter.getKeyPair((PEMKeyPair) o);	
				
		sign.initSign(keys.getPrivate());
		sign.update(zaHash); 		
		byte[] potpis=sign.sign();
		return potpis;
	}
	
	// potrebno za verifikaciju
	public static boolean verifikacija(byte[] potpis,Korisnik posiljalac, byte[] potpis2) throws FileNotFoundException, InvalidKeyException, CertificateException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, SignatureException
	{
		FileInputStream fin = new FileInputStream(posiljalac.putanjaDoSertifikata);
		cf = CertificateFactory.getInstance("X.509");
		X509Certificate certificate = (X509Certificate)cf.generateCertificate(fin);
		PublicKey pk = certificate.getPublicKey();
			
		Signature sig = Signature.getInstance("SHA256withRSA");
		sig.initVerify(pk);
		sig.update(potpis2);
		return sig.verify(potpis);
	}
	
}

