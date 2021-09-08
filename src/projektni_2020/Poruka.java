package projektni_2020;


import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Flow.Publisher;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.encoders.Base64;






public class Poruka {
	
		public static String originalnaPoruka;
		public byte[] kriptovanaPoruka;
	
		private byte[] kljuc; // simetricni
		private byte[] kriptovanKljuc;
		
		public static Korisnik primalac;
		public static boolean prvaKomunikacija=true;
		public static boolean poslednjaKomunikacija=true;

		
		private byte[] potpis;
		public static String imePosiljaoca;
		
		static public byte[] sviBajtoviZaSliku;
		
		public static BufferedImage slikaZaSteg;
		public static File putanjaDoSlike;
		public int brojac=0;
		public Poruka(String poruka)	{
			originalnaPoruka = poruka;
		}
		
		public Poruka(byte[] kriptovanKljuc, byte[] kriptovanaPoruka, byte[] potpis)	{
			this.kriptovanaPoruka = kriptovanaPoruka;
			this.kriptovanKljuc = kriptovanKljuc;
			this.potpis = potpis;
		}
		
		private byte[] getPotpis() {
			return potpis;
		}
		
		public void setPrimalac(String primalac)	{
			for(Korisnik k : Korisnik.kreiraniNalozi)
			{
				if(k.getIme().equals(primalac))
				{
					this.primalac = k;
				}
			}
		}
		
		public void sacuvaj() throws IOException {
			PrintWriter pwPrintWriter= new PrintWriter(new FileWriter(new File("inbox"+File.separator+Poruka.primalac.ime+File.separator+Korisnik.trenutniPrijavljeni.ime+File.separator+brojac+".txt"),true));
			brojac++;
		     pwPrintWriter.println(Korisnik.trenutniPrijavljeni.ime+"@@@@@"+Base64.toBase64String(this.kriptovanKljuc)+"@@@@@"+Base64.toBase64String(this.kriptovanaPoruka)+"@@@@@"+Base64.toBase64String(this.potpis));
		     pwPrintWriter.close();
		}
		
		
		public void kriptujPoruku() throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException
		{
			this.kriptovanaPoruka = EnkripcijaDekripcija.kriptujPoruku(originalnaPoruka);	
		}
		public void kriptujPorukuStego() throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException
		{
			this.kriptovanaPoruka = EnkripcijaDekripcija.kriptujPorukuStego(originalnaPoruka);	
		}
		
		public void kriptujKljuc() throws FileNotFoundException, CertificateException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException
		{
			this.kriptovanKljuc = EnkripcijaDekripcija.kriptujKljuc(this.primalac.putanjaDoSertifikata);
		}
		
		public void potpis() throws NoSuchAlgorithmException, IOException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, SignatureException
		{
			this.potpis = EnkripcijaDekripcija.digitalanPotpis(originalnaPoruka.getBytes(), EnkripcijaDekripcija.simetricanKljuc);
		}
		
		public void kreirajBajtoveZaSliku()
		{
			this.sviBajtoviZaSliku = Steganografija.kreirajBajtoveZaSliku(this.kriptovanKljuc, this.kriptovanaPoruka, this.potpis);
		}
				
		public Korisnik getPrimalac()
		{
			return primalac;
		}
		
		public boolean prevelikaPorukaZaDatuSliku()
		{
			int sirina = slikaZaSteg.getWidth();
			int duzina = slikaZaSteg.getHeight();
	
			if(sirina*duzina < (this.sviBajtoviZaSliku.length*9))
			{	
				return true;
			}
			return false;
		}
			
		public static boolean verifikacijaPoruke(File putanjaDoSlike) throws NoSuchAlgorithmException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, CertificateException, SignatureException, NoSuchProviderException
		{
			BufferedImage bufferedImage=null;
			if(putanjaDoSlike != null)
			{			 
					 try {				 
			            bufferedImage = ImageIO.read(putanjaDoSlike);    
			           // Image image = SwingFXUtils.toFXImage(bufferedImage, null);		           
			         } catch (IOException ex) { }
			 }	
			Poruka procitana = Steganografija.procitajPoruku(bufferedImage); // IZ SLIKE !!!
			Steganografija.procitana = procitana;
			Steganografija.procitana.putanjaDoSlike = putanjaDoSlike;
		
			byte[] dekriptovanKljuc = EnkripcijaDekripcija.dekriptujKljuc(procitana.kriptovanKljuc);
			procitana.kljuc = dekriptovanKljuc;
			byte[] dekriptovanaPoruka = EnkripcijaDekripcija.dekriptujPoruku(procitana.kriptovanaPoruka);
			procitana.originalnaPoruka = new String(dekriptovanaPoruka);
			Steganografija.procitana.originalnaPoruka = new String(dekriptovanaPoruka);
			
			
			String posiljalacPoruke  = (new String(dekriptovanaPoruka)).split("@@@@@")[1];
			Korisnik posiljalac = new Korisnik(posiljalacPoruke);
	
			byte[] potpisKriptovan = procitana.getPotpis();
			String porukaPrava = (new String(dekriptovanaPoruka)).split("@@@@@")[0]; // korisni sadrzaj poruke	
			byte[] zaHash = new byte[dekriptovanKljuc.length + porukaPrava.getBytes().length];
			for(int i= 0; i<porukaPrava.getBytes().length;i++){
				zaHash[i]=porukaPrava.getBytes()[i];
			}	
			for(int i = 0; i<dekriptovanKljuc.length;i++){
				zaHash[porukaPrava.getBytes().length + i] = dekriptovanKljuc[i];
			}
			boolean ok =  EnkripcijaDekripcija.verifikacija(potpisKriptovan, posiljalac,zaHash ); 
			if(ok){return true;}else {return false;}
		}
		
		public static String citanje (File putanja) 
		{

			String rez="";
			try {
			BufferedReader brBufferedReader=new BufferedReader(new FileReader(putanja));
			String line="";
			while ((line=brBufferedReader.readLine())!= null)
			{
				String posiljaoc = line.split("@@@@@")[0];
				String kljuc= line.split("@@@@@")[1];
				String poruka= line.split("@@@@@")[2];
				String potpis= line.split("@@@@@")[3];
				byte[] dek_kljuc=EnkripcijaDekripcija.dekriptujKljuc(Base64.decode(kljuc));

				byte[] dek_poruka=EnkripcijaDekripcija.dekriptujPoruku(Base64.decode(poruka));

				Poruka nova=new Poruka(new String(dek_poruka));
				nova.potpis = new byte[dek_poruka.length + dek_kljuc.length];
				for(int i= 0; i<dek_poruka.length;i++){
					nova.potpis[i]=dek_poruka[i];
				}	
				for(int i = 0; i<dek_kljuc.length;i++){
					nova.potpis[dek_poruka.length + i] = dek_kljuc[i];
				}

				if (EnkripcijaDekripcija.verifikacija(Base64.decode(potpis), new Korisnik(posiljaoc), nova.potpis)) { 
					
					System.out.println("Uspijesna verifikacija");
					rez+="Nova poruka!\nPosiljalac: "+posiljaoc+ "\nPoruka: "+ nova.originalnaPoruka+"\n";
				}
				else {
					System.out.println("Neuspijesna verifikacija");
					rez+="Neuspijesno verifikovanje poruke od strane "+posiljaoc;
				}
				
			}
			
			brBufferedReader.close();
			putanja.delete();
		} catch (Exception e) {
			// TODO: handle exception
		}
			return rez;
		}
		
}

